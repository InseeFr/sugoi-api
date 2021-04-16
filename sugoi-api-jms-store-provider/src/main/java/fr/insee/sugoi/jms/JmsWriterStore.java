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
package fr.insee.sugoi.jms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jms.JmsException;

import fr.insee.sugoi.core.model.PasswordChangeRequest;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.model.ProviderResponse.ProviderResponseStatus;
import fr.insee.sugoi.core.model.SendMode;
import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.jms.model.BrokerResponse;
import fr.insee.sugoi.jms.utils.JmsAtttributes;
import fr.insee.sugoi.jms.utils.Method;
import fr.insee.sugoi.jms.writer.JmsWriter;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.UserStorage;

public class JmsWriterStore implements WriterStore {

  private JmsWriter jmsWriter;

  private String queueRequestName;
  private String queueResponseName;
  private String queueUrgentRequestName;
  private String queueUrgentResponseName;

  private Realm realm;

  private UserStorage userStorage;

  public JmsWriterStore(JmsWriter jmsWriter, String queueRequestName, String queueResponseName,
      String queueUrgentRequestName, String queueUrgentResponseName, Realm realm, UserStorage userStorage) {
    this.realm = realm;
    this.userStorage = userStorage;
    this.jmsWriter = jmsWriter;
    this.queueRequestName = queueRequestName;
    this.queueResponseName = queueResponseName;
    this.queueUrgentRequestName = queueUrgentRequestName;
    this.queueUrgentResponseName = queueUrgentResponseName;
  }

  @Override
  public void deleteUser(String id) {
    Map<String, Object> params = new HashMap<>();
    params.put(JmsAtttributes.USER_ID, id);
    params.put(JmsAtttributes.REALM, realm.getName());
    params.put(JmsAtttributes.USER_STORAGE, userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, Method.DELETE_USER, params);
  }

  @Override
  public User createUser(User user) {
    Map<String, Object> params = new HashMap<>();
    params.put(JmsAtttributes.USER, user);
    params.put(JmsAtttributes.REALM, realm.getName());
    params.put(JmsAtttributes.USER_STORAGE, userStorage.getName());
    String correlationId = jmsWriter.writeRequestInQueue(queueRequestName, Method.CREATE_USER, params);
    // WARN : on n'a pas forcément l'entity id ici
    return user;
  }

  @Override
  public ProviderResponse updateUser(User updatedUser) {
    Map<String, Object> params = new HashMap<>();
    params.put(JmsAtttributes.USER, updatedUser);
    params.put(JmsAtttributes.REALM, realm.getName());
    params.put(JmsAtttributes.USER_STORAGE, userStorage.getName());
    String correlationId = jmsWriter.writeRequestInQueue(queueRequestName, Method.UPDATE_USER, params);

    ProviderResponse response = new ProviderResponse();
    // IF asynchronous : status must be requested in another request
    // response.setEntityId(updatedUser.getUsername());
    // response.setRequestId(correlationId);
    // response.setStatus(ProviderResponseStatus.REQUESTED);

    // IF synchronous
    try {
      BrokerResponse br = jmsWriter.checkResponseInQueue(queueResponseName, correlationId);
      response = br.getProviderResponse();
      if (response.getStatus() == ProviderResponseStatus.OK) {
        response.setStatus(ProviderResponseStatus.ACCEPTED);
      } else if (response.getStatus() == ProviderResponseStatus.KO) {
        throw response.getException();
      }
    } catch (JmsException e) {
      response.setStatus(ProviderResponseStatus.REQUESTED);
      response.setRequestId(correlationId);
    }

    return response;

  }

  @Override
  public void deleteGroup(String appName, String groupName) {
    Map<String, Object> params = new HashMap<>();
    params.put(JmsAtttributes.APP_NAME, appName);
    params.put(JmsAtttributes.GROUP_NAME, groupName);
    params.put(JmsAtttributes.REALM, realm.getName());
    params.put(JmsAtttributes.USER_STORAGE, userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, Method.DELETE_GROUP, params);
  }

  @Override
  public Group createGroup(String appName, Group group) {
    Map<String, Object> params = new HashMap<>();
    params.put(JmsAtttributes.APP_NAME, appName);
    params.put(JmsAtttributes.GROUP, group);
    params.put(JmsAtttributes.REALM, realm.getName());
    params.put(JmsAtttributes.USER_STORAGE, userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, Method.CREATE_GROUP, params);
    return group;
  }

  @Override
  public Group updateGroup(String appName, Group updatedGroup) {
    Map<String, Object> params = new HashMap<>();
    params.put(JmsAtttributes.APPLICATION, appName);
    params.put(JmsAtttributes.GROUP, updatedGroup);
    params.put(JmsAtttributes.REALM, realm.getName());
    params.put(JmsAtttributes.USER_STORAGE, userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, Method.UPDATE_GROUP, params);
    return updatedGroup;
  }

  @Override
  public void deleteOrganization(String name) {
    Map<String, Object> params = new HashMap<>();
    params.put(JmsAtttributes.ORGANIZATION_NAME, name);
    params.put(JmsAtttributes.REALM, realm.getName());
    params.put(JmsAtttributes.USER_STORAGE, userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, Method.DELETE_ORGANIZATION, params);
  }

