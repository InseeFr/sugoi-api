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

import fr.insee.sugoi.core.event.configuration.EventKeysConfig;
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
    boolean isEventError = false;
    SugoiEventTypeEnum eventType = cse.getEventType();
    String realm = cse.getRealm();
    Map<String, Object> properties = cse.getProperties();
    String userStorage = cse.getUserStorage() == null ? "default" : cse.getUserStorage();
    Map<String, Object> toLog = new HashMap<>();
    toLog.put(EventKeysConfig.TYPE, eventType.toString());
    if (properties != null && properties.get(EventKeysConfig.ERROR) != null) {
      isEventError = true;
      toLog.put(EventKeysConfig.ERROR, properties.get(EventKeysConfig.ERROR));
    }

    switch (eventType) {
      case CREATE_USER:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.USER_ID, ((User) properties.get(EventKeysConfig.USER)).getUsername());
        break;
      case UPDATE_USER:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.USER_ID, ((User) properties.get(EventKeysConfig.USER)).getUsername());
        break;
      case DELETE_USER:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(EventKeysConfig.USER_ID, (String) properties.get(EventKeysConfig.USER_ID));
        break;
      case FIND_USERS:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        Map<String, String> params = new HashMap<>();
        params.put(
            "username", ((User) properties.get(EventKeysConfig.USER_PROPERTIES)).getUsername());
        params.put(
            "commun_name",
            (String)
                ((User) properties.get(EventKeysConfig.USER_PROPERTIES))
                    .getAttributes()
                    .get("commmun_name"));
        params.put(
            "description",
            (String)
                ((User) properties.get(EventKeysConfig.USER_PROPERTIES))
                    .getAttributes()
                    .get("description"));
        toLog.put("params", params);
        break;
      case FIND_USER_BY_ID:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(EventKeysConfig.USER_ID, (String) properties.get(EventKeysConfig.USER_ID));
        break;
      case CREATE_ORGANIZATION:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.ORGANIZATION_ID,
            ((Organization) properties.get(EventKeysConfig.ORGANIZATION)).getIdentifiant());
        break;
      case UPDATE_ORGANIZATION:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.ORGANIZATION_ID,
            ((Organization) properties.get(EventKeysConfig.ORGANIZATION)).getIdentifiant());
        break;
      case DELETE_ORGANIZATION:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.ORGANIZATION_ID,
            (String) properties.get(EventKeysConfig.ORGANIZATION_ID));
        break;
      case FIND_ORGANIZATIONS:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            "params",
            ((Organization) properties.get(EventKeysConfig.ORGANIZATION_FILTER)).getIdentifiant());
        break;
      case FIND_ORGANIZATION_BY_ID:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.ORGANIZATION_ID,
            (String) properties.get(EventKeysConfig.ORGANIZATION_ID));
        break;
      case CREATE_HABILITATION:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.HABILITATION_ID,
            ((Habilitation) properties.get(EventKeysConfig.HABILITATION)).getId());
        break;
      case UPDATE_HABILITATION:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.HABILITATION_ID,
            ((Habilitation) properties.get(EventKeysConfig.HABILITATION)).getId());
        break;
      case DELETE_HABILITATION:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.HABILITATION_ID,
            (String) properties.get(EventKeysConfig.HABILITATION_ID));
        break;
      case FIND_HABILITATIONS:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        break;
      case FIND_HABILITATION_BY_ID:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.HABILITATION_ID,
            (String) properties.get(EventKeysConfig.HABILITATION_ID));
        break;
      case CREATE_REALM:
        toLog.put(
            EventKeysConfig.REALM_NAME, ((Realm) properties.get(EventKeysConfig.REALM)).getName());
        break;
      case UPDATE_REALM:
        toLog.put(
            EventKeysConfig.REALM_NAME, ((Realm) properties.get(EventKeysConfig.REALM)).getName());
        break;
      case DELETE_REALM:
        toLog.put(
            EventKeysConfig.REALM_NAME, ((Realm) properties.get(EventKeysConfig.REALM)).getName());
        break;
      case FIND_REALMS:
        break;
      case FIND_REALM_BY_ID:
        toLog.put(EventKeysConfig.REALM_NAME, (String) properties.get(EventKeysConfig.REALM_NAME));
        break;
      case CREATE_APPLICATION:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.APPLICATION_NAME,
            ((Application) properties.get(EventKeysConfig.APPLICATION)).getName());
        break;
      case UPDATE_APPLICATION:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.APPLICATION_NAME,
            ((Application) properties.get(EventKeysConfig.APPLICATION)).getName());
        break;
      case DELETE_APPLICATION:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.APPLICATION_NAME,
            (String) properties.get(EventKeysConfig.APPLICATION_ID));
        break;
      case FIND_APPLICATIONS:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            "params", ((Application) properties.get(EventKeysConfig.APPLICATION_FILTER)).getName());
        break;
      case FIND_APPLICATION_BY_ID:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.APPLICATION_NAME,
            ((String) properties.get(EventKeysConfig.APPLICATION_ID)));
        break;
      case CREATE_GROUP:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.GROUP_NAME, ((Group) properties.get(EventKeysConfig.GROUP)).getName());
        break;
      case UPDATE_GROUP:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.GROUP_NAME, ((Group) properties.get(EventKeysConfig.GROUP)).getName());
        break;
      case DELETE_GROUP:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(EventKeysConfig.GROUP_NAME, ((String) properties.get(EventKeysConfig.GROUP_ID)));
        break;
      case FIND_GROUPS:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        break;
      case FIND_GROUP_BY_ID:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(EventKeysConfig.GROUP_NAME, ((String) properties.get(EventKeysConfig.GROUP_ID)));
        break;
      case CHANGE_PASSWORD:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.USER_ID, ((User) properties.get(EventKeysConfig.USER)).getUsername());
        break;
      case INIT_PASSWORD:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.USER_ID,
            (String) ((User) properties.get(EventKeysConfig.USER)).getUsername());
        break;
      case RESET_PASSWORD:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.USER_ID,
            (String) ((User) properties.get(EventKeysConfig.USER)).getUsername());
        break;

      case CREATE_USER_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.USER_ID, ((User) properties.get(EventKeysConfig.USER)).getUsername());
        break;
      case UPDATE_USER_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.USER_ID, ((User) properties.get(EventKeysConfig.USER)).getUsername());
        break;
      case DELETE_USER_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(EventKeysConfig.USER_ID, (String) properties.get(EventKeysConfig.USER_ID));
        break;
      case FIND_USERS_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        params = new HashMap<>();
        params.put(
            "username", ((User) properties.get(EventKeysConfig.USER_PROPERTIES)).getUsername());
        params.put(
            "commun_name",
            (String)
                ((User) properties.get(EventKeysConfig.USER_PROPERTIES))
                    .getAttributes()
                    .get("commmun_name"));
        params.put(
            "description",
            (String)
                ((User) properties.get(EventKeysConfig.USER_PROPERTIES))
                    .getAttributes()
                    .get("description"));
        toLog.put("params", params);
        break;
      case FIND_USER_BY_ID_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(EventKeysConfig.USER_ID, (String) properties.get(EventKeysConfig.USER_ID));
        break;
      case CREATE_ORGANIZATION_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.ORGANIZATION_ID,
            ((Organization) properties.get(EventKeysConfig.ORGANIZATION)).getIdentifiant());
        break;
      case UPDATE_ORGANIZATION_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.ORGANIZATION_ID,
            ((Organization) properties.get(EventKeysConfig.ORGANIZATION)).getIdentifiant());
        break;
      case DELETE_ORGANIZATION_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.ORGANIZATION_ID,
            (String) properties.get(EventKeysConfig.ORGANIZATION_ID));
        break;
      case FIND_ORGANIZATIONS_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            "params",
            ((Organization) properties.get(EventKeysConfig.ORGANIZATION_FILTER)).getIdentifiant());
        break;
      case FIND_ORGANIZATION_BY_ID_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.ORGANIZATION_ID,
            (String) properties.get(EventKeysConfig.ORGANIZATION_ID));
        break;
      case CREATE_HABILITATION_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.HABILITATION_ID,
            ((Habilitation) properties.get(EventKeysConfig.HABILITATION)).getId());
        break;
      case UPDATE_HABILITATION_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.HABILITATION_ID,
            ((Habilitation) properties.get(EventKeysConfig.HABILITATION)).getId());
        break;
      case DELETE_HABILITATION_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.HABILITATION_ID,
            (String) properties.get(EventKeysConfig.HABILITATION_ID));
        break;
      case FIND_HABILITATIONS_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        break;
      case FIND_HABILITATION_BY_ID_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.HABILITATION_ID,
            (String) properties.get(EventKeysConfig.HABILITATION_ID));
        break;
      case CREATE_REALM_ERROR:
        toLog.put(
            EventKeysConfig.REALM_NAME, ((Realm) properties.get(EventKeysConfig.REALM)).getName());
        break;
      case UPDATE_REALM_ERROR:
        toLog.put(
            EventKeysConfig.REALM_NAME, ((Realm) properties.get(EventKeysConfig.REALM)).getName());
        break;
      case DELETE_REALM_ERROR:
        toLog.put(
            EventKeysConfig.REALM_NAME, ((Realm) properties.get(EventKeysConfig.REALM)).getName());
        break;
      case FIND_REALMS_ERROR:
        break;
      case FIND_REALM_BY_ID_ERROR:
        toLog.put(EventKeysConfig.REALM_NAME, (String) properties.get(EventKeysConfig.REALM_NAME));
        break;
      case CREATE_APPLICATION_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.APPLICATION_NAME,
            ((Application) properties.get(EventKeysConfig.APPLICATION)).getName());
        break;
      case UPDATE_APPLICATION_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.APPLICATION_NAME,
            ((Application) properties.get(EventKeysConfig.APPLICATION)).getName());
        break;
      case DELETE_APPLICATION_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.APPLICATION_NAME,
            (String) properties.get(EventKeysConfig.APPLICATION_ID));
        break;
      case FIND_APPLICATIONS_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            "params", ((Application) properties.get(EventKeysConfig.APPLICATION_FILTER)).getName());
        break;
      case FIND_APPLICATION_BY_ID_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.APPLICATION_NAME,
            ((String) properties.get(EventKeysConfig.APPLICATION_ID)));
        break;
      case CREATE_GROUP_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.GROUP_NAME, ((Group) properties.get(EventKeysConfig.GROUP)).getName());
        break;
      case UPDATE_GROUP_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(
            EventKeysConfig.GROUP_NAME, ((Group) properties.get(EventKeysConfig.GROUP)).getName());
        break;
      case DELETE_GROUP_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(EventKeysConfig.GROUP_NAME, ((String) properties.get(EventKeysConfig.GROUP_ID)));
        break;
      case FIND_GROUPS_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        break;
      case FIND_GROUP_BY_ID_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(EventKeysConfig.GROUP_NAME, ((String) properties.get(EventKeysConfig.GROUP_ID)));
        break;
      case CHANGE_PASSWORD_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(EventKeysConfig.USER_ID, properties.get(EventKeysConfig.USER_ID));
        break;
      case INIT_PASSWORD_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(EventKeysConfig.USER_ID, properties.get(EventKeysConfig.USER_ID));
        break;
      case RESET_PASSWORD_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(EventKeysConfig.USER_ID, properties.get(EventKeysConfig.USER_ID));
        break;
      case ADD_APP_MANAGED_ATTRIBUTES:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(EventKeysConfig.USER_ID, properties.get(EventKeysConfig.USER_ID));
        toLog.put(EventKeysConfig.ATTRIBUTE_KEY, properties.get(EventKeysConfig.ATTRIBUTE_KEY));
        toLog.put(EventKeysConfig.ATTRIBUTE_VALUE, properties.get(EventKeysConfig.ATTRIBUTE_VALUE));
      case ADD_APP_MANAGED_ATTRIBUTES_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(EventKeysConfig.USER_ID, properties.get(EventKeysConfig.USER_ID));
        toLog.put(EventKeysConfig.ATTRIBUTE_KEY, properties.get(EventKeysConfig.ATTRIBUTE_KEY));
        toLog.put(EventKeysConfig.ATTRIBUTE_VALUE, properties.get(EventKeysConfig.ATTRIBUTE_VALUE));
        toLog.put(EventKeysConfig.ERROR, properties.get(EventKeysConfig.ERROR));
      case DELETE_APP_MANAGED_ATTRIBUTES:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(EventKeysConfig.USER_ID, properties.get(EventKeysConfig.USER_ID));
        toLog.put(EventKeysConfig.ATTRIBUTE_KEY, properties.get(EventKeysConfig.ATTRIBUTE_KEY));
        toLog.put(EventKeysConfig.ATTRIBUTE_VALUE, properties.get(EventKeysConfig.ATTRIBUTE_VALUE));

      case DELETE_APP_MANAGED_ATTRIBUTES_ERROR:
        toLog.put(EventKeysConfig.REALM, realm);
        toLog.put(EventKeysConfig.USERSTORAGE, userStorage);
        toLog.put(EventKeysConfig.USER_ID, properties.get(EventKeysConfig.USER_ID));
        toLog.put(EventKeysConfig.ATTRIBUTE_KEY, properties.get(EventKeysConfig.ATTRIBUTE_KEY));
        toLog.put(EventKeysConfig.ATTRIBUTE_VALUE, properties.get(EventKeysConfig.ATTRIBUTE_VALUE));
      default:
        break;
    }
    if (!toLog.isEmpty()) {
      String log = "";
      for (String key : toLog.keySet()) {
        log += key + "=\"" + toLog.get(key) + "\" ";
      }
      if (isEventError) {
        logger.error(log);
      } else {
        logger.info(log);
      }
    }
  }
}
