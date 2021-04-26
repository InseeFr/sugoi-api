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
import fr.insee.sugoi.core.exceptions.OrganizationAlreadyExistException;
import fr.insee.sugoi.core.exceptions.OrganizationNotCreatedException;
import fr.insee.sugoi.core.exceptions.OrganizationNotFoundException;
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
import java.util.Optional;
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
    try {
      if (findById(realm, storageName, organization.getIdentifiant()).isEmpty()) {
        String orgName =
            storeProvider
                .getWriterStore(realm, storageName)
                .createOrganization(organization)
                .getIdentifiant();
        sugoiEventPublisher.publishCustomEvent(
            realm,
            storageName,
            SugoiEventTypeEnum.CREATE_ORGANIZATION,
            Map.ofEntries(Map.entry(EventKeysConfig.ORGANIZATION, organization)));
        return findById(realm, storageName, orgName)
            .orElseThrow(
                () ->
                    new OrganizationNotCreatedException(
                        "Cannot create organization " + orgName + " in realm " + realm));
      }
      throw new OrganizationAlreadyExistException(
          "Organization " + organization.getIdentifiant() + " already exist in realm " + realm);
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storageName,
          SugoiEventTypeEnum.CREATE_ORGANIZATION_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.ORGANIZATION, organization),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      if (e instanceof OrganizationNotCreatedException) {
        throw (OrganizationNotCreatedException) e;
      } else if (e instanceof OrganizationAlreadyExistException) {
        throw (OrganizationAlreadyExistException) e;
      } else {
        throw e;
      }
    }
  }

  @Override
  public void delete(String realm, String storageName, String id) {
    try {

      findById(realm, storageName, id)
          .orElseThrow(
              () ->
                  new OrganizationNotFoundException(
                      "Cannot find organization " + id + " in realm " + realm));
      storeProvider.getWriterStore(realm, storageName).deleteOrganization(id);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storageName,
          SugoiEventTypeEnum.DELETE_ORGANIZATION,
          Map.ofEntries(Map.entry(EventKeysConfig.ORGANIZATION_ID, id)));
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storageName,
          SugoiEventTypeEnum.DELETE_ORGANIZATION_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.ORGANIZATION_ID, id),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      if (e instanceof OrganizationNotFoundException) {
        throw (OrganizationNotFoundException) e;
      } else {
        throw e;
      }
    }
  }

  @Override
  public Optional<Organization> findById(String realm, String storage, String id) {
    try {
      Organization org = null;
      if (id != null) {
        if (storage != null) {
          org = storeProvider.getReaderStore(realm, storage).getOrganization(id);
          org.addMetadatas(EventKeysConfig.REALM, realm.toLowerCase());
          org.addMetadatas(EventKeysConfig.USERSTORAGE, storage.toLowerCase());
        } else {
          Realm r = realmProvider.load(realm);
          for (UserStorage us : r.getUserStorages()) {
            try {
              org = storeProvider.getReaderStore(realm, us.getName()).getOrganization(id);
              org.addMetadatas(GlobalKeysConfig.REALM, realm);
              org.addMetadatas(EventKeysConfig.USERSTORAGE, us.getName());
              break;
            } catch (Exception e) {
              logger.debug(
                  "Error when trying to find organization "
                      + id
                      + " on realm "
                      + realm
                      + " and userstorage "
                      + us
                      + " error "
                      + e.getMessage());
            }
          }
        }
        sugoiEventPublisher.publishCustomEvent(
            realm,
            storage,
            SugoiEventTypeEnum.FIND_ORGANIZATION_BY_ID,
            Map.ofEntries(Map.entry(EventKeysConfig.ORGANIZATION_ID, id)));
      }
      return Optional.ofNullable(org);
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storage,
          SugoiEventTypeEnum.FIND_ORGANIZATION_BY_ID_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.ORGANIZATION_ID, id),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      return Optional.empty();
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
      throw new RuntimeException("Erreur lors de la récupération des organizations", e);
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
  public void update(String realm, String storageName, Organization organization) {
    try {
      findById(realm, storageName, organization.getIdentifiant())
          .orElseThrow(
              () ->
                  new OrganizationNotFoundException(
                      "Cannot find organization "
                          + organization.getIdentifiant()
                          + " in realm "
                          + realm));
      storeProvider.getWriterStore(realm, storageName).updateOrganization(organization);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storageName,
          SugoiEventTypeEnum.UPDATE_ORGANIZATION,
          Map.ofEntries(Map.entry(EventKeysConfig.ORGANIZATION, organization)));
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storageName,
          SugoiEventTypeEnum.UPDATE_ORGANIZATION_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.ORGANIZATION, organization),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      if (e instanceof OrganizationNotFoundException) {
        throw (OrganizationNotFoundException) e;
      } else {
        throw e;
      }
    }
  }
}
