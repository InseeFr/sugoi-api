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
package fr.insee.sugoi.core.configuration.impl;

import fr.insee.sugoi.core.configuration.RealmProvider;
import fr.insee.sugoi.core.configuration.RealmStorage;
import fr.insee.sugoi.core.configuration.StoreStorage;
import fr.insee.sugoi.core.utils.Exceptions.RealmNotFoundException;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class RealmStorageImpl implements RealmStorage {

  @Value("${fr.insee.sugoi.ldap.default.username}")
  private String defaultUsername;

  @Value("${fr.insee.sugoi.ldap.default.password}")
  private String defaultPassword;

  @Value("${fr.insee.sugoi.ldap.default.pool}")
  private String defaultPoolSize;

  @Autowired private RealmProvider realmProvider;

  @Autowired private StoreStorage connectionStorage;

  @Cacheable
  public Realm getRealm(String realmName) {
    try {
      Realm realm = realmProvider.load(realmName);
      return realm;
    } catch (RealmNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private Map<String, String> generateConf(Realm realm, UserStorage userStorage) {
    Map<String, String> config = new HashMap<>();
    config.put("name", userStorage.getName() != null ? userStorage.getName() : realm.getName());
    config.put("url", realm.getUrl());
    config.put("username", defaultUsername);
    config.put("password", defaultPassword);
    config.put("pool_size", defaultPoolSize);
    config.put("user_source", userStorage.getUserSource());
    config.put("app_source", realm.getAppSource());
    config.put("organization_source", userStorage.getOrganizationSource());
    config.put("realm_name", realm.getName());
    config.put("type", "ldap");
    return config;
  }

  @Override
  public List<Realm> getRealms() {
    List<Realm> realms = realmProvider.findAll();
    return realms;
  }
}
