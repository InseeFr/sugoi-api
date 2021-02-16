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
package fr.insee.sugoi.store.file;

import fr.insee.sugoi.core.model.PasswordChangeRequest;
import fr.insee.sugoi.core.model.SendMode;
import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import java.util.List;
import java.util.Map;

public class FileWriterStore implements WriterStore {

  public FileWriterStore(Map<String, String> generateConfig) {}

  @Override
  public void deleteUser(String id) {
    // TODO Auto-generated method stub
  }

  @Override
  public User createUser(User user) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public User updateUser(User updatedUser) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteGroup(String appName, String groupName) {
    // TODO Auto-generated method stub

  }

  @Override
  public Group createGroup(String appName, Group appGroup) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Group updateGroup(String appName, Group updatedGroup) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteOrganization(String name) {
    // TODO Auto-generated method stub

  }

  @Override
  public Organization createOrganization(Organization organization) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Organization updateOrganization(Organization updatedOrganization) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteUserFromGroup(String appName, String groupName, String userId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addUserToGroup(String appName, String groupName, String userId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void reinitPassword(
      User user, String generatedPassword, PasswordChangeRequest pcr, List<SendMode> sendMode) {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPassword(
      User user, String password, PasswordChangeRequest pcr, List<SendMode> sendMode) {
    // TODO Auto-generated method stub

  }

  @Override
  public void changePasswordResetStatus(User user, boolean isReset) {
    // TODO Auto-generated method stub

  }

  @Override
  public Application createApplication(Application application) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Application updateApplication(Application updatedApplication) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteApplication(String applicationName) {
    // TODO Auto-generated method stub

  }

  @Override
  public void changePassword(
      User user, String oldPassword, String newPassword, PasswordChangeRequest pcr) {
    // TODO Auto-generated method stub

  }
}
