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

import fr.insee.sugoi.core.service.CredentialsService;
import fr.insee.sugoi.core.service.GroupService;
import fr.insee.sugoi.core.service.OrganizationService;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.jms.model.BrokerRequest;
import fr.insee.sugoi.jms.utils.Converter;
import fr.insee.sugoi.jms.utils.JmsAtttributes;
import fr.insee.sugoi.jms.utils.Method;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.paging.PasswordChangeRequest;
import fr.insee.sugoi.model.paging.SendMode;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JmsRequestRouter {

  private static final Logger logger = LogManager.getLogger(JmsReceiverRequest.class);

  @Autowired private StoreProvider storeProvider;

  @Autowired private Converter converter;

  @Autowired private UserService userService;

  @Autowired private CredentialsService credentialsService;

  @Autowired private GroupService groupService;

  @Autowired private OrganizationService orgService;

  public void exec(BrokerRequest request) throws Exception {
    String realm = (String) request.getmethodParams().get("realm");
    String userStorage = (String) request.getmethodParams().get("userStorage");
    String operation = request.getMethod();
    logger.info(
        "Receive request from Broker for realm {} and userStorage {} operation:{}",
        realm,
        userStorage,
        operation);
    switch (operation) {
      case Method.DELETE_USER:
        String id = (String) request.getmethodParams().get(JmsAtttributes.USER_ID);
        userService.delete(realm, userStorage, id);
        break;
      case Method.CREATE_USER:
        User user = converter.toUser(request.getmethodParams().get(JmsAtttributes.USER));
        userService.create(realm, userStorage, user);
        break;
      case Method.UPDATE_USER:
        User updatedUser = converter.toUser(request.getmethodParams().get(JmsAtttributes.USER));
        userService.update(realm, userStorage, updatedUser);
        break;
      case Method.DELETE_GROUP:
        String appName = (String) request.getmethodParams().get(JmsAtttributes.APP_NAME);
        String groupName = (String) request.getmethodParams().get(JmsAtttributes.GROUP_NAME);
        groupService.delete(realm, appName, groupName);
        break;
      case Method.CREATE_GROUP:
        appName = (String) request.getmethodParams().get(JmsAtttributes.APP_NAME);
        Group group = converter.toGroup(request.getmethodParams().get(JmsAtttributes.GROUP));
        groupService.create(realm, appName, group);
        break;
      case Method.UPDATE_GROUP:
        appName = (String) request.getmethodParams().get(JmsAtttributes.APP_NAME);
        Group updatedGroup = converter.toGroup(request.getmethodParams().get(JmsAtttributes.GROUP));
        groupService.update(realm, appName, updatedGroup);
        break;
      case Method.DELETE_ORGANIZATION:
        String name = (String) request.getmethodParams().get(JmsAtttributes.ORGANIZATION_NAME);
        orgService.delete(realm, userStorage, name);
        break;
      case Method.CREATE_ORGANIZATION:
        Organization organization =
            converter.toOrganization(request.getmethodParams().get(JmsAtttributes.ORGANIZATION));
        orgService.create(realm, userStorage, organization);
        break;
      case Method.UPDATE_ORGANIZATION:
        Organization updatedOrganization =
            converter.toOrganization(request.getmethodParams().get(JmsAtttributes.ORGANIZATION));
        orgService.update(realm, userStorage, updatedOrganization);
        break;
      case Method.DELETE_USER_FROM_GROUP:
        appName = (String) request.getmethodParams().get(JmsAtttributes.APP_NAME);
        groupName = (String) request.getmethodParams().get(JmsAtttributes.GROUP_NAME);
        String userId = (String) request.getmethodParams().get(JmsAtttributes.USER_ID);
        groupService.deleteUserFromGroup(realm, userId, appName, groupName);
        break;
      case Method.ADD_USER_TO_GROUP:
        appName = (String) request.getmethodParams().get(JmsAtttributes.APP_NAME);
        groupName = (String) request.getmethodParams().get(JmsAtttributes.GROUP_NAME);
        userId = (String) request.getmethodParams().get(JmsAtttributes.USER_ID);
        groupService.addUserToGroup(realm, userId, appName, groupName);
        break;
      case Method.REINIT_PASSWORD:
        user = converter.toUser(request.getmethodParams().get(JmsAtttributes.USER));
        PasswordChangeRequest pcr =
            converter.toPasswordChangeRequest(
                request.getmethodParams().get(JmsAtttributes.PASSWORD_CHANGE_REQUEST));
        List<SendMode> sendMode =
            converter.toSendModeList(request.getmethodParams().get(JmsAtttributes.SEND_MODE));
        credentialsService.reinitPassword(realm, userStorage, user.getUsername(), pcr, sendMode);
        break;
      case Method.INIT_PASSWORD:
        user = converter.toUser(request.getmethodParams().get(JmsAtttributes.USER));
        pcr =
            converter.toPasswordChangeRequest(
                request.getmethodParams().get(JmsAtttributes.PASSWORD_CHANGE_REQUEST));
        sendMode =
            converter.toSendModeList(request.getmethodParams().get(JmsAtttributes.SEND_MODE));
        credentialsService.initPassword(realm, userStorage, user.getUsername(), pcr, sendMode);
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
        user = converter.toUser(request.getmethodParams().get(JmsAtttributes.USER));
        pcr =
            converter.toPasswordChangeRequest(
                request.getmethodParams().get(JmsAtttributes.PASSWORD_CHANGE_REQUEST));
        credentialsService.changePassword(realm, userStorage, user.getUsername(), pcr);
        break;
      case Method.ADD_APP_MANAGED_ATTRIBUTE:
        userService.addAppManagedAttribute(
            realm,
            userStorage,
            (String) request.getmethodParams().get(JmsAtttributes.USER_ID),
            (String) request.getmethodParams().get(JmsAtttributes.ATTRIBUTE_KEY),
            (String) request.getmethodParams().get(JmsAtttributes.ATTRIBUTE_VALUE));

      case Method.DELETE_APP_MANAGED_ATTRIBUTE:
        userService.deleteAppManagedAttribute(
            realm,
            userStorage,
            (String) request.getmethodParams().get(JmsAtttributes.USER_ID),
            (String) request.getmethodParams().get(JmsAtttributes.ATTRIBUTE_KEY),
            (String) request.getmethodParams().get(JmsAtttributes.ATTRIBUTE_VALUE));
      default:
        throw new Exception("Invalid Operation");
    }
  }
}
