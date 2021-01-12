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
import fr.insee.sugoi.core.service.OrganizationService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizationServiceImpl implements OrganizationService {

  @Autowired private StoreProvider storeProvider;

  @Override
  public Organization create(String realm, Organization organization, String storageName) {
    return storeProvider.getWriterStore(realm, storageName).createOrganization(organization);
  }

  @Override
  public void delete(String realm, String id, String storageName) {
    storeProvider.getWriterStore(realm, storageName).deleteOrganization(id);
  }

  @Override
  public Organization findById(String realm, String id, String storage) {
    return storeProvider.getReaderStore(realm, storage).getOrganization(id);
  }

  @Override
  public PageResult<Organization> findByProperties(
      String realm,
      Organization organizationFilter,
      PageableResult pageableResult,
      String storageName) {
    return storeProvider
        .getReaderStore(realm, storageName)
        .searchOrganizations(organizationFilter, pageableResult, "AND");
  }

  @Override
  public void update(String realm, Organization organization, String storageName) {
    storeProvider.getWriterStore(realm, storageName).updateOrganization(organization);
  }
}
