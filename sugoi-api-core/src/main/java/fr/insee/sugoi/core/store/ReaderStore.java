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
package fr.insee.sugoi.core.store;

import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;

/**
 * Reader store are responsible of all operations reading a store (to fetch informations) All method
 * in this class should have no effect on the store (except for `validateCredential`)
 *
 * <p>Implementations may use caching, but should take care of managing this cache so that
 * modifications are reflected in due time
 */
public interface ReaderStore {

  public User getUser(String id);

  public PageResult<User> searchUsers(
      User searchUser, PageableResult pageable, String searchOperator);

  public PageResult<User> getUsersInGroup(String appName, String groupName);

  public Organization getOrganization(String id);

  public PageResult<Organization> searchOrganizations(
      Organization organizationFilter, PageableResult pageable, String searchOperator);

  public Group getGroup(String appName, String groupName);

  public PageResult<Group> searchGroups(
      String appName, Group groupFilter, PageableResult pageable, String searchOperator);

  public boolean validateCredentials(User user, String credential);

  public Application getApplication(String applicationName);

  public PageResult<Application> searchApplications(
      Application applicationFilter, PageableResult pageable, String searchOperator);
}
