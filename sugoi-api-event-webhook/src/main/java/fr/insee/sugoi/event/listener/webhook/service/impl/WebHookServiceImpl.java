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

import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.event.listener.webhook.service.WebHookService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
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

  public static final Logger logger = LoggerFactory.getLogger(WebHookService.class);

  @Autowired private Environment env;

  @Autowired private ConfigService configService;

  @Override
  public void send(String webHookName, String target, String content, Map<String, String> headers) {
    try {
      HttpHeaders _headers = new HttpHeaders();
      headers.keySet().stream().forEach(header -> _headers.add(header, headers.get(header)));
      RestTemplate restTemplate = new RestTemplate();
      HttpEntity<String> request = new HttpEntity<String>(content, _headers);
      restTemplate.postForEntity(target, request, String.class);
      logger.info("Sending webHook to {} success", target);
    } catch (HttpClientErrorException e) {
      logger.info(
          "Something went wrong when sending request to {} receive status {} response {}",
          target,
          e.getRawStatusCode(),
          e.getResponseBodyAsString());
      throw new RuntimeException(
          "Something went wrong when sending webhook to target " + target, e);
    } catch (HttpServerErrorException e) {
      logger.info(
          "Something went wrong on server when sending request to {} receive status {} response {}",
          target,
          e.getRawStatusCode(),
          e.getResponseBodyAsString());
      throw new RuntimeException(
          "Something went wrong when sending webhook to target " + target, e);
    } catch (Exception e) {
      logger.info("Unexpected error when producing webhook");
      throw new RuntimeException(
          "Something went wrong when sending webhook to target " + target, e);
    }
  }

  @Override
  public void resetPassword(String webHookName, Map<String, Object> values) {
    String template = null;
    try {
      template =
          configService
              .getRealm((String) values.get(GlobalKeysConfig.REALM))
              .get()
              .getUserStorages()
              .stream()
              .filter(
                  us ->
                      us.getName()
                          .equalsIgnoreCase((String) values.get(GlobalKeysConfig.USERSTORAGE)))
              .findFirst()
              .get()
              .getProperties()
              .get(webHookName + "_reset_template");
    } catch (Exception e) {
      // we don't need to manage this exception here
    }
    if (template == null) {
      template =
          loadResource(
              env.getProperty(
                  "sugoi.api.event.webhook." + webHookName + ".default.reset.template"));
    }

    String content = injectValueInTemplate(template, values);
    String target = env.getProperty("sugoi.api.event.webhook." + webHookName + ".target");
    String authType = env.getProperty("sugoi.api.event.webhook." + webHookName + ".auth.type");
    Map<String, String> headers = new HashMap<>();
    headers.put("content-type", "application/json");
    addAuthHeader(webHookName, authType, headers);
    send(webHookName, target, content, headers);
  }

  @Override
  public void initPassword(String webHookName, Map<String, Object> values) {
    String template = null;
    try {
      template =
          configService
              .getRealm((String) values.get(GlobalKeysConfig.REALM))
              .get()
              .getUserStorages()
              .stream()
              .filter(
                  us ->
                      us.getName()
                          .equalsIgnoreCase((String) values.get(GlobalKeysConfig.USERSTORAGE)))
              .findFirst()
              .get()
              .getProperties()
              .get(webHookName + "_init_template");
    } catch (Exception e) {
      // we don't need to manage this exception here
    }
    if (template == null) {
      template =
          loadResource(
              env.getProperty(
                  "sugoi.api.event.webhook." + webHookName + ".default.reset.template"));
    }
    String content = injectValueInTemplate(template, values);
    String target = env.getProperty("sugoi.api.event.webhook." + webHookName + ".target");
    String authType = env.getProperty("sugoi.api.event.webhook." + webHookName + ".auth.type");
    Map<String, String> headers = new HashMap<>();
    headers.put("content-type", "application/json");
    addAuthHeader(webHookName, authType, headers);
    send(webHookName, target, content, headers);
  }

  private void addAuthHeader(String webHookName, String authType, Map<String, String> headers) {
    if (authType.equalsIgnoreCase("basic")) {
      String username = env.getProperty("sugoi.api.event.webhook." + webHookName + ".auth.user");
      String password =
          env.getProperty("sugoi.api.event.webhook." + webHookName + ".auth.password");
      String auth = username + ":" + password;
      byte[] encodedAuth = Base64Utils.encode(auth.getBytes());
      String authHeader = "Basic " + new String(encodedAuth);
      headers.put("Authorization", authHeader);
    } else if (authType.equalsIgnoreCase("oauth")) {
      String token =
          env.getProperty(
              env.getProperty("sugoi.api.event.webhook." + webHookName + ".auth.token"));
      headers.put("Authorization", "bearer " + token);
    }
  }

  private String loadResource(String path) {
    try {
      ResourceLoader resourceLoader = new DefaultResourceLoader();
      Resource resource = resourceLoader.getResource(path);
      return new String(IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8.name()));
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
}
