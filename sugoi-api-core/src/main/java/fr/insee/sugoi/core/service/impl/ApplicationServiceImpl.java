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
import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.model.SearchType;
import fr.insee.sugoi.core.service.ApplicationService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.Application;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationServiceImpl implements ApplicationService {

  @Autowired private StoreProvider storeProvider;
  @Autowired private SugoiEventPublisher sugoiEventPublisher;

  @Override
  public Application create(String realm, Application application) {
    sugoiEventPublisher.publishCustomEvent(
        realm,
        null,
        SugoiEventTypeEnum.CREATE_APPLICATION,
        Map.ofEntries(Map.entry("application", application)));
    return storeProvider.getWriterStore(realm).createApplication(application);
  }

  @Override
  public void delete(String realm, String id) {
    sugoiEventPublisher.publishCustomEvent(
        realm,
        null,
        SugoiEventTypeEnum.DELETE_APPLICATION,
        Map.ofEntries(Map.entry("applicationId", id)));
    storeProvider.getWriterStore(realm).deleteApplication(id);
  }

  @Override
  public void update(String realm, Application application) {
    sugoiEventPublisher.publishCustomEvent(
        realm,
        null,
        SugoiEventTypeEnum.UPDATE_APPLICATION,
        Map.ofEntries(Map.entry("application", application)));
    storeProvider.getWriterStore(realm).updateApplication(application);
  }

  @Override
  public Application findById(String realm, String id) {
    if (id == null) {
      id = "";
    }

    sugoiEventPublisher.publishCustomEvent(
        realm,
        null,
        SugoiEventTypeEnum.FIND_APPLICATION_BY_ID,
        Map.ofEntries(Map.entry("applicationId", id)));
    return storeProvider.getReaderStore(realm).getApplication(id);
  }

  @Override
  public PageResult<Application> findByProperties(
      String realm, Application applicationFilter, PageableResult pageableResult) {
    sugoiEventPublisher.publishCustomEvent(
        realm,
        null,
        SugoiEventTypeEnum.FIND_APPLICATIONS,
        Map.ofEntries(Map.entry("applicationFilter", applicationFilter)));
    return storeProvider
        .getReaderStore(realm)
        .searchApplications(applicationFilter, pageableResult, SearchType.AND.name());
  }
}
