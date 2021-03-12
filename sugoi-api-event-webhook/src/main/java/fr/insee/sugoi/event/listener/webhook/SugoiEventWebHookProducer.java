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

import fr.insee.sugoi.core.event.model.SugoiEvent;
import fr.insee.sugoi.core.event.model.SugoiEventTypeEnum;
import fr.insee.sugoi.core.model.PasswordChangeRequest;
import fr.insee.sugoi.core.model.SendMode;
import fr.insee.sugoi.event.listener.webhook.service.WebHookService;
import fr.insee.sugoi.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "sugoi.api.event.webhook.enabled", havingValue = "true")
public class SugoiEventWebHookProducer {

  @Value("${sugoi.api.event.webhook.name}")
  private List<String> webHookNames;

  @Autowired private Environment env;

  @Autowired private WebHookService webHookService;

  @EventListener
  @SuppressWarnings("unchecked")
  public void handleContextStart(SugoiEvent cse) {
    SugoiEventTypeEnum eventType = cse.getEventType();
    switch (eventType) {
      case INIT_PASSWORD:
        List<SendMode> sendModes = (List<SendMode>) cse.getProperties().get("sendModes");
        User user = (User) cse.getProperties().get("user");
        String password = (String) cse.getProperties().get("password");
        PasswordChangeRequest pcr = (PasswordChangeRequest) cse.getProperties().get("pcr");
        String realm = cse.getRealm();
        String userStorage = cse.getUserStorage() != null ? cse.getUserStorage() : "default";
        Map<String, Object> values = new HashMap<>();
        values.put("realm", realm);
        values.put("userStorage", userStorage);
        values.put("user", user);
        values.put("mail", pcr.getEmail() != null ? pcr.getEmail() : user.getMail());
        values.put("password", password);
        values.put("address", pcr.getAddress());
        values.put(
            "properties",
            pcr.getProperties() != null ? pcr.getProperties() : new HashMap<String, String>());
        webHookNames.stream()
            .filter(
                webHookName ->
                    sendModes.stream()
                        .map(sendMode -> sendMode.getSendMode().toUpperCase())
                        .collect(Collectors.toList())
                        .contains(
                            env.getProperty("sugoi.api.event.webhook." + webHookName + ".tag")
                                .toUpperCase()))
            .forEach(webHookName -> webHookService.initPassword(webHookName, values));
        break;
      case RESET_PASSWORD:
        sendModes = (List<SendMode>) cse.getProperties().get("sendModes");
        user = (User) cse.getProperties().get("user");
        password = (String) cse.getProperties().get("password");
        pcr = (PasswordChangeRequest) cse.getProperties().get("pcr");
        realm = cse.getRealm();
        userStorage = cse.getUserStorage() != null ? cse.getUserStorage() : "default";
        values = new HashMap<>();
        values.put("realm", realm);
        values.put("userStorage", userStorage);
        values.put("user", user);
        values.put("mail", pcr.getEmail() != null ? pcr.getEmail() : user.getMail());
        values.put("password", password);
        values.put("address", pcr.getAddress());
        values.put(
            "properties",
            pcr.getProperties() != null ? pcr.getProperties() : new HashMap<String, String>());
        webHookNames.stream()
            .filter(
                webHookName ->
                    sendModes.stream()
                        .map(sendMode -> sendMode.getSendMode().toUpperCase())
                        .collect(Collectors.toList())
                        .contains(
                            env.getProperty("sugoi.api.event.webhook." + webHookName + ".tag")
                                .toUpperCase()))
            .forEach(webHookName -> webHookService.resetPassword(webHookName, values));
        break;
      default:
        break;
    }
  }
}
