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

import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.OrganizationService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.UserStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizationServiceImpl implements OrganizationService {

  @Autowired private RealmProvider realmProvider;

  @Autowired private StoreProvider storeProvider;

  @Override
  public Organization searchOrganization(String realmName, String name) {
    UserStorage userStorage = realmProvider.load(realmName).getUserStorages().get(0);
    return storeProvider
        .getStoreForUserStorage(realmName, userStorage.getName())
        .getReader()
        .searchOrganization(realmName, name);
  }
}
