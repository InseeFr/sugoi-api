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
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.OrganizationService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.UserStorage;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizationServiceImpl implements OrganizationService {

  @Autowired private RealmProvider realmProvider;

  @Autowired private StoreProvider storeProvider;

  public Organization create(String realm, String storage, Organization organization) {
    UserStorage userStorage = realmProvider.load(realm).getUserStorages().get(0);
    return storeProvider
        .getStoreForUserStorage(realm, userStorage.getName())
        .getWriter()
        .createOrganization(organization);
  }

  @Override
  public void delete(String realm, String id) {
    UserStorage userStorage = realmProvider.load(realm).getUserStorages().get(0);
    storeProvider
        .getStoreForUserStorage(realm, userStorage.getName())
        .getWriter()
        .deleteOrganization(id);
  }

  @Override
  public PageResult<Organization> search(
      String realm, String application, String role, String property) {
    UserStorage userStorage = realmProvider.load(realm).getUserStorages().get(0);
    PageableResult pageableResult = new PageableResult();
    Map<String, String> properties = new HashMap<>();
    properties.put("application", application);
    properties.put("role", role);
    properties.put("property", property);
    return storeProvider
        .getStoreForUserStorage(realm, userStorage.getName())
        .getReader()
        .searchOrganizations(new Organization(), pageableResult, "AND");
  }

  @Override
  public void update(String realm, String storage, String id, Organization organization) {
    UserStorage userStorage = realmProvider.load(realm).getUserStorages().get(0);
    storeProvider
        .getStoreForUserStorage(realm, userStorage.getName())
        .getWriter()
        .updateOrganization(organization);
  }
}
