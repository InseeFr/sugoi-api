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

import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

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

  @Value("${fr.insee.sugoi.ldap.default.group_filter_pattern:}")
  private String defaultGroupFilterPattern;

  @Value("${fr.insee.sugoi.ldap.default.group_source_pattern:}")
  private String defaultGroupSourcePattern;

  @Value("${fr.insee.sugoi.ldap.default.vlv.enabled:false}")
  private String vlvEnabled;

  @Bean("LdapReaderStore")
  @Lazy
  @Scope("prototype")
  public LdapReaderStore ldapReaderStore(Realm realm, UserStorage userStorage) {
    return new LdapReaderStore(generateConfig(realm, userStorage));
  }

  @Bean("LdapWriterStore")
  @Lazy
  @Scope("prototype")
  public LdapWriterStore ldapWriterStore(Realm realm, UserStorage userStorage) {
    return new LdapWriterStore(generateConfig(realm, userStorage));
  }

  public Map<String, String> generateConfig(Realm realm, UserStorage userStorage) {
    Map<String, String> config = new HashMap<>();
    config.put(
        LdapConfigKeys.NAME,
        userStorage.getName() != null ? userStorage.getName() : realm.getName());
    config.put(LdapConfigKeys.URL, realm.getUrl());
    config.put(LdapConfigKeys.PORT, defaultPort);
    config.put(LdapConfigKeys.USERNAME, defaultUsername);
    config.put(LdapConfigKeys.PASSWORD, defaultPassword);
    config.put(LdapConfigKeys.POOL_SIZE, defaultPoolSize);
    config.put(LdapConfigKeys.USER_SOURCE, userStorage.getUserSource());
    config.put(LdapConfigKeys.APP_SOURCE, realm.getAppSource());
    config.put(LdapConfigKeys.ORGANIZATION_SOURCE, userStorage.getOrganizationSource());
    config.put(LdapConfigKeys.ADDRESS_SOURCE, userStorage.getAddressSource());
    config.put(
        LdapConfigKeys.GROUP_SOURCE_PATTERN,
        userStorage.getProperties().get("group_source_pattern") != null
            ? userStorage.getProperties().get("group_source_pattern")
            : defaultGroupSourcePattern);
    config.put(
        LdapConfigKeys.GROUP_FILTER_PATTERN,
        userStorage.getProperties().get("group_filter_pattern") != null
            ? userStorage.getProperties().get("group_filter_pattern")
            : defaultGroupFilterPattern);
    config.put(LdapConfigKeys.REALM_NAME, realm.getName());
    config.put(
        LdapConfigKeys.VLV_ENABLED,
        realm.getProperties().get(LdapConfigKeys.VLV_ENABLED) != null
            ? realm.getProperties().get(LdapConfigKeys.VLV_ENABLED)
            : vlvEnabled);

    return config;
  }
}
