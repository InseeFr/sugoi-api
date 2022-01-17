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

import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.model.ProviderResponse.ProviderResponseStatus;
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
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JmsRequestRouter {

  private static final Logger logger = LoggerFactory.getLogger(JmsRequestRouter.class);

  @Autowired private StoreProvider storeProvider;

  @Autowired private Converter converter;

  @Autowired private UserService userService;

  @Autowired private CredentialsService credentialsService;

  @Autowired private GroupService groupService;

  @Autowired private OrganizationService orgService;

  public ProviderResponse exec(BrokerRequest request) throws Exception {
    ProviderResponse response = new ProviderResponse();
    try {
      String realm = (String) request.getmethodParams().get("realm");
      String userStorage = (String) request.getmethodParams().get("userStorage");
      ProviderRequest providerRequest =
          converter.toProviderRequest(
              request.getmethodParams().get(JmsAtttributes.PROVIDER_REQUEST));
      String operation = request.getMethod();
      logger.info(
          "Receive request from Broker for realm {} and userStorage {} operation:{}",
          realm,
          userStorage,
          operation);
      switch (operation) {
        case Method.DELETE_USER:
          String id = (String) request.getmethodParams().get(JmsAtttributes.USER_ID);
          userService.delete(realm, userStorage, id, providerRequest);
          break;
        case Method.CREATE_USER:
          User user = converter.toUser(request.getmethodParams().get(JmsAtttributes.USER));
          response.setEntityId(
              userService.create(realm, userStorage, user, providerRequest).getEntityId());
          break;
        case Method.UPDATE_USER:
          User updatedUser = converter.toUser(request.getmethodParams().get(JmsAtttributes.USER));
          userService.update(realm, userStorage, updatedUser, providerRequest);
          break;
        case Method.DELETE_GROUP:
          String appName = (String) request.getmethodParams().get(JmsAtttributes.APP_NAME);
          String groupName = (String) request.getmethodParams().get(JmsAtttributes.GROUP_NAME);
          groupService.delete(realm, appName, groupName, providerRequest);
          break;
        case Method.CREATE_GROUP:
          appName = (String) request.getmethodParams().get(JmsAtttributes.APP_NAME);
          Group group = converter.toGroup(request.getmethodParams().get(JmsAtttributes.GROUP));
          groupService.create(realm, appName, group, providerRequest);
          break;
        case Method.UPDATE_GROUP:
          appName = (String) request.getmethodParams().get(JmsAtttributes.APP_NAME);
          Group updatedGroup =
              converter.toGroup(request.getmethodParams().get(JmsAtttributes.GROUP));
          groupService.update(realm, appName, updatedGroup, providerRequest);
          break;
        case Method.DELETE_ORGANIZATION:
          String name = (String) request.getmethodParams().get(JmsAtttributes.ORGANIZATION_NAME);
          orgService.delete(realm, userStorage, name, providerRequest);
          break;
        case Method.CREATE_ORGANIZATION:
          Organization organization =
              converter.toOrganization(request.getmethodParams().get(JmsAtttributes.ORGANIZATION));
          orgService.create(realm, userStorage, organization, providerRequest);
          break;
        case Method.UPDATE_ORGANIZATION:
          Organization updatedOrganization =
              converter.toOrganization(request.getmethodParams().get(JmsAtttributes.ORGANIZATION));
          orgService.update(realm, userStorage, updatedOrganization, providerRequest);
          break;
        case Method.DELETE_USER_FROM_GROUP:
          appName = (String) request.getmethodParams().get(JmsAtttributes.APP_NAME);
          groupName = (String) request.getmethodParams().get(JmsAtttributes.GROUP_NAME);
          String userId = (String) request.getmethodParams().get(JmsAtttributes.USER_ID);
          groupService.deleteUserFromGroup(
              realm, userStorage, userId, appName, groupName, providerRequest);
          break;
        case Method.ADD_USER_TO_GROUP:
          appName = (String) request.getmethodParams().get(JmsAtttributes.APP_NAME);
          groupName = (String) request.getmethodParams().get(JmsAtttributes.GROUP_NAME);
          userId = (String) request.getmethodParams().get(JmsAtttributes.USER_ID);
          groupService.addUserToGroup(
              realm, userStorage, userId, appName, groupName, providerRequest);
          break;
        case Method.REINIT_PASSWORD:
          userId = (String) request.getmethodParams().get(JmsAtttributes.USER_ID);
          boolean pwdChangeRequest =
              ((Boolean) request.getmethodParams().get(JmsAtttributes.SHOULD_RESET_PASSWORD));
          Map<String, String> templateProperties =
              converter.toMapStringString(
                  request.getmethodParams().get(JmsAtttributes.TEMPLATE_PROPERTIES));
          String webhookTag = (String) request.getmethodParams().get(JmsAtttributes.WEBHOOK_TAG);
          credentialsService.reinitPassword(
              realm,
              userStorage,
              userId,
              templateProperties,
              webhookTag,
              pwdChangeRequest,
              providerRequest);
          break;
        case Method.INIT_PASSWORD:
          userId = (String) request.getmethodParams().get(JmsAtttributes.USER_ID);
          String password = (String) request.getmethodParams().get(JmsAtttributes.PASSWORD);
          boolean changePasswordResetStatus =
              (Boolean) request.getmethodParams().get(JmsAtttributes.SHOULD_RESET_PASSWORD);
          credentialsService.initPassword(
              realm, userStorage, userId, password, changePasswordResetStatus, providerRequest);
          break;
        case Method.CREATE_APPLICATION:
          Application application =
              converter.toApplication(request.getmethodParams().get(JmsAtttributes.APPLICATION));
          storeProvider
              .getWriterStore(realm, userStorage)
              .createApplication(application, providerRequest);
          break;
        case Method.UPDATE_APPLICATION:
          Application updatedApplication =
              converter.toApplication(request.getmethodParams().get(JmsAtttributes.APPLICATION));
          storeProvider
              .getWriterStore(realm, userStorage)
              .updateApplication(updatedApplication, providerRequest);
          break;
        case Method.DELETE_APPLICATION:
          String applicationName = (String) request.getmethodParams().get(JmsAtttributes.APP_NAME);
          storeProvider
              .getWriterStore(realm, userStorage)
              .deleteApplication(applicationName, providerRequest);
          break;
        case Method.CHANGE_PASSWORD:
          userId = (String) request.getmethodParams().get(JmsAtttributes.USER_ID);
          credentialsService.changePassword(
              realm,
              userStorage,
              userId,
              (String) request.getmethodParams().get(JmsAtttributes.OLD_PASSWORD),
              (String) request.getmethodParams().get(JmsAtttributes.NEW_PASSWORD),
              (String) request.getmethodParams().get(JmsAtttributes.WEBHOOK_TAG),
              converter.toMapStringString(
                  request.getmethodParams().get(JmsAtttributes.TEMPLATE_PROPERTIES)),
              providerRequest);
          break;
        case Method.ADD_APP_MANAGED_ATTRIBUTE:
          userService.addAppManagedAttribute(
              realm,
              userStorage,
              (String) request.getmethodParams().get(JmsAtttributes.USER_ID),
              (String) request.getmethodParams().get(JmsAtttributes.ATTRIBUTE_KEY),
              (String) request.getmethodParams().get(JmsAtttributes.ATTRIBUTE_VALUE),
              providerRequest);
          break;
        case Method.DELETE_APP_MANAGED_ATTRIBUTE:
          userService.deleteAppManagedAttribute(
              realm,
              userStorage,
              (String) request.getmethodParams().get(JmsAtttributes.USER_ID),
              (String) request.getmethodParams().get(JmsAtttributes.ATTRIBUTE_KEY),
              (String) request.getmethodParams().get(JmsAtttributes.ATTRIBUTE_VALUE),
              providerRequest);
          break;
        case Method.DELETE_CERTIFICATE:
          userService.deleteCertificate(
              realm,
              userStorage,
              (String) request.getmethodParams().get(JmsAtttributes.USER_ID),
              providerRequest);
          break;
        case Method.UPDATE_CERTIFICATE:
          String cert = (String) request.getmethodParams().get(JmsAtttributes.CERTIFICATE);
          byte encodedCert[] = Base64.getDecoder().decode(cert);
          ByteArrayInputStream inputStream = new ByteArrayInputStream(encodedCert);
          CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
          X509Certificate certificate =
              (X509Certificate) certFactory.generateCertificate(inputStream);
          userService.updateCertificate(
              realm,
              userStorage,
              (String) request.getmethodParams().get(JmsAtttributes.USER_ID),
              certificate.getEncoded(),
              providerRequest);
          break;
        case Method.UPDATE_GPG_KEY:
          orgService.updateGpgKey(
              realm,
              userStorage,
              (String) request.getmethodParams().get(JmsAtttributes.ORGANIZATION_NAME),
              ((String) request.getmethodParams().get(JmsAtttributes.GPG_KEY)).getBytes(),
              providerRequest);
          break;
        case Method.DELETE_GPG_KEY:
          orgService.deleteGpgKey(
              realm,
              userStorage,
              (String) request.getmethodParams().get(JmsAtttributes.ORGANIZATION_NAME),
              providerRequest);
          break;
        case Method.DELETE_USER_FROM_MANAGER_GROUP:
          appName = (String) request.getmethodParams().get(JmsAtttributes.APP_NAME);
          userId = (String) request.getmethodParams().get(JmsAtttributes.USER_ID);
          groupService.deleteUserFromManagerGroup(
              realm, userStorage, userId, appName, providerRequest);
          break;
        case Method.ADD_USER_TO_MANAGER_GROUP:
          appName = (String) request.getmethodParams().get(JmsAtttributes.APP_NAME);
          userId = (String) request.getmethodParams().get(JmsAtttributes.USER_ID);
          groupService.addUserToGroupManager(realm, userStorage, userId, appName, providerRequest);
          break;
        default:
          throw new Exception("Invalid Operation");
      }
      response.setStatus(ProviderResponseStatus.OK);
    } catch (RuntimeException e) {
      response.setStatus(ProviderResponseStatus.KO);
      response.setException(e);
      response.setExceptionType(e.getClass().getCanonicalName());
    }
    return response;
  }
}
