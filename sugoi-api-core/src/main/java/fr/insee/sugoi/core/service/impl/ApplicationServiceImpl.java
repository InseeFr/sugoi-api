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

import fr.insee.sugoi.core.event.configuration.EventKeysConfig;
import fr.insee.sugoi.core.event.model.SugoiEventTypeEnum;
import fr.insee.sugoi.core.event.publisher.SugoiEventPublisher;
import fr.insee.sugoi.core.exceptions.ApplicationAlreadyExistException;
import fr.insee.sugoi.core.exceptions.ApplicationNotCreatedException;
import fr.insee.sugoi.core.exceptions.ApplicationNotFoundException;
import fr.insee.sugoi.core.service.ApplicationService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import fr.insee.sugoi.model.paging.SearchType;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationServiceImpl implements ApplicationService {

  @Autowired private StoreProvider storeProvider;
  @Autowired private SugoiEventPublisher sugoiEventPublisher;

  @Override
  public Application create(String realm, Application application) {
    try {
      if (findById(realm, application.getName()).isEmpty()) {
        String appName =
            storeProvider.getWriterStore(realm).createApplication(application).getName();
        sugoiEventPublisher.publishCustomEvent(
            realm,
            null,
            SugoiEventTypeEnum.CREATE_APPLICATION,
            Map.ofEntries(Map.entry(EventKeysConfig.APPLICATION, application)));
        return findById(realm, appName)
            .orElseThrow(
                () ->
                    new ApplicationNotCreatedException(
                        "Cannot create application " + appName + " in realm " + realm));
      }
      throw new ApplicationAlreadyExistException(
          "Application " + application.getName() + " already exist in realm " + realm);
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          null,
          SugoiEventTypeEnum.CREATE_APPLICATION_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.APPLICATION, application),
              Map.entry(EventKeysConfig.ERROR, e.toString())));

      if (e instanceof ApplicationAlreadyExistException) {
        throw (ApplicationAlreadyExistException) e;
      } else if (e instanceof ApplicationNotCreatedException) {
        throw (ApplicationNotCreatedException) e;
      } else {
        throw e;
      }
    }
  }

  @Override
  public void delete(String realm, String id) {
    try {
      findById(realm, id)
          .orElseThrow(
              () ->
                  new ApplicationNotFoundException(
                      "Application " + id + " doesn't exist in realm " + realm));
      storeProvider.getWriterStore(realm).deleteApplication(id);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          null,
          SugoiEventTypeEnum.DELETE_APPLICATION,
          Map.ofEntries(Map.entry(EventKeysConfig.APPLICATION_ID, id)));

    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          null,
          SugoiEventTypeEnum.DELETE_APPLICATION_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.APPLICATION_ID, id),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      if (e instanceof ApplicationNotFoundException) {
        throw (ApplicationNotFoundException) e;
      } else {
        throw e;
      }
    }
  }

  @Override
  public void update(String realm, Application application) {
    try {
      findById(realm, application.getName())
          .orElseThrow(
              () ->
                  new ApplicationNotFoundException(
                      "Application " + application.getName() + " doesn't exist in realm " + realm));
      storeProvider.getWriterStore(realm).updateApplication(application);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          null,
          SugoiEventTypeEnum.UPDATE_APPLICATION,
          Map.ofEntries(Map.entry(EventKeysConfig.APPLICATION, application)));
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          null,
          SugoiEventTypeEnum.UPDATE_APPLICATION_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.APPLICATION, application),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      if (e instanceof ApplicationNotFoundException) {
        throw (ApplicationNotFoundException) e;
      } else {
        throw e;
      }
    }
  }

  @Override
  public Optional<Application> findById(String realm, String id) {
    try {

      Application app = storeProvider.getReaderStore(realm).getApplication(id);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          null,
          SugoiEventTypeEnum.FIND_APPLICATION_BY_ID,
          Map.ofEntries(Map.entry(EventKeysConfig.APPLICATION_ID, id)));
      return Optional.ofNullable(app);
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          null,
          SugoiEventTypeEnum.FIND_APPLICATION_BY_ID_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.APPLICATION_ID, id),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      return Optional.empty();
    }
  }

  @Override
  public PageResult<Application> findByProperties(
      String realm, Application applicationFilter, PageableResult pageableResult) {
    try {

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
