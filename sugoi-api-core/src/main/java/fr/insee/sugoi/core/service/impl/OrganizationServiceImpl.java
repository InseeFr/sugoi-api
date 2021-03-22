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

import fr.insee.sugoi.core.event.model.SugoiEventTypeEnum;
import fr.insee.sugoi.core.event.publisher.SugoiEventPublisher;
import fr.insee.sugoi.core.exceptions.EntityNotFoundException;
import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.model.SearchType;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.OrganizationService;
import fr.insee.sugoi.core.store.ReaderStore;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizationServiceImpl implements OrganizationService {

  protected static final Logger logger = LogManager.getLogger(OrganizationServiceImpl.class);

  @Autowired private StoreProvider storeProvider;

  @Autowired private SugoiEventPublisher sugoiEventPublisher;

  @Autowired private RealmProvider realmProvider;

  @Override
  public Organization create(String realm, String storageName, Organization organization) {
    sugoiEventPublisher.publishCustomEvent(
        realm,
        storageName,
        SugoiEventTypeEnum.CREATE_ORGANIZATION,
        Map.ofEntries(Map.entry("organization", organization)));
    return storeProvider.getWriterStore(realm, storageName).createOrganization(organization);
  }

  @Override
  public void delete(String realm, String storageName, String id) {
    sugoiEventPublisher.publishCustomEvent(
        realm,
        storageName,
        SugoiEventTypeEnum.DELETE_ORGANIZATION,
        Map.ofEntries(Map.entry("organizationId", id)));

    storeProvider.getWriterStore(realm, storageName).deleteOrganization(id);
  }

  @Override
  public Organization findById(String realm, String storage, String id) {
    if (id == null) {
      id = "";
    }
    sugoiEventPublisher.publishCustomEvent(
        realm,
        storage,
        SugoiEventTypeEnum.FIND_ORGANIZATION_BY_ID,
        Map.ofEntries(Map.entry("organizationId", id)));
    if (storage != null) {
      try {

        Organization org = storeProvider.getReaderStore(realm, storage).getOrganization(id);
        org.addMetadatas("realm", realm.toLowerCase());
        org.addMetadatas("userStorage", storage.toLowerCase());
        return org;
      } catch (Exception e) {
        throw new EntityNotFoundException(
            "Organization not found in realm " + realm + " and userStorage " + storage);
      }
    } else {
      Realm r = realmProvider.load(realm);
      for (UserStorage us : r.getUserStorages()) {
        try {
          Organization org = storeProvider.getReaderStore(realm, us.getName()).getOrganization(id);
          if (org != null) {
            org.addMetadatas("realm", realm);
            org.addMetadatas("userStorage", us.getName());
            return org;
          }
        } catch (Exception e) {
          logger.debug(
              "Organization " + id + "not in realm " + realm + " and userstorage " + us.getName());
        }
      }
    }
    throw new EntityNotFoundException("Organization not found in realm " + realm);
  }

  @Override
  public PageResult<Organization> findByProperties(
      String realm,
      String storageName,
      Organization organizationFilter,
      PageableResult pageableResult,
      SearchType typeRecherche) {

    sugoiEventPublisher.publishCustomEvent(
        realm,
        storageName,
        SugoiEventTypeEnum.FIND_ORGANIZATIONS,
        Map.ofEntries(
            Map.entry("organizationFilter", organizationFilter),
            Map.entry("pageableResult", pageableResult),
            Map.entry("typeRecherche", typeRecherche)));
    PageResult<Organization> result = new PageResult<>();
    result.setPageSize(pageableResult.getSize());
    try {
      if (storageName != null) {
        result =
            storeProvider
                .getReaderStore(realm, storageName)
                .searchOrganizations(organizationFilter, pageableResult, typeRecherche.name());
      } else {
        Realm r = realmProvider.load(realm);
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
                    org.addMetadatas("realm", realm);
                    org.addMetadatas("userStorage", us.getName());
                  });
          result.getResults().addAll(temResult.getResults());
          result.setTotalElements(result.getResults().size());
          if (result.getTotalElements() >= result.getPageSize()) {
            return result;
          }
          pageableResult.setSize(pageableResult.getSize() - result.getTotalElements());
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Erreur lors de la récupération des organizations", e);
    }
    return result;
  }

  @Override
  public void update(String realm, String storageName, Organization organization) {
    sugoiEventPublisher.publishCustomEvent(
        realm,
        storageName,
        SugoiEventTypeEnum.UPDATE_ORGANIZATION,
        Map.ofEntries(Map.entry("organization", organization)));
    storeProvider.getWriterStore(realm, storageName).updateOrganization(organization);
  }
}
