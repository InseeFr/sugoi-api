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
package fr.insee.sugoi.store.ldap;

import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class LdapStoreBeans {

  @Value("${fr.insee.sugoi.ldap.default.username:}")
  private String defaultUsername;

  @Value("${fr.insee.sugoi.ldap.default.password:}")
  private String defaultPassword;

  @Value("${fr.insee.sugoi.ldap.default.pool:}")
  private String defaultPoolSize;

  @Value("${fr.insee.sugoi.ldap.default.port:}")
  private String defaultPort;

  @Bean("LdapReaderStore")
  @Lazy
  public LdapReaderStore ldapReaderStore(Realm realm, UserStorage userStorage) {
    return new LdapReaderStore(generateConfig(realm, userStorage));
  }

  @Bean("LdapWriterStore")
  @Lazy
  public LdapWriterStore ldapWriterStore(Realm realm, UserStorage userStorage) {
    return new LdapWriterStore(generateConfig(realm, userStorage));
  }

  public Map<String, String> generateConfig(Realm realm, UserStorage userStorage) {
    Map<String, String> config = new HashMap<>();
    config.put("name", userStorage.getName() != null ? userStorage.getName() : realm.getName());
    config.put("url", realm.getUrl());
    config.put("port", defaultPort);
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
}
