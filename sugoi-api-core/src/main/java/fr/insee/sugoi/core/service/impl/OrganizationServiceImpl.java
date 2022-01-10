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
import fr.insee.sugoi.core.event.model.SugoiEventTypeEnum;
import fr.insee.sugoi.core.event.publisher.SugoiEventPublisher;
import fr.insee.sugoi.core.exceptions.OrganizationNotFoundException;
import fr.insee.sugoi.core.exceptions.RealmNotFoundException;
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
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import fr.insee.sugoi.model.paging.SearchType;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizationServiceImpl implements OrganizationService {

  protected static final Logger logger = LoggerFactory.getLogger(OrganizationServiceImpl.class);

  @Autowired private StoreProvider storeProvider;

  @Autowired private SugoiEventPublisher sugoiEventPublisher;

  @Autowired private RealmProvider realmProvider;

  @Override
  public ProviderResponse create(
      String realm,
      String storageName,
      Organization organization,
      ProviderRequest providerRequest) {
    try {
      ProviderResponse response =
          storeProvider
              .getWriterStore(realm, storageName)
              .createOrganization(organization, providerRequest);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storageName,
          SugoiEventTypeEnum.CREATE_ORGANIZATION,
          Map.ofEntries(Map.entry(EventKeysConfig.ORGANIZATION, organization)));
      if (!providerRequest.isAsynchronousAllowed()
          && response.getStatus().equals(ProviderResponseStatus.OK)) {
        response.setEntity(findById(realm, storageName, organization.getIdentifiant()));
      }
      return response;
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storageName,
          SugoiEventTypeEnum.CREATE_ORGANIZATION_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.ORGANIZATION, organization),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      throw e;
    }
  }

  @Override
  public ProviderResponse delete(
      String realm, String storageName, String id, ProviderRequest providerRequest) {
    try {
      ProviderResponse response =
          storeProvider.getWriterStore(realm, storageName).deleteOrganization(id, providerRequest);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storageName,
          SugoiEventTypeEnum.DELETE_ORGANIZATION,
          Map.ofEntries(Map.entry(EventKeysConfig.ORGANIZATION_ID, id)));
      return response;
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storageName,
          SugoiEventTypeEnum.DELETE_ORGANIZATION_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.ORGANIZATION_ID, id),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      throw e;
    }
  }

  @Override
  public Organization findById(String realm, String storage, String id) {
    try {
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
      sugoiEventPublisher.publishCustomEvent(
          realm,
          nonNullStorage,
          SugoiEventTypeEnum.FIND_ORGANIZATION_BY_ID,
          Map.ofEntries(Map.entry(EventKeysConfig.ORGANIZATION_ID, id)));
      return org;
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storage,
          SugoiEventTypeEnum.FIND_ORGANIZATION_BY_ID_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.ORGANIZATION_ID, id != null ? id : ""),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      throw e;
    }
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
    try {
      if (storageName != null) {
        result =
            storeProvider
                .getReaderStore(realm, storageName)
                .searchOrganizations(organizationFilter, pageableResult, typeRecherche.name());
      } else {
        Realm r = realmProvider.load(realm).orElseThrow(() -> new RealmNotFoundException(realm));
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
                    org.addMetadatas(GlobalKeysConfig.REALM, realm);
                    org.addMetadatas(GlobalKeysConfig.USERSTORAGE, us.getName());
                  });
          result.getResults().addAll(temResult.getResults());
          result.setTotalElements(
              temResult.getTotalElements() == -1
                  ? temResult.getTotalElements()
                  : result.getTotalElements() + temResult.getTotalElements());
          result.setSearchToken(temResult.getSearchToken());
          result.setHasMoreResult(temResult.isHasMoreResult());
          if (result.getResults().size() >= result.getPageSize()) {
            sugoiEventPublisher.publishCustomEvent(
                realm,
                storageName,
                SugoiEventTypeEnum.FIND_ORGANIZATIONS,
                Map.ofEntries(
                    Map.entry(EventKeysConfig.ORGANIZATION_FILTER, organizationFilter),
                    Map.entry(EventKeysConfig.PAGEABLE_RESULT, pageableResult),
                    Map.entry(EventKeysConfig.TYPE_RECHERCHE, typeRecherche)));
            return result;
          }
          pageableResult.setSize(pageableResult.getSize() - result.getTotalElements());
        }
      }
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storageName,
          SugoiEventTypeEnum.FIND_ORGANIZATIONS_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.ORGANIZATION_FILTER, organizationFilter),
              Map.entry(EventKeysConfig.PAGEABLE_RESULT, pageableResult),
              Map.entry(EventKeysConfig.TYPE_RECHERCHE, typeRecherche),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      throw e;
    }
    sugoiEventPublisher.publishCustomEvent(
        realm,
        storageName,
        SugoiEventTypeEnum.FIND_ORGANIZATIONS,
        Map.ofEntries(
            Map.entry(EventKeysConfig.ORGANIZATION_FILTER, organizationFilter),
            Map.entry(EventKeysConfig.PAGEABLE_RESULT, pageableResult),
            Map.entry(EventKeysConfig.TYPE_RECHERCHE, typeRecherche)));
    return result;
  }

  @Override
  public ProviderResponse update(
      String realm,
      String storageName,
      Organization organization,
      ProviderRequest providerRequest) {
    try {

      ProviderResponse response =
          storeProvider
              .getWriterStore(realm, storageName)
              .updateOrganization(organization, providerRequest);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storageName,
          SugoiEventTypeEnum.UPDATE_ORGANIZATION,
          Map.ofEntries(Map.entry(EventKeysConfig.ORGANIZATION, organization)));
      if (!providerRequest.isAsynchronousAllowed()
          && response.getStatus().equals(ProviderResponseStatus.OK)) {
        response.setEntity(findById(realm, storageName, organization.getIdentifiant()));
      }
      return response;
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storageName,
          SugoiEventTypeEnum.UPDATE_ORGANIZATION_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.ORGANIZATION, organization),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      throw e;
    }
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
