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
import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.model.ProviderResponse.ProviderResponseStatus;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.ApplicationService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.exceptions.ApplicationNotFoundException;
import fr.insee.sugoi.model.exceptions.RealmNotFoundException;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import fr.insee.sugoi.model.paging.SearchType;
import java.util.ArrayList;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationServiceImpl implements ApplicationService {

  @Autowired private StoreProvider storeProvider;
  @Autowired private SugoiEventPublisher sugoiEventPublisher;
  @Autowired private RealmProvider realmProvider;

  @Override
  public ProviderResponse create(
      String realm, Application application, ProviderRequest providerRequest) {
    try {
      if (application.getGroups() == null) {
        application.setGroups(new ArrayList<Group>());
      }
      ProviderResponse response =
          storeProvider.getWriterStore(realm).createApplication(application, providerRequest);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          null,
          SugoiEventTypeEnum.CREATE_APPLICATION,
          Map.ofEntries(Map.entry(EventKeysConfig.APPLICATION, application)));
      if (!providerRequest.isAsynchronousAllowed()
          && response.getStatus().equals(ProviderResponseStatus.OK)) {
        response.setEntity(findById(realm, application.getName()));
      }
      return response;
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          null,
          SugoiEventTypeEnum.CREATE_APPLICATION_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.APPLICATION, application),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      throw e;
    }
  }

  @Override
  public ProviderResponse delete(String realm, String id, ProviderRequest providerRequest) {
    try {
      ProviderResponse response =
          storeProvider.getWriterStore(realm).deleteApplication(id, providerRequest);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          null,
          SugoiEventTypeEnum.DELETE_APPLICATION,
          Map.ofEntries(Map.entry(EventKeysConfig.APPLICATION_ID, id)));
      return response;
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          null,
          SugoiEventTypeEnum.DELETE_APPLICATION_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.APPLICATION_ID, id),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      throw e;
    }
  }

  @Override
  public ProviderResponse update(
      String realm, Application application, ProviderRequest providerRequest) {
    try {
      ProviderResponse response =
          storeProvider.getWriterStore(realm).updateApplication(application, providerRequest);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          null,
          SugoiEventTypeEnum.UPDATE_APPLICATION,
          Map.ofEntries(Map.entry(EventKeysConfig.APPLICATION, application)));
      if (!providerRequest.isAsynchronousAllowed()
          && response.getStatus().equals(ProviderResponseStatus.OK)) {
        response.setEntity(findById(realm, application.getName()));
      }
      return response;
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          null,
          SugoiEventTypeEnum.UPDATE_APPLICATION_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.APPLICATION, application),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      throw e;
    }
  }

  @Override
  public Application findById(String realm, String id) {
    try {
      Application app =
          storeProvider
              .getReaderStore(realm)
              .getApplication(id)
              .orElseThrow(() -> new ApplicationNotFoundException(realm, id));
      sugoiEventPublisher.publishCustomEvent(
          realm,
          null,
          SugoiEventTypeEnum.FIND_APPLICATION_BY_ID,
          Map.ofEntries(Map.entry(EventKeysConfig.APPLICATION_ID, id)));
      return app;
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          null,
          SugoiEventTypeEnum.FIND_APPLICATION_BY_ID_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.APPLICATION_ID, id),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      throw e;
    }
  }

  @Override
  public PageResult<Application> findByProperties(
      String realm, Application applicationFilter, PageableResult pageableResult) {
    try {
      Realm r = realmProvider.load(realm).orElseThrow(() -> new RealmNotFoundException(realm));
      pageableResult.setSizeWithMax(
          Integer.parseInt(
              r.getProperties().get(GlobalKeysConfig.APPLICATIONS_MAX_OUTPUT_SIZE).get(0)));
      PageResult<Application> apps =
          storeProvider
              .getReaderStore(realm)
              .searchApplications(applicationFilter, pageableResult, SearchType.AND.name());
      sugoiEventPublisher.publishCustomEvent(
          realm,
          null,
          SugoiEventTypeEnum.FIND_APPLICATIONS,
          Map.ofEntries(Map.entry(EventKeysConfig.APPLICATION_FILTER, applicationFilter)));
      return apps;
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          null,
          SugoiEventTypeEnum.FIND_APPLICATIONS_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.APPLICATION_FILTER, applicationFilter),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      throw e;
    }
  }
}
