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

import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.core.event.configuration.EventKeysConfig;
import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.model.ProviderResponse.ProviderResponseStatus;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.OrganizationService;
import fr.insee.sugoi.core.store.ReaderStore;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.model.exceptions.OrganizationNotFoundException;
import fr.insee.sugoi.model.exceptions.RealmNotFoundException;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import fr.insee.sugoi.model.paging.SearchType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizationServiceImpl implements OrganizationService {

  protected static final Logger logger = LoggerFactory.getLogger(OrganizationServiceImpl.class);

  @Autowired private StoreProvider storeProvider;

  @Autowired private RealmProvider realmProvider;

  @Override
  public ProviderResponse create(
      String realm,
      String storageName,
      Organization organization,
      ProviderRequest providerRequest) {
    ProviderResponse response =
        storeProvider
            .getWriterStore(realm, storageName)
            .createOrganization(organization, providerRequest);
    if (!providerRequest.isAsynchronousAllowed()
        && response.getStatus().equals(ProviderResponseStatus.OK)) {
      response.setEntity(findById(realm, storageName, organization.getIdentifiant()));
    }
    return response;
  }

  @Override
  public ProviderResponse delete(
      String realm, String storageName, String id, ProviderRequest providerRequest) {
    System.out.println("on passe dans organisation service impl avec " + providerRequest);
    return storeProvider.getWriterStore(realm, storageName).deleteOrganization(id, providerRequest);
  }

  @Override
  public Organization findById(String realm, String storage, String id) {
    Realm r = realmProvider.load(realm).orElseThrow(() -> new RealmNotFoundException(realm));
    String nonNullStorage =
        storage != null
            ? storage
            : r.getUserStorages().stream()
                .filter(us -> exist(realm, us.getName(), id))
                .findFirst()
                .orElseThrow(() -> new OrganizationNotFoundException(realm, id))
                .getName();
    Organization org =
        storeProvider
            .getReaderStore(realm, nonNullStorage)
            .getOrganization(id)
            .orElseThrow(() -> new OrganizationNotFoundException(realm, id));
    org.addMetadatas(EventKeysConfig.REALM, realm.toLowerCase());
    org.addMetadatas(EventKeysConfig.USERSTORAGE, nonNullStorage.toLowerCase());
    return org;
  }

  @Override
  public PageResult<Organization> findByProperties(
      String realm,
      String storageName,
      Organization organizationFilter,
      PageableResult pageableResult,
      SearchType typeRecherche) {
    PageResult<Organization> result = new PageResult<>();
    result.setPageSize(pageableResult.getSize());
    Realm r = realmProvider.load(realm).orElseThrow(() -> new RealmNotFoundException(realm));
    pageableResult.setSizeWithMax(
        Integer.parseInt(
            r.getProperties().get(GlobalKeysConfig.ORGANIZATIONS_MAX_OUTPUT_SIZE).get(0)));
    if (storageName != null) {
      result =
          storeProvider
              .getReaderStore(realm, storageName)
              .searchOrganizations(organizationFilter, pageableResult, typeRecherche.name());
    } else {
      for (UserStorage us : r.getUserStorages()) {
        ReaderStore readerStore =
            storeProvider.getStoreForUserStorage(realm, us.getName()).getReader();
        PageResult<Organization> temResult =
            readerStore.searchOrganizations(
                organizationFilter, pageableResult, typeRecherche.name());
        temResult
            .getResults()
            .forEach(
                org -> {
                  org.addMetadatas(GlobalKeysConfig.REALM.getName(), realm);
                  org.addMetadatas(GlobalKeysConfig.USERSTORAGE.getName(), us.getName());
                });
        result.getResults().addAll(temResult.getResults());
        result.setTotalElements(
            temResult.getTotalElements() == -1
                ? temResult.getTotalElements()
                : result.getTotalElements() + temResult.getTotalElements());
        result.setSearchToken(temResult.getSearchToken());
        result.setHasMoreResult(temResult.isHasMoreResult());
        if (result.getResults().size() >= result.getPageSize()) {
          return result;
        }
        pageableResult.setSize(pageableResult.getSize() - result.getTotalElements());
      }
    }
    return result;
  }

  @Override
  public ProviderResponse update(
      String realm,
      String storageName,
      Organization organization,
      ProviderRequest providerRequest) {
    ProviderResponse response =
        storeProvider
            .getWriterStore(realm, storageName)
            .updateOrganization(organization, providerRequest);
    if (!providerRequest.isAsynchronousAllowed()
        && response.getStatus().equals(ProviderResponseStatus.OK)) {
      response.setEntity(findById(realm, storageName, organization.getIdentifiant()));
    }
    return response;
  }

  @Override
  public ProviderResponse updateGpgKey(
      String realm, String storage, String id, byte[] bytes, ProviderRequest providerRequest) {
    return storeProvider
        .getWriterStore(realm, storage)
        .updateOrganizationGpgKey(findById(realm, storage, id), bytes, providerRequest);
  }

  @Override
  public ProviderResponse deleteGpgKey(
      String realm, String storage, String id, ProviderRequest providerRequest) {
    return storeProvider
        .getWriterStore(realm, storage)
        .deleteOrganizationGpgKey(findById(realm, storage, id), providerRequest);
  }

  @Override
  public byte[] getGpgkey(String realm, String storage, String id) {
    return findById(realm, storage, id).getGpgkey();
  }

  @Override
  public boolean exist(String realm, String userStorage, String organizationId) {
    return storeProvider
        .getReaderStore(realm, userStorage)
        .getOrganization(organizationId)
        .isPresent();
  }
}
