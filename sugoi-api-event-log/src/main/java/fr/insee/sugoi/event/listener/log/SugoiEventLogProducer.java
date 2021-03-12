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
package fr.insee.sugoi.event.listener.log;

import fr.insee.sugoi.core.event.model.SugoiEvent;
import fr.insee.sugoi.core.event.model.SugoiEventTypeEnum;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Habilitation;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "sugoi.api.event.log.producer.enabled",
    havingValue = "true",
    matchIfMissing = false)
public class SugoiEventLogProducer {

  public static final Logger logger = LoggerFactory.getLogger(SugoiEventLogProducer.class);

  @EventListener
  public void handleContextStart(SugoiEvent cse) {
    SugoiEventTypeEnum eventType = cse.getEventType();
    String realm = cse.getRealm();
    Map<String, Object> properties = cse.getProperties();
    String userStorage = cse.getUserStorage() == null ? "default" : cse.getUserStorage();
    Map<String, Object> toLog = new HashMap<>();
    toLog.put("type", eventType.toString());
    switch (eventType) {
      case CREATE_USER:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("userId", ((User) properties.get("user")).getUsername());
        break;
      case UPDATE_USER:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("userId", ((User) properties.get("user")).getUsername());
        break;
      case DELETE_USER:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("userId", (String) properties.get("userId"));
        break;
      case FIND_USERS:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        Map<String, String> params = new HashMap<>();
        params.put("username", ((User) properties.get("userProperties")).getUsername());
        params.put(
            "commun_name",
            (String) ((User) properties.get("userProperties")).getAttributes().get("commmun_name"));
        params.put(
            "description",
            (String) ((User) properties.get("userProperties")).getAttributes().get("description"));
        toLog.put("params", params);
        break;
      case FIND_USER_BY_ID:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("userId", (String) properties.get("userId"));
        break;
      case CREATE_ORGANIZATION:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put(
            "organizationId", ((Organization) properties.get("organization")).getIdentifiant());
        break;
      case UPDATE_ORGANIZATION:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put(
            "organizationId", ((Organization) properties.get("organization")).getIdentifiant());
        break;
      case DELETE_ORGANIZATION:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("organizationId", (String) properties.get("organizationId"));
        break;
      case FIND_ORGANIZATIONS:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("params", ((Organization) properties.get("organizationFilter")).getIdentifiant());
        break;
      case FIND_ORGANIZATION_BY_ID:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("organizationId", (String) properties.get("organizationId"));
        break;
      case CREATE_HABILITATION:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("habilitationId", ((Habilitation) properties.get("habilitation")).getId());
        break;
      case UPDATE_HABILITATION:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("habilitationId", ((Habilitation) properties.get("habilitation")).getId());
        break;
      case DELETE_HABILITATION:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("habilitationId", (String) properties.get("habilitationId"));
        break;
      case FIND_HABILITATIONS:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        break;
      case FIND_HABILITATION_BY_ID:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("habilitationId", (String) properties.get("habilitationId"));
        break;
      case CREATE_REALM:
        toLog.put("realmName", ((Realm) properties.get("realm")).getName());
        break;
      case UPDATE_REALM:
        toLog.put("realmName", ((Realm) properties.get("realm")).getName());
        break;
      case DELETE_REALM:
        toLog.put("realmName", ((Realm) properties.get("realm")).getName());
        break;
      case FIND_REALMS:
        break;
      case FIND_REALM_BY_ID:
        toLog.put("realmName", (String) properties.get("realmName"));
        break;
      case CREATE_APPLICATION:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("applicationName", ((Application) properties.get("application")).getName());
        break;
      case UPDATE_APPLICATION:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("applicationName", ((Application) properties.get("application")).getName());
        break;
      case DELETE_APPLICATION:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("applicationName", (String) properties.get("applicationId"));
        break;
      case FIND_APPLICATIONS:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("params", ((Application) properties.get("applicationFilter")).getName());
        break;
      case FIND_APPLICATION_BY_ID:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("applicationName", ((String) properties.get("applicationId")));
        break;
      case CREATE_GROUP:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("groupName", ((Group) properties.get("group")).getName());
        break;
      case UPDATE_GROUP:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("groupName", ((Group) properties.get("group")).getName());
        break;
      case DELETE_GROUP:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("groupName", ((String) properties.get("groupId")));
        break;
      case FIND_GROUPS:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        break;
      case FIND_GROUP_BY_ID:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("groupName", ((String) properties.get("groupId")));
        break;
      case CHANGE_PASSWORD:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("userId", ((User) properties.get("user")).getUsername());
        break;
      case INIT_PASSWORD:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("userId", (String) ((User) properties.get("user")).getUsername());
        break;
      case RESET_PASSWORD:
        toLog.put("realm", realm);
        toLog.put("userStorage", userStorage);
        toLog.put("userId", (String) ((User) properties.get("user")).getUsername());
        break;
      default:
        break;
    }
    if (!toLog.isEmpty()) {
      String log = "";
      for (String key : toLog.keySet()) {
        log += key + "=\"" + toLog.get(key) + "\" ";
      }
      logger.info(log);
    }
  }
}
