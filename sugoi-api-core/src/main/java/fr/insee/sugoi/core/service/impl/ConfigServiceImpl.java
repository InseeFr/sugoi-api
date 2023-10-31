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

import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.model.ProviderResponse.ProviderResponseStatus;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.exceptions.RealmNotFoundException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** ConfigServiceImpl */
@Service
public class ConfigServiceImpl implements ConfigService {

  @Autowired private RealmProvider realmProvider;

  @Override
  public Realm getRealm(String name) {
    return realmProvider.load(name).orElseThrow(() -> new RealmNotFoundException(name));
  }

  @Override
  public List<Realm> getRealms() {
    return realmProvider.findAll();
  }

  @Override
  public ProviderResponse deleteRealm(String realmName, ProviderRequest providerRequest) {
    return realmProvider.deleteRealm(realmName, providerRequest);
  }

  @Override
  public ProviderResponse updateRealm(Realm realm, ProviderRequest providerRequest) {
    ProviderResponse response = realmProvider.updateRealm(realm, providerRequest);
    if (!providerRequest.isAsynchronousAllowed()
        && response.getStatus().equals(ProviderResponseStatus.OK)) {
      response.setEntity(getRealm(realm.getName()));
    }
    return response;
  }

  @Override
  public ProviderResponse createRealm(Realm realm, ProviderRequest providerRequest) {
    ProviderResponse response = realmProvider.createRealm(realm, providerRequest);
    if (!providerRequest.isAsynchronousAllowed()
        && response.getStatus().equals(ProviderResponseStatus.OK)) {
      response.setEntity(getRealm(realm.getName()));
    }
    return response;
  }
}
