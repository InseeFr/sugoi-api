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
import java.util.Map;

import fr.insee.sugoi.core.store.WriterStore;
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
    params.put("id", id);
    params.put("realm", realm.getName());
    params.put("userStorage", userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, "deleteUser", params);
  }

  @Override
  public User createUser(User user) {
    Map<String, Object> params = new HashMap<>();
    params.put("user", user);
    params.put("realm", realm.getName());
    params.put("userStorage", userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, "createUser", params);
    return user;
  }

  @Override
  public User updateUser(User updatedUser) {
    Map<String, Object> params = new HashMap<>();
    params.put("updatedUser", updatedUser);
    params.put("realm", realm.getName());
    params.put("userStorage", userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, "updateUser", params);
    return updatedUser;
  }

  @Override
  public void deleteGroup(String appName, String groupName) {
    Map<String, Object> params = new HashMap<>();
    params.put("appName", appName);
    params.put("groupName", groupName);
    params.put("realm", realm.getName());
    params.put("userStorage", userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, "deleteGroup", params);

  }

  @Override
  public Group createGroup(String appName, Group group) {
    Map<String, Object> params = new HashMap<>();
    params.put("appName", appName);
    params.put("group", group);
    params.put("realm", realm.getName());
    params.put("userStorage", userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, "createGroup", params);
    return group;
  }

  @Override
  public Group updateGroup(String appName, Group updatedGroup) {
    Map<String, Object> params = new HashMap<>();
    params.put("appName", appName);
    params.put("updatedGroup", updatedGroup);
    params.put("realm", realm.getName());
    params.put("userStorage", userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, "updateGroup", params);
    return updatedGroup;
  }

  @Override
  public void deleteOrganization(String name) {
    Map<String, Object> params = new HashMap<>();
    params.put("name", name);
    params.put("realm", realm.getName());
    params.put("userStorage", userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, "deleteOrganization", params);
  }

  @Override
  public Organization createOrganization(Organization organization) {
    Map<String, Object> params = new HashMap<>();
    params.put("organization", organization);
    params.put("realm", realm.getName());
    params.put("userStorage", userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, "createOrganization", params);
    return organization;
  }

  @Override
  public Organization updateOrganization(Organization updatedOrganization) {
    Map<String, Object> params = new HashMap<>();
    params.put("updatedOrganization", updatedOrganization);
    params.put("realm", realm.getName());
    params.put("userStorage", userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, "updateOrganization", params);
    return updatedOrganization;
  }

  @Override
  public void deleteUserFromGroup(String appName, String groupName, String userId) {
    Map<String, Object> params = new HashMap<>();
    params.put("appName", appName);
    params.put("groupName", groupName);
    params.put("userId", userId);
    params.put("realm", realm.getName());
    params.put("userStorage", userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, "deleteUserFromGroup", params);
  }

  @Override
  public void addUserToGroup(String appName, String groupName, String userId) {
    Map<String, Object> params = new HashMap<>();
    params.put("appName", appName);
    params.put("groupName", groupName);
    params.put("userId", userId);
    params.put("realm", realm.getName());
    params.put("userStorage", userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, "addUserToGroup", params);
  }

  @Override
  public void reinitPassword(User user) {
    Map<String, Object> params = new HashMap<>();
    params.put("user", user);
    params.put("realm", realm.getName());
    params.put("userStorage", userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, "reinitPassword", params);
  }

  @Override
  public void initPassword(User user, String password) {
    Map<String, Object> params = new HashMap<>();
    params.put("user", user);
    params.put("password", password);
    params.put("realm", realm.getName());
    params.put("userStorage", userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, "initPassword", params);
  }

  @Override
  public void changePasswordResetStatus(User user, boolean isReset) {
    Map<String, Object> params = new HashMap<>();
    params.put("user", user);
    params.put("isReset", isReset);
    params.put("realm", realm.getName());
    params.put("userStorage", userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, "changePasswordResetStatus", params);
  }

  @Override
  public Application createApplication(Application application) {
    Map<String, Object> params = new HashMap<>();
    params.put("application", application);
    params.put("realm", realm.getName());
    params.put("userStorage", userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, "createApplication", params);
    return application;
  }

  @Override
  public Application updateApplication(Application updatedApplication) {
    Map<String, Object> params = new HashMap<>();
    params.put("updatedApplication", updatedApplication);
    params.put("realm", realm.getName());
    params.put("userStorage", userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, "updateApplication", params);
    return updatedApplication;
  }

  @Override
  public void deleteApplication(String applicationName) {
    Map<String, Object> params = new HashMap<>();
    params.put("applicationName", applicationName);
    params.put("realm", realm.getName());
    params.put("userStorage", userStorage.getName());
    jmsWriter.writeRequestInQueue(queueRequestName, "deleteApplication", params);
  }
}
