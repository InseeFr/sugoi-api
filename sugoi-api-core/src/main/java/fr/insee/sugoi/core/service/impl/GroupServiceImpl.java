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
package fr.insee.sugoi.core.service.impl;

import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.service.GroupService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupServiceImpl implements GroupService {

  @Autowired private StoreProvider storeProvider;

  @Override
  public Group create(String realm, String storageName, String appName, Group group) {
    return storeProvider.getWriterStore(realm, storageName).createGroup(appName, group);
  }

  @Override
  public void delete(String realm, String storageName, String appName, String id) {
    storeProvider.getWriterStore(realm, storageName).deleteGroup(appName, id);
  }

  @Override
  public Group findById(String realm, String storage, String appName, String id) {
    return storeProvider.getReaderStore(realm, storage).getGroup(appName, id);
  }

  @Override
  public PageResult<Group> findByProperties(
      String realm,
      String storageName,
      String appName,
      Group groupFilter,
      PageableResult pageableResult) {
    return storeProvider
        .getReaderStore(realm, storageName)
        .searchGroups(appName, groupFilter, pageableResult, "AND");
  }

  @Override
  public void update(String realm, String storageName, String appName, Group group) {
    storeProvider.getWriterStore(realm, storageName).updateGroup(appName, group);
  }
}
