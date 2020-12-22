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
package fr.insee.sugoi.core.service;

import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.model.Organization;

public interface OrganizationService {

  Organization create(String realm, String storage, Organization organization);

  void delete(String realm, String id);

  PageResult<Organization> search(String realm, String application, String role, String property);

  void update(String realm, String storage, String id, Organization organization);
}
