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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.jms.model.BrokerRequest;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.UserStorage;

@Service
public class JmsRequestRouter {

  private static final Logger logger = LogManager.getLogger(JmsReceiverRequest.class);

  @Autowired
  private StoreProvider storeProvider;

  public void exec(BrokerRequest request) throws Exception {
    Realm realm = (Realm) request.getmethodParams().get("realm");
    UserStorage userStorage = (UserStorage) request.getmethodParams().get("userStorage");
    String operation = request.getMethod();
    logger.info("Receive request from Broker for realm {} and userStorage {} operation:{}", realm.getName(),
        userStorage.getName(), operation);
    switch (operation) {
      case "deleteUser":
        String id = (String) request.getmethodParams().get("id");
        storeProvider.getWriterStore(realm.getName(), userStorage.getName()).deleteUser(id);
        break;
      case "createUser":
        User user = (User) request.getmethodParams().get("user");
        storeProvider.getWriterStore(realm.getName(), userStorage.getName()).createUser(user);
        break;
      case "updateUser":
        User updatedUser = (User) request.getmethodParams().get("updatedUser");
        storeProvider.getWriterStore(realm.getName(), userStorage.getName()).updateUser(updatedUser);
        break;
      case "deleteGroup":
        String appName = (String) request.getmethodParams().get("appName");
        String groupName = (String) request.getmethodParams().get("groupName");
        storeProvider.getWriterStore(realm.getName(), userStorage.getName()).deleteGroup(appName, groupName);
        break;
      case "createGroup":
        appName = (String) request.getmethodParams().get("appName");
        Group group = (Group) request.getmethodParams().get("group");
        storeProvider.getWriterStore(realm.getName(), userStorage.getName()).createGroup(appName, group);
        break;
      case "updateGroup":
        appName = (String) request.getmethodParams().get("appName");
        Group updatedGroup = (Group) request.getmethodParams().get("updatedGroup");
        storeProvider.getWriterStore(realm.getName(), userStorage.getName()).createGroup(appName, updatedGroup);
        break;
      case "deleteOrganization":
        String name = (String) request.getmethodParams().get("name");
        storeProvider.getWriterStore(realm.getName(), userStorage.getName()).deleteOrganization(name);
        break;
      case "createOrganization":
        Organization organization = (Organization) request.getmethodParams().get("organization");
        storeProvider.getWriterStore(realm.getName(), userStorage.getName()).createOrganization(organization);
        break;
      case "updateOrganization":
        Organization updatedOrganization = (Organization) request.getmethodParams().get("updatedOrganization");
        storeProvider.getWriterStore(realm.getName(), userStorage.getName()).updateOrganization(updatedOrganization);
        break;
      case "deleteUserFromGroup":
        appName = (String) request.getmethodParams().get("appName");
        groupName = (String) request.getmethodParams().get("groupName");
        String userId = (String) request.getmethodParams().get("userId");
        storeProvider.getWriterStore(realm.getName(), userStorage.getName()).deleteUserFromGroup(appName, groupName,
            userId);
        break;
      case "addUserToGroup":
        appName = (String) request.getmethodParams().get("appName");
        groupName = (String) request.getmethodParams().get("groupName");
        userId = (String) request.getmethodParams().get("userId");
        storeProvider.getWriterStore(realm.getName(), userStorage.getName()).addUserToGroup(appName, groupName, userId);
        break;
      case "reinitPassword":
        user = (User) request.getmethodParams().get("user");
        storeProvider.getWriterStore(realm.getName(), userStorage.getName()).reinitPassword(user);
        break;
      case "initPassword":
        user = (User) request.getmethodParams().get("user");
        String password = (String) request.getmethodParams().get("password");
        storeProvider.getWriterStore(realm.getName(), userStorage.getName()).initPassword(user, password);
        break;
      case "changePasswordResetStatus":
        user = (User) request.getmethodParams().get("user");
        Boolean isReset = (Boolean) request.getmethodParams().get("isReset");
        storeProvider.getWriterStore(realm.getName(), userStorage.getName()).changePasswordResetStatus(user, isReset);
        break;
      case "createApplication":
        Application application = (Application) request.getmethodParams().get("application");
        storeProvider.getWriterStore(realm.getName(), userStorage.getName()).createApplication(application);
        break;
      case "updateApplication":
        Application updatedApplication = (Application) request.getmethodParams().get("updatedApplication");
        storeProvider.getWriterStore(realm.getName(), userStorage.getName()).updateApplication(updatedApplication);
        break;
      case "deleteApplication":
        String applicationName = (String) request.getmethodParams().get("applicationName");
        storeProvider.getWriterStore(realm.getName(), userStorage.getName()).deleteApplication(applicationName);
        break;
      default:
        throw new Exception("Invalid Operation");
    }
  }
}
