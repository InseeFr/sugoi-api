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

import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.store.ReaderStore;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import java.util.Map;

public class FileReaderStore implements ReaderStore {

  public FileReaderStore(Map<String, String> generateConfig) {}

  public FileReaderStore() {}

  @Override
  public User getUser(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PageResult<User> searchUsers(
      Map<String, String> properties, PageableResult pageable, String typeRecherche) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Organization getOrganization(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PageResult<User> getUsersInGroup(String appName, String groupName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PageResult<Organization> searchOrganizations(
      Map<String, String> searchProperties, PageableResult pageable, String searchOperator) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Group getGroup(String appName, String groupName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PageResult<Group> searchGroups(
      String appName,
      Map<String, String> searchProperties,
      PageableResult pageable,
      String searchOperator) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean validateCredentials(User user, String credential) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Application getApplication(String applicationName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PageResult<Application> searchApplications(
      Map<String, String> searchProperties, PageableResult pageable, String searchOperator) {
    // TODO Auto-generated method stub
    return null;
  }
}