  @Override
  public Organization createOrganization(Organization organization) {
    Map<String, Object> params = new HashMap<>();
    params.put(JmsAtttributes.ORGANIZATION, organization);
    params.put(JmsAtttributes.REALM, realm.getName());
    params.put(JmsAtttributes.USER_STORAGE, userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, Method.CREATE_ORGANIZATION, params);
    return organization;
  }

  @Override
  public Organization updateOrganization(Organization updatedOrganization) {
    Map<String, Object> params = new HashMap<>();
    params.put(JmsAtttributes.ORGANIZATION, updatedOrganization);
    params.put(JmsAtttributes.REALM, realm.getName());
    params.put(JmsAtttributes.USER_STORAGE, userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, Method.UPDATE_ORGANIZATION, params);
    return updatedOrganization;
  }

  @Override
  public void deleteUserFromGroup(String appName, String groupName, String userId) {
    Map<String, Object> params = new HashMap<>();
    params.put(JmsAtttributes.APP_NAME, appName);
    params.put(JmsAtttributes.GROUP_NAME, groupName);
    params.put(JmsAtttributes.USER_ID, userId);
    params.put(JmsAtttributes.REALM, realm.getName());
    params.put(JmsAtttributes.USER_STORAGE, userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, Method.DELETE_USER_FROM_GROUP, params);
  }

  @Override
  public void addUserToGroup(String appName, String groupName, String userId) {
    Map<String, Object> params = new HashMap<>();
    params.put(JmsAtttributes.APP_NAME, appName);
    params.put(JmsAtttributes.GROUP_NAME, groupName);
    params.put(JmsAtttributes.USER_ID, userId);
    params.put(JmsAtttributes.REALM, realm.getName());
    params.put(JmsAtttributes.USER_STORAGE, userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, Method.ADD_USER_TO_GROUP, params);
  }

  @Override
  public void reinitPassword(User user, String password, PasswordChangeRequest pcr, List<SendMode> sendModes) {
    Map<String, Object> params = new HashMap<>();
    params.put(JmsAtttributes.USER, user);
    params.put(JmsAtttributes.REALM, realm.getName());
    params.put(JmsAtttributes.USER_STORAGE, userStorage.getName());
    params.put(JmsAtttributes.PASSWORD, password);
    params.put(JmsAtttributes.PASSWORD_CHANGE_REQUEST, pcr);
    params.put(JmsAtttributes.SEND_MODE, sendModes);
    jmsWriter.writeRequestInQueue(queueRequestName, Method.REINIT_PASSWORD, params);
  }

  @Override
  public void initPassword(User user, String password, PasswordChangeRequest pcr, List<SendMode> sendModes) {
    Map<String, Object> params = new HashMap<>();
    params.put(JmsAtttributes.USER, user);
    params.put(JmsAtttributes.PASSWORD, password);
    params.put(JmsAtttributes.REALM, realm.getName());
    params.put(JmsAtttributes.USER_STORAGE, userStorage.getName());
    params.put(JmsAtttributes.PASSWORD_CHANGE_REQUEST, pcr);
    params.put(JmsAtttributes.SEND_MODE, sendModes);
    jmsWriter.writeRequestInQueue(queueRequestName, Method.INIT_PASSWORD, params);
  }

  @Override
  public void changePasswordResetStatus(User user, boolean isReset) {
    Map<String, Object> params = new HashMap<>();
    params.put(JmsAtttributes.USER, user);
    params.put(JmsAtttributes.IS_RESET, isReset);
    params.put(JmsAtttributes.REALM, realm.getName());
    params.put(JmsAtttributes.USER_STORAGE, userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, Method.CHANGE_PASSWORD_RESET_STATUS, params);
  }

  @Override
  public Application createApplication(Application application) {
    Map<String, Object> params = new HashMap<>();
    params.put(JmsAtttributes.APPLICATION, application);
    params.put(JmsAtttributes.REALM, realm.getName());
    params.put(JmsAtttributes.USER_STORAGE, userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, Method.CREATE_APPLICATION, params);
    return application;
  }

  @Override
  public Application updateApplication(Application updatedApplication) {
    Map<String, Object> params = new HashMap<>();
    params.put(JmsAtttributes.APPLICATION, updatedApplication);
    params.put(JmsAtttributes.REALM, realm.getName());
    params.put(JmsAtttributes.USER_STORAGE, userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, Method.UPDATE_APPLICATION, params);
    return updatedApplication;
  }

  @Override
  public void deleteApplication(String applicationName) {
    Map<String, Object> params = new HashMap<>();
    params.put(JmsAtttributes.APP_NAME, applicationName);
    params.put(JmsAtttributes.REALM, realm.getName());
    params.put(JmsAtttributes.USER_STORAGE, userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, Method.DELETE_APPLICATION, params);
  }

  @Override
  public void changePassword(User user, String oldPassword, String newPassword, PasswordChangeRequest pcr) {
    Map<String, Object> params = new HashMap<>();
    params.put(JmsAtttributes.USER, user);
    params.put(JmsAtttributes.NEW_PASSWORD, newPassword);
    params.put(JmsAtttributes.OLD_PASSWORD, oldPassword);
    params.put(JmsAtttributes.REALM, realm.getName());
    params.put(JmsAtttributes.USER_STORAGE, userStorage.getName());
    params.put(JmsAtttributes.PASSWORD_CHANGE_REQUEST, pcr);
    jmsWriter.writeRequestInQueue(queueRequestName, Method.CHANGE_PASSWORD, params);
  }
}
