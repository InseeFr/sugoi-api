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
import fr.insee.sugoi.core.service.ApplicationService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationServiceImpl implements ApplicationService {

  @Autowired private StoreProvider storeProvider;

  @Override
  public Application create(String realm, String storageName, Application application) {
    return storeProvider.getWriterStore(realm, storageName).createApplication(application);
  }

  @Override
  public void delete(String realm, String storageName, String id) {
    storeProvider.getWriterStore(realm, storageName).deleteApplication(id);
  }

  @Override
  public void update(String realm, String storageName, Application application) {
    storeProvider.getWriterStore(realm, storageName).updateApplication(application);
  }

  @Override
  public Application findById(String realm, String storage, String id) {
    return storeProvider.getReaderStore(realm, storage).getApplication(id);
  }

  @Override
  public PageResult<Application> findByProperties(
      String realm,
      String storageName,
      Application applicationFilter,
      PageableResult pageableResult) {
    return storeProvider
        .getReaderStore(realm, storageName)
        .searchApplications(applicationFilter, pageableResult, "AND");
  }
}
