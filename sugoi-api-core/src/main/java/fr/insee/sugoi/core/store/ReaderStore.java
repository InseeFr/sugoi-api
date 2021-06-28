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

import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;

/**
 * Reader store are responsible of all operations reading a store (to fetch informations) All method
 * in this class should have no effect on the store (except for `validateCredential`)
 *
 * <p>Implementations may use caching, but should take care of managing this cache so that
 * modifications are reflected in due time
 */
public interface ReaderStore {

  /**
   * Retrieve the user with the given id in the store.
   *
   * @param id the id of the user
   * @return the user with matching id, null if no user matches
   */
  public User getUser(String id);

  /**
   * Search users matching userFilter filled attributes.
   *
   * @param userFilter an incomplete user with attributes set to be matched with
   * @param pageable properties for pageable request
   * @param searchOperator 'OR' or 'AND' to determine if multiple attributes should match or only
   *     one
   * @return a PageResult containing a list of matching users.
   */
  public PageResult<User> searchUsers(
      User userFilter, PageableResult pageable, String searchOperator);

  /**
   * Retrieve the organization with the given id in the store.
   *
   * @param id the id of the organization
   * @throws UnsupportedOperationException if the configuration for organizations is not set on the
   *     UserStorage.
   * @return the organization with matching id, null if no user matches
   */
  public Organization getOrganization(String id);

  /**
   * Search organizations matching organizationFilter filled attributes.
   *
   * @param organizationFilter an incomplete organization with attributes set to be matched with
   * @param pageable properties for pageable request
   * @param searchOperator 'OR' or 'AND' to determine if multiple attributes should match or only
   *     one
   * @throws UnsupportedOperationException if the configuration for organizations is not set on the
   *     UserStorage.
   * @return a PageResult containing a list of matching organizations.
   */
  public PageResult<Organization> searchOrganizations(
      Organization organizationFilter, PageableResult pageable, String searchOperator);

  /**
   * Retrieve the application with the given name in the store.
   *
   * @param applicationName
   * @throws UnsupportedOperationException if the configuration for applications is not set on the
   *     Realm.
   * @return the application with all its informations, groups and users in groups included. If no
   *     application matches applicationName, null is returned.
   */
  public Application getApplication(String applicationName);

  /**
   * Search applications matching applicationFilter filled attributes.
   *
   * @param applicationFilter an incomplete application with attributes set to be match
   * @param pageable properties for pageable request
   * @param searchOperator 'OR' or 'AND' to determine if multiple attributes should match or only
   *     one
   * @throws UnsupportedOperationException if the configuration for applications is not set on the
   *     Realm.
   * @return a PageResult containing a list of matching applications.
   */
  public PageResult<Application> searchApplications(
      Application applicationFilter, PageableResult pageable, String searchOperator);

  /**
   * Retrieve the group with name groupName of the application appName. All the user belonging to
   * the group are included with only there name.
   *
   * @param appName
   * @param groupName
   * @throws UnsupportedOperationException if the configuration for applications or groups is not
   *     set.
   * @return the group with its simplified list of users. Null if no group found.
   */
  public Group getGroup(String appName, String groupName);

  /**
   * Search groups in application appName matching groupFilter filled attributes. Groups are
   * returned with simplified users.
   *
   * @param appName name of the application where to search the group
   * @param groupFilter an incomplete group with attributes set to be match
   * @param pageable properties for pageable request
   * @param searchOperator 'OR' or 'AND' to determine if multiple attributes should match or only
   *     one
   * @throws UnsupportedOperationException if the configuration for applications or groups is not
   *     set.
   * @return a PageResult containing all matching groups with simplified users.
   */
  public PageResult<Group> searchGroups(
      String appName, Group groupFilter, PageableResult pageable, String searchOperator);

  /**
   * Retrieve a page of users in the group groupName of the application appName. Users are returned
   * with all of their information. If a user doesn't belong to the realm it will not be added. If
   * the group doesn't exist the users list is empty.
   *
   * @param appName
   * @param groupName
   * @throws UnsupportedOperationException if the configuration for applications or groups is not
   *     set.
   * @return a PageResult containing a page of users in groupName
   */
  public PageResult<User> getUsersInGroup(String appName, String groupName);

  /**
   * Check if credential is the credential of the user in the store.
   *
   * @param user
   * @param credential
   * @return true if credential matches the credential of the user in the store, false otherwise
   */
  public boolean validateCredentials(User user, String credential);
}
