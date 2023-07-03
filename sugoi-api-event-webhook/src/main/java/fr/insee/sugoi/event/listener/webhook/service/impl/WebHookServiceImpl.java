/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fr.insee.sugoi.event.listener.webhook.service.impl;

import fr.insee.sugoi.core.event.configuration.EventKeysConfig;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.event.listener.webhook.service.TemplateConfigKeys;
import fr.insee.sugoi.event.listener.webhook.service.WebHookService;
import fr.insee.sugoi.model.exceptions.NoReceiverMailException;
import fr.insee.sugoi.model.exceptions.UserStorageNotFoundException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class WebHookServiceImpl implements WebHookService {

  public static final Logger logger = LoggerFactory.getLogger(WebHookServiceImpl.class);
  private static final String WEBHOOK_PROPERTY_PREFIX = "sugoi.api.event.webhook.";

  private static RestTemplate restTemplate = new RestTemplate();

  @Autowired private Environment env;

  @Autowired private RealmProvider realmProvider;

  @Override
  public void resetPassword(String webHookName, Map<String, Object> values) {
    if (!((List<?>) values.get(EventKeysConfig.MAILS)).isEmpty()) {
      sendRequestToWebhookFromTemplate(
          values, webHookName, TemplateConfigKeys.RESET_TEMPLATE, ".default.reset.template");
    } else {
      throw new NoReceiverMailException("There is no mail address to send the message to");
    }
  }

  @Override
  public void changePassword(String webHookName, Map<String, Object> values) {
    if (!((List<?>) values.get(EventKeysConfig.MAILS)).isEmpty()) {
      sendRequestToWebhookFromTemplate(
          values,
          webHookName,
          TemplateConfigKeys.CHANGEPWD_TEMPLATE,
          ".default.changepwd.template");
    } else {
      throw new NoReceiverMailException("There is no mail address to send the message to");
    }
  }

  @Override
  public void sendLogin(String webHookName, Map<String, Object> values) {
    if (!((List<?>) values.get(EventKeysConfig.MAILS)).isEmpty()) {
      sendRequestToWebhookFromTemplate(
          values, webHookName, TemplateConfigKeys.LOGIN_TEMPLATE, ".default.send-login.template");
    } else {
      throw new NoReceiverMailException("There is no mail address to send the message to");
    }
  }

  private void sendRequestToWebhookFromTemplate(
      Map<String, Object> templateProperties,
      String webHookName,
      TemplateConfigKeys realmTemplateKey,
      String propertyTemplateSuffix) {
    String template =
        loadTemplate(
            realmTemplateKey,
            webHookName,
            WEBHOOK_PROPERTY_PREFIX + webHookName + propertyTemplateSuffix,
            (String) templateProperties.get(EventKeysConfig.REALM),
            (String) templateProperties.get(EventKeysConfig.USERSTORAGE));
    if (template != null) {
      String content = injectValueInTemplate(template, templateProperties);
      String target = env.getProperty(WEBHOOK_PROPERTY_PREFIX + webHookName + ".target");
      String authType = env.getProperty(WEBHOOK_PROPERTY_PREFIX + webHookName + ".auth.type");
      Map<String, String> headers = new HashMap<>();
      headers.put("content-type", "application/json");
      if (authType != null) {
        addAuthHeader(webHookName, authType, headers);
      }
      send(target, content, headers);
    }
  }

  public void send(String target, String content, Map<String, String> headers) {
    try {
      HttpHeaders finalHeaders = new HttpHeaders();
      headers.keySet().stream().forEach(header -> finalHeaders.add(header, headers.get(header)));
      HttpEntity<String> request = new HttpEntity<>(content, finalHeaders);
      restTemplate.postForEntity(target, request, String.class);
      logger.info("Sending webHook to {} success", target);
    } catch (HttpServerErrorException | HttpClientErrorException e) {
      throw new RuntimeException(
          String.format(
              "Something went wrong on server when sending request to %s receive status %s response %s",
              target, e.getRawStatusCode(), e.getResponseBodyAsString()),
          e);
    } catch (Exception e) {
      throw new RuntimeException(
          "Something went wrong when sending webhook to target " + target, e);
    }
  }

  private void addAuthHeader(String webHookName, String authType, Map<String, String> headers) {
    if (authType.equalsIgnoreCase("basic")) {
      String username = env.getProperty(WEBHOOK_PROPERTY_PREFIX + webHookName + ".auth.user");
      String password = env.getProperty(WEBHOOK_PROPERTY_PREFIX + webHookName + ".auth.password");
      String auth = username + ":" + password;
      byte[] encodedAuth = Base64Utils.encode(auth.getBytes());
      String authHeader = "Basic " + new String(encodedAuth);
      headers.put("Authorization", authHeader);
    } else if (authType.equalsIgnoreCase("oauth")) {
      String tokenProperty = env.getProperty(WEBHOOK_PROPERTY_PREFIX + webHookName + ".auth.token");
      if (tokenProperty != null) {
        String token = env.getProperty(tokenProperty);
        headers.put("Authorization", "bearer " + token);
      }
    }
  }

  private String loadResource(String path) {
    try {
      ResourceLoader resourceLoader = new DefaultResourceLoader();
      Resource resource = resourceLoader.getResource(path);
      return IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8.name());
    } catch (IOException e) {
      throw new RuntimeException("Unable to load " + path, e);
    }
  }

  private String injectValueInTemplate(String content, Map<String, Object> values) {
    try {
      Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);
      Template t;
      t = new Template("name", new StringReader(content), cfg);
      Writer out = new StringWriter();
      t.process(values, out);
      return out.toString();
    } catch (IOException | TemplateException e) {
      throw new RuntimeException("Unable to inject data in template", e);
    }
  }

  private String loadTemplate(
      TemplateConfigKeys realmTemplateKey,
      String webhookName,
      String propertyTemplateConfiguration,
      String realmName,
      String userStorageName) {
    String templateLocation =
        createTemplateLocationMapByWebhook(
                realmProvider.load(realmName).orElseThrow().getUserStorages().stream()
                    .filter(us -> us.getName().equalsIgnoreCase(userStorageName))
                    .findFirst()
                    .orElseThrow(() -> new UserStorageNotFoundException(realmName, userStorageName))
                    .getProperties()
                    .get(realmTemplateKey))
            .get(webhookName);
    if (templateLocation != null) {
      try {
        return restTemplate.getForEntity(templateLocation, String.class).getBody();
      } catch (Exception e) {
        logger.error(
            "Could not retrieve template at location {} . Falling back on property. {}",
            templateLocation,
            e.getLocalizedMessage());
      }
    }
    if (propertyTemplateConfiguration != null) {
      return loadResource(env.getProperty(propertyTemplateConfiguration));
    }
    return null;
  }

  private Map<String, String> createTemplateLocationMapByWebhook(String allWebhookConfiguration) {
    if (allWebhookConfiguration != null) {
      return Arrays.stream(allWebhookConfiguration.split("\\|"))
          .collect(
              Collectors.toMap(
                  conf -> StringUtils.substringBefore(conf, ":"),
                  conf -> StringUtils.substringAfter(conf, ":")));
    } else return new HashMap<>();
  }
}
