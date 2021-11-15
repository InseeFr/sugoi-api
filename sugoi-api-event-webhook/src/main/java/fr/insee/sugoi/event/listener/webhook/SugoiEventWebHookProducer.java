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
    switch (eventType) {
      case RESET_PASSWORD:
        User user = (User) cse.getProperties().get(EventKeysConfig.USER);
        String password = (String) cse.getProperties().get(EventKeysConfig.PASSWORD);
        String realm = cse.getRealm();
        String userStorage = cse.getUserStorage() != null ? cse.getUserStorage() : "default";

        String webHookTag =
            cse.getProperties().get(EventKeysConfig.WEBSERVICE_TAG) != null
                ? (String) cse.getProperties().get(EventKeysConfig.WEBSERVICE_TAG)
                : "MAIL";
        Map<String, Object> values = new HashMap<>();
        values.put(EventKeysConfig.REALM, realm);
        values.put(EventKeysConfig.USERSTORAGE, userStorage);
        values.put(EventKeysConfig.USER, user);
        values.put(EventKeysConfig.MAIL, cse.getProperties().get(EventKeysConfig.MAIL));
        values.put(EventKeysConfig.PASSWORD, password);
        values.put(EventKeysConfig.PROPERTIES, cse.getProperties().get(EventKeysConfig.PROPERTIES));
        webHookNames.stream()
            .filter(
                webHookName ->
                    (((String) env.getProperty("sugoi.api.event.webhook." + webHookName + ".tag"))
                        .equalsIgnoreCase(webHookTag)))
            .forEach(webHookName -> webHookService.resetPassword(webHookName, values));
        break;
      case SEND_LOGIN:
        user = (User) cse.getProperties().get(EventKeysConfig.USER);
        webHookTag =
            cse.getProperties().get(EventKeysConfig.WEBSERVICE_TAG) != null
                ? (String) cse.getProperties().get(EventKeysConfig.WEBSERVICE_TAG)
                : "MAIL";
        realm = cse.getRealm();
        userStorage = cse.getUserStorage() != null ? cse.getUserStorage() : "default";
        values = new HashMap<>();
        values.put(EventKeysConfig.REALM, realm);
        values.put(EventKeysConfig.USERSTORAGE, userStorage);
        values.put(EventKeysConfig.USER, user);
        values.put(EventKeysConfig.USER_ID, user.getUsername());
        values.put(EventKeysConfig.MAIL, cse.getProperties().get(EventKeysConfig.MAIL));
        values.put(EventKeysConfig.PROPERTIES, cse.getProperties().get(EventKeysConfig.PROPERTIES));
        webHookNames.stream()
            .filter(
                webHookName ->
                    (((String) env.getProperty("sugoi.api.event.webhook." + webHookName + ".tag"))
                        .equalsIgnoreCase(webHookTag)))
            .forEach(webHookName -> webHookService.sendLogin(webHookName, values));
        break;
      default:
        break;
    }
  }
}
