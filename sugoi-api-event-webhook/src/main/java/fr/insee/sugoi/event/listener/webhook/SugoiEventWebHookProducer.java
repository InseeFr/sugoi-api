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
package fr.insee.sugoi.event.listener.webhook;

import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.core.event.configuration.EventKeysConfig;
import fr.insee.sugoi.core.event.model.SugoiEvent;
import fr.insee.sugoi.core.event.model.SugoiEventTypeEnum;
import fr.insee.sugoi.event.listener.webhook.service.WebHookService;
import fr.insee.sugoi.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@ConditionalOnProperty(name = "sugoi.api.event.webhook.enabled", havingValue = "true")
@EnableAsync
public class SugoiEventWebHookProducer {

  @Value("${sugoi.api.event.webhook.name}")
  private List<String> webHookNames;

  @Autowired private Environment env;

  @Autowired private WebHookService webHookService;

  @Async
  @EventListener
  public void handleContextStart(SugoiEvent cse) {
    SugoiEventTypeEnum eventType = cse.getEventType();
    String webHookTag = (String) cse.getProperties().get(EventKeysConfig.WEBSERVICE_TAG);
    Map<String, Object> values = getValuesForTemplateFromEvent(cse);
    switch (eventType) {
      case RESET_PASSWORD:
        webHookNames.stream()
            .filter(
                webHookName ->
                    ((env.getProperty("sugoi.api.event.webhook." + webHookName + ".tag"))
                        .equalsIgnoreCase(webHookTag)))
            .forEach(webHookName -> webHookService.resetPassword(webHookName, values));
        break;
      case SEND_LOGIN:
        webHookNames.stream()
            .filter(
                webHookName ->
                    ((env.getProperty("sugoi.api.event.webhook." + webHookName + ".tag"))
                        .equalsIgnoreCase(webHookTag)))
            .forEach(webHookName -> webHookService.sendLogin(webHookName, values));
        break;
      default:
        break;
    }
  }

  private Map<String, Object> getValuesForTemplateFromEvent(SugoiEvent event) {
    Map<String, Object> values = new HashMap<>();

    User user = (User) event.getProperties().get(EventKeysConfig.USER);
    String password = (String) event.getProperties().get(EventKeysConfig.PASSWORD);
    String realm = event.getRealm();
    String userStorage = event.getUserStorage() != null ? event.getUserStorage() : "default";

    values.put(GlobalKeysConfig.REALM, realm);
    values.put(GlobalKeysConfig.USERSTORAGE, userStorage);
    values.put(EventKeysConfig.USER, user);
    values.put(EventKeysConfig.MAIL, event.getProperties().get(EventKeysConfig.MAIL));
    values.put(EventKeysConfig.PASSWORD, password);
    values.put(EventKeysConfig.USER_ID, user.getUsername());
    values.put(EventKeysConfig.PROPERTIES, event.getProperties().get(EventKeysConfig.PROPERTIES));
    return values;
  }

  private String getTagWithDefaultMail(String maybeNullTag) {
    return maybeNullTag != null && !maybeNullTag.isBlank() ? maybeNullTag : "MAIL";
  }
}
