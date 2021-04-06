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
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.model.Realm;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** ConfigServiceImpl */
@Service
public class ConfigServiceImpl implements ConfigService {

  @Autowired private RealmProvider realmProvider;

  @Autowired private SugoiEventPublisher sugoiEventPublisher;

  @Override
  public Realm getRealm(String name) {
    sugoiEventPublisher.publishCustomEvent(
        null,
        null,
        SugoiEventTypeEnum.FIND_REALM_BY_ID,
        Map.ofEntries(Map.entry(EventKeysConfig.REALM_NAME, name)));
    return realmProvider.load(name);
  }

  @Override
  public List<Realm> getRealms() {
    sugoiEventPublisher.publishCustomEvent(null, null, SugoiEventTypeEnum.FIND_REALMS, null);
    return realmProvider.findAll();
  }

  @Override
  public void deleteRealm(String realmName) {
    sugoiEventPublisher.publishCustomEvent(null, null, SugoiEventTypeEnum.DELETE_REALM, null);
    realmProvider.deleteRealm(realmName);
  }

  @Override
  public void updateRealm(Realm realm) {
    sugoiEventPublisher.publishCustomEvent(null, null, SugoiEventTypeEnum.UPDATE_REALM, null);
    realmProvider.updateRealm(realm);
  }

  @Override
  public void createRealm(Realm realm) {
    sugoiEventPublisher.publishCustomEvent(null, null, SugoiEventTypeEnum.CREATE_REALM, null);
    realmProvider.createRealm(realm);
  }
}
