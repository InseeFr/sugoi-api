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
import fr.insee.sugoi.jms.utils.JmsAtttributes;
import fr.insee.sugoi.jms.utils.Method;
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
      case Method.DELETE_USER:
        String id = (String) request.getmethodParams().get(JmsAtttributes.USER_ID);
        storeProvider.getWriterStore(realm, userStorage).deleteUser(id);
        break;
      case Method.CREATE_USER:
        User user = converter.toUser(request.getmethodParams().get(JmsAtttributes.USER));
        storeProvider.getWriterStore(realm, userStorage).createUser(user);
        break;
      case Method.UPDATE_USER:
        User updatedUser = converter.toUser(request.getmethodParams().get(JmsAtttributes.USER));
        storeProvider.getWriterStore(realm, userStorage).updateUser(updatedUser);
        break;
      case Method.DELETE_GROUP:
        String appName = (String) request.getmethodParams().get(JmsAtttributes.APP_NAME);
        String groupName = (String) request.getmethodParams().get(JmsAtttributes.GROUP_NAME);
        storeProvider.getWriterStore(realm, userStorage).deleteGroup(appName, groupName);
        break;
      case Method.CREATE_GROUP:
        appName = (String) request.getmethodParams().get(JmsAtttributes.APP_NAME);
        Group group = converter.toGroup(request.getmethodParams().get(JmsAtttributes.GROUP));
        storeProvider.getWriterStore(realm, userStorage).createGroup(appName, group);
        break;
      case Method.UPDATE_GROUP:
        appName = (String) request.getmethodParams().get(JmsAtttributes.APP_NAME);
        Group updatedGroup = converter.toGroup(request.getmethodParams().get(JmsAtttributes.GROUP));
        storeProvider.getWriterStore(realm, userStorage).createGroup(appName, updatedGroup);
        break;
      case Method.DELETE_ORGANIZATION:
        String name = (String) request.getmethodParams().get(JmsAtttributes.ORGANIZATION_NAME);
        storeProvider.getWriterStore(realm, userStorage).deleteOrganization(name);
        break;
      case Method.CREATE_ORGANIZATION:
        Organization organization =
            converter.toOrganization(request.getmethodParams().get(JmsAtttributes.ORGANIZATION));
        storeProvider.getWriterStore(realm, userStorage).createOrganization(organization);
        break;
      case Method.UPDATE_ORGANIZATION:
        Organization updatedOrganization =
            converter.toOrganization(request.getmethodParams().get(JmsAtttributes.ORGANIZATION));
        storeProvider.getWriterStore(realm, userStorage).updateOrganization(updatedOrganization);
        break;
      case Method.DELETE_USER_FROM_GROUP:
        appName = (String) request.getmethodParams().get(JmsAtttributes.APP_NAME);
        groupName = (String) request.getmethodParams().get(JmsAtttributes.GROUP_NAME);
        String userId = (String) request.getmethodParams().get(JmsAtttributes.USER_ID);
        storeProvider
            .getWriterStore(realm, userStorage)
            .deleteUserFromGroup(appName, groupName, userId);
        break;
      case Method.ADD_USER_TO_GROUP:
        appName = (String) request.getmethodParams().get(JmsAtttributes.APP_NAME);
        groupName = (String) request.getmethodParams().get(JmsAtttributes.GROUP_NAME);
        userId = (String) request.getmethodParams().get(JmsAtttributes.USER_ID);
        storeProvider.getWriterStore(realm, userStorage).addUserToGroup(appName, groupName, userId);
        break;
      case Method.REINIT_PASSWORD:
        user = converter.toUser(request.getmethodParams().get(JmsAtttributes.USER));
        String password = (String) request.getmethodParams().get(JmsAtttributes.PASSWORD);
        storeProvider.getWriterStore(realm, userStorage).reinitPassword(user, password);
        break;
      case Method.INIT_PASSWORD:
        user = converter.toUser(request.getmethodParams().get(JmsAtttributes.USER));
        password = (String) request.getmethodParams().get(JmsAtttributes.PASSWORD);
        storeProvider.getWriterStore(realm, userStorage).initPassword(user, password);
        break;
      case Method.CHANGE_PASSWORD_RESET_STATUS:
        user = converter.toUser(request.getmethodParams().get(JmsAtttributes.USER));
        Boolean isReset = (Boolean) request.getmethodParams().get(JmsAtttributes.IS_RESET);
        storeProvider.getWriterStore(realm, userStorage).changePasswordResetStatus(user, isReset);
        break;
      case Method.CREATE_APPLICATION:
        Application application =
            converter.toApplication(request.getmethodParams().get(JmsAtttributes.APPLICATION));
        storeProvider.getWriterStore(realm, userStorage).createApplication(application);
        break;
      case Method.UPDATE_APPLICATION:
        Application updatedApplication =
            converter.toApplication(request.getmethodParams().get(JmsAtttributes.APPLICATION));
        storeProvider.getWriterStore(realm, userStorage).updateApplication(updatedApplication);
        break;
      case Method.DELETE_APPLICATION:
        String applicationName = (String) request.getmethodParams().get(JmsAtttributes.APP_NAME);
        storeProvider.getWriterStore(realm, userStorage).deleteApplication(applicationName);
        break;
      case Method.CHANGE_PASSWORD:
        String oldPassword = (String) request.getmethodParams().get(JmsAtttributes.OLD_PASSWORD);
        String newPassword = (String) request.getmethodParams().get(JmsAtttributes.NEW_PASSWORD);
        user = converter.toUser(request.getmethodParams().get(JmsAtttributes.USER));
        storeProvider
            .getWriterStore(realm, userStorage)
            .changePassword(user, oldPassword, newPassword);
      default:
        throw new Exception("Invalid Operation");
    }
  }
}
