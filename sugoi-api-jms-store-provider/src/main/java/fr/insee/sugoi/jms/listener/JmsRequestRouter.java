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
package fr.insee.sugoi.jms.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.jms.model.BrokerRequest;
import fr.insee.sugoi.jms.utils.Converter;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JmsRequestRouter {

  private static final Logger logger = LogManager.getLogger(JmsReceiverRequest.class);

  @Autowired private StoreProvider storeProvider;

  @Autowired private Converter converter;

  public void exec(BrokerRequest request) throws Exception {
    String realm = (String) request.getmethodParams().get("realm");
    String userStorage = (String) request.getmethodParams().get("userStorage");
    String operation = request.getMethod();
    ObjectMapper mapper = new ObjectMapper();
    logger.info(
        "Receive request from Broker for realm {} and userStorage {} operation:{}",
        realm,
        userStorage,
        operation);
    switch (operation) {
      case "deleteUser":
        String id = (String) request.getmethodParams().get("id");
        storeProvider.getWriterStore(realm, userStorage).deleteUser(id);
        break;
      case "createUser":
        User user = converter.toUser(request.getmethodParams().get("user"));
        storeProvider.getWriterStore(realm, userStorage).createUser(user);
        break;
      case "updateUser":
        User updatedUser = converter.toUser(request.getmethodParams().get("updatedUser"));
        storeProvider.getWriterStore(realm, userStorage).updateUser(updatedUser);
        break;
      case "deleteGroup":
        String appName = (String) request.getmethodParams().get("appName");
        String groupName = (String) request.getmethodParams().get("groupName");
        storeProvider.getWriterStore(realm, userStorage).deleteGroup(appName, groupName);
        break;
      case "createGroup":
        appName = (String) request.getmethodParams().get("appName");
        Group group = converter.toGroup(request.getmethodParams().get("group"));
        storeProvider.getWriterStore(realm, userStorage).createGroup(appName, group);
        break;
      case "updateGroup":
        appName = (String) request.getmethodParams().get("appName");
        Group updatedGroup = converter.toGroup(request.getmethodParams().get("updatedGroup"));
        storeProvider.getWriterStore(realm, userStorage).createGroup(appName, updatedGroup);
        break;
      case "deleteOrganization":
        String name = (String) request.getmethodParams().get("name");
        storeProvider.getWriterStore(realm, userStorage).deleteOrganization(name);
        break;
      case "createOrganization":
        Organization organization =
            converter.toOrganization(request.getmethodParams().get("organization"));
        storeProvider.getWriterStore(realm, userStorage).createOrganization(organization);
        break;
      case "updateOrganization":
        Organization updatedOrganization =
            converter.toOrganization(request.getmethodParams().get("updatedOrganization"));
        storeProvider.getWriterStore(realm, userStorage).updateOrganization(updatedOrganization);
        break;
      case "deleteUserFromGroup":
        appName = (String) request.getmethodParams().get("appName");
        groupName = (String) request.getmethodParams().get("groupName");
        String userId = (String) request.getmethodParams().get("userId");
        storeProvider
            .getWriterStore(realm, userStorage)
            .deleteUserFromGroup(appName, groupName, userId);
        break;
      case "addUserToGroup":
        appName = (String) request.getmethodParams().get("appName");
        groupName = (String) request.getmethodParams().get("groupName");
        userId = (String) request.getmethodParams().get("userId");
        storeProvider.getWriterStore(realm, userStorage).addUserToGroup(appName, groupName, userId);
        break;
      case "reinitPassword":
        user = converter.toUser(request.getmethodParams().get("user"));
        storeProvider.getWriterStore(realm, userStorage).reinitPassword(user);
        break;
      case "initPassword":
        user = converter.toUser(request.getmethodParams().get("user"));
        String password = (String) request.getmethodParams().get("password");
        storeProvider.getWriterStore(realm, userStorage).initPassword(user, password);
        break;
      case "changePasswordResetStatus":
        user = converter.toUser(request.getmethodParams().get("user"));
        Boolean isReset = (Boolean) request.getmethodParams().get("isReset");
        storeProvider.getWriterStore(realm, userStorage).changePasswordResetStatus(user, isReset);
        break;
      case "createApplication":
        Application application =
            converter.toApplication(request.getmethodParams().get("application"));
        storeProvider.getWriterStore(realm, userStorage).createApplication(application);
        break;
      case "updateApplication":
        Application updatedApplication =
            converter.toApplication(request.getmethodParams().get("updatedApplication"));
        storeProvider.getWriterStore(realm, userStorage).updateApplication(updatedApplication);
        break;
      case "deleteApplication":
        String applicationName = (String) request.getmethodParams().get("applicationName");
        storeProvider.getWriterStore(realm, userStorage).deleteApplication(applicationName);
        break;
      default:
        throw new Exception("Invalid Operation");
    }
  }
}
