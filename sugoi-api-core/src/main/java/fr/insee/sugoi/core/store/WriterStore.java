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

public interface WriterStore {

  void deleteUser(String id);

  User createUser(User user);

  User updateUser(User updatedUser);

  void deleteGroup(String name);

  Group createGroup(Group group);

  Group updateGroup(Group updatedGroup);

  void deleteOrganization(String organizationId);

  Application createApplication(Application application);

  Application updateApplication(Application updatedApplication);

  void deleteApplication(String applicationName);

  Organization createOrganization(Organization organization);

  Organization updateOrganization(Organization updatedOrganization);

  void deleteUserFromGroup(String groupName, String userId);

  void addUserToGroup(String groupName, String userId);

  void reinitPassword(User user);

  void initPassword(User user, String password);

  void changePasswordResetStatus(User user, boolean isReset);
}
