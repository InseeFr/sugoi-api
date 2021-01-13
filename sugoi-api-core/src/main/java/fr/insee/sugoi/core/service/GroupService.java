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
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.model.Group;

public interface GroupService {

  Group create(String realm, String storage, String appName, Group group);

  void update(String realm, String storage, String appName, Group group);

  void delete(String realm, String storage, String appName, String id);

  Group findById(String realm, String storage, String appName, String id);

  PageResult<Group> findByProperties(
      String realm,
      String storage,
      String appName,
      Group groupFilter,
      PageableResult pageableResult);
}
