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

import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.model.MappingType;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.RealmConfigKeys;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.model.technics.StoreMapping;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

@Configuration
@ConfigurationProperties("fr.insee.sugoi.ldap.default")
public class LdapStoreBeans {

  @Value("${fr.insee.sugoi.ldap.default.username:}")
  private String defaultUsername;

  @Value("${fr.insee.sugoi.ldap.default.password:}")
  private String defaultPassword;

  @Value("${fr.insee.sugoi.ldap.default.pool:}")
  private String defaultPoolSize;

  @Value("${fr.insee.sugoi.ldap.default.connection.timeout:30000}")
  private String defaultConnectionTimeout;

  @Value("${fr.insee.sugoi.ldap.default.port:}")
  private String defaultPort;

  private boolean useAuthenticatedConnectionForReading = true;

  @Value("${fr.insee.sugoi.ldap.default.group_filter_pattern:}")
  private String defaultGroupFilterPattern;

  @Value("${fr.insee.sugoi.ldap.default.group_source_pattern:}")
  private String defaultGroupSourcePattern;

  @Value("${fr.insee.sugoi.ldap.default.group_manager_source_pattern:}")
  private String defaultGroupManagerSourcePattern;

  @Value("${fr.insee.sugoi.ldap.default.vlv.enabled:false}")
  private String vlvEnabled;

  @Value("${fr.insee.sugoi.ldap.default.user-object-classes:top,person}")
  private String defaultUserObjectClasses;

  @Value("${fr.insee.sugoi.ldap.default.organization-object-classes:top,organization}")
  private String defaultOrganizationObjectClasses;

  @Value("${fr.insee.sugoi.ldap.default.group-object-classes:top,groupOfUniqueNames}")
  private String defaultGroupObjectClasses;

  @Value("${fr.insee.sugoi.ldap.default.application-object-classes:top,organizationalUnit}")
  private String defaultApplicationObjectClasses;

  @Value("${fr.insee.sugoi.ldap.default.address-object-classes:top,locality}")
  private String defaultAddressObjectClasses;

  @Value(
      "#{'${fr.insee.sugoi.ldap.default.user-mapping:username:uid,String,rw;groups:memberOf,list_group,ro;habilitations:inseeGroupeDefaut,list_habilitation,rw}'.split(';')}")
  private List<String> defaultUserMapping;

  @Value(
      "#{'${fr.insee.sugoi.ldap.default.organization-mapping:identifiant:uid,String,rw;address:inseeAdressePostaleDN,address,rw;organization:inseeOrganisationDN,organization,rw}'.split(';')}")
  private List<String> defaultOrganizationMapping;

  @Value(
      "#{'${fr.insee.sugoi.ldap.default.group-mapping:name:cn,String,rw;description:description,String,rw;users:uniquemember,list_user,rw}'.split(';')}")
  private List<String> defaultGroupMapping;

  @Value("#{'${fr.insee.sugoi.ldap.default.application-mapping:name:ou,String,rw}'.split(';')}")
  private List<String> defaultApplicationMapping;

  @Bean("LdapReaderStore")
  @Lazy
  @Scope("prototype")
  public LdapReaderStore ldapReaderStore(Realm realm, UserStorage userStorage) {
    return new LdapReaderStore(
        generateConfig(realm, userStorage), getCompleteMapping(realm, userStorage));
  }

  @Bean("LdapWriterStore")
  @Lazy
  @Scope("prototype")
  public LdapWriterStore ldapWriterStore(Realm realm, UserStorage userStorage) {
    return new LdapWriterStore(
        generateConfig(realm, userStorage), getCompleteMapping(realm, userStorage));
  }

  public Map<RealmConfigKeys, String> generateConfig(Realm realm, UserStorage userStorage) {
    Map<RealmConfigKeys, String> config = new HashMap<>();
    config.put(LdapConfigKeys.REALM_NAME, realm.getName());
    config.put(LdapConfigKeys.USERSTORAGE_NAME, userStorage.getName());
    config.put(LdapConfigKeys.URL, realm.getUrl());
    config.put(LdapConfigKeys.PORT, realm.getPort() != null ? realm.getPort() : defaultPort);
    config.put(LdapConfigKeys.USERNAME, defaultUsername);
    config.put(LdapConfigKeys.PASSWORD, defaultPassword);
    config.put(
        LdapConfigKeys.READ_CONNECTION_AUTHENTICATED,
        String.valueOf(useAuthenticatedConnectionForReading));
    config.put(LdapConfigKeys.POOL_SIZE, defaultPoolSize);
    config.put(
        LdapConfigKeys.LDAP_CONNECTION_TIMEOUT,
        userStorage.getProperties().get(LdapConfigKeys.LDAP_CONNECTION_TIMEOUT) != null
            ? userStorage.getProperties().get(LdapConfigKeys.LDAP_CONNECTION_TIMEOUT)
            : defaultConnectionTimeout);
    config.put(GlobalKeysConfig.USER_SOURCE, userStorage.getUserSource());
    config.put(GlobalKeysConfig.APP_SOURCE, realm.getAppSource());
    config.put(GlobalKeysConfig.ORGANIZATION_SOURCE, userStorage.getOrganizationSource());
    config.put(GlobalKeysConfig.ADDRESS_SOURCE, userStorage.getAddressSource());
    config.put(
        LdapConfigKeys.GROUP_SOURCE_PATTERN,
        realm.getProperties().get(LdapConfigKeys.GROUP_SOURCE_PATTERN) != null
            ? realm.getProperties().get(LdapConfigKeys.GROUP_SOURCE_PATTERN)
            : defaultGroupSourcePattern);
    config.put(
        LdapConfigKeys.GROUP_MANAGER_SOURCE_PATTERN,
        realm.getProperties().get(LdapConfigKeys.GROUP_MANAGER_SOURCE_PATTERN) != null
            ? realm.getProperties().get(LdapConfigKeys.GROUP_MANAGER_SOURCE_PATTERN)
            : defaultGroupManagerSourcePattern);
    config.put(
        LdapConfigKeys.GROUP_FILTER_PATTERN,
        realm.getProperties().get(LdapConfigKeys.GROUP_FILTER_PATTERN) != null
            ? realm.getProperties().get(LdapConfigKeys.GROUP_FILTER_PATTERN)
            : defaultGroupFilterPattern);
    config.put(LdapConfigKeys.REALM_NAME, realm.getName());
    config.put(
        LdapConfigKeys.VLV_ENABLED,
        realm.getProperties().get(LdapConfigKeys.VLV_ENABLED) != null
            ? realm.getProperties().get(LdapConfigKeys.VLV_ENABLED)
            : vlvEnabled);
    config.put(
        LdapConfigKeys.USER_OBJECT_CLASSES,
        userStorage.getProperties().get(LdapConfigKeys.USER_OBJECT_CLASSES) != null
            ? userStorage.getProperties().get(LdapConfigKeys.USER_OBJECT_CLASSES)
            : defaultUserObjectClasses);
    config.put(
        LdapConfigKeys.ORGANIZATION_OBJECT_CLASSES,
        userStorage.getProperties().get(LdapConfigKeys.ORGANIZATION_OBJECT_CLASSES) != null
            ? userStorage.getProperties().get(LdapConfigKeys.ORGANIZATION_OBJECT_CLASSES)
            : defaultOrganizationObjectClasses);
    config.put(
        LdapConfigKeys.GROUP_OBJECT_CLASSES,
        realm.getProperties().get(LdapConfigKeys.GROUP_OBJECT_CLASSES) != null
            ? realm.getProperties().get(LdapConfigKeys.GROUP_OBJECT_CLASSES)
            : defaultGroupObjectClasses);
    config.put(
        LdapConfigKeys.APPLICATION_OBJECT_CLASSES,
        realm.getProperties().get(LdapConfigKeys.APPLICATION_OBJECT_CLASSES) != null
            ? realm.getProperties().get(LdapConfigKeys.APPLICATION_OBJECT_CLASSES)
            : defaultApplicationObjectClasses);
    config.put(
        LdapConfigKeys.ADDRESS_OBJECT_CLASSES,
        userStorage.getProperties().get(LdapConfigKeys.ADDRESS_OBJECT_CLASSES) != null
            ? userStorage.getProperties().get(LdapConfigKeys.ADDRESS_OBJECT_CLASSES)
            : defaultAddressObjectClasses);

    return config;
  }

  public Map<MappingType, List<StoreMapping>> getCompleteMapping(
      Realm realm, UserStorage userStorage) {
    Map<MappingType, List<StoreMapping>> resultMappings = new EnumMap<>(MappingType.class);
    if (userStorage.getUserMappings() == null || userStorage.getUserMappings().isEmpty()) {
      resultMappings.put(
          MappingType.USERMAPPING,
          defaultUserMapping.stream().map(StoreMapping::new).collect(Collectors.toList()));
    } else {
      resultMappings.put(MappingType.USERMAPPING, userStorage.getUserMappings());
    }
    if (realm.getApplicationMappings() == null || realm.getApplicationMappings().isEmpty()) {
      resultMappings.put(
          MappingType.APPLICATIONMAPPING,
          defaultApplicationMapping.stream().map(StoreMapping::new).collect(Collectors.toList()));
    } else {
      resultMappings.put(MappingType.APPLICATIONMAPPING, realm.getApplicationMappings());
    }
    if (userStorage.getOrganizationMappings() == null
        || userStorage.getOrganizationMappings().isEmpty()) {
      resultMappings.put(
          MappingType.ORGANIZATIONMAPPING,
          defaultOrganizationMapping.stream().map(StoreMapping::new).collect(Collectors.toList()));
    } else {
      resultMappings.put(MappingType.ORGANIZATIONMAPPING, userStorage.getOrganizationMappings());
    }
    if (realm.getGroupMappings() == null || realm.getGroupMappings().isEmpty()) {
      resultMappings.put(
          MappingType.GROUPMAPPING,
          defaultGroupMapping.stream().map(StoreMapping::new).collect(Collectors.toList()));
    } else {
      resultMappings.put(MappingType.GROUPMAPPING, realm.getGroupMappings());
    }
    return resultMappings;
  }

  public boolean isUseAuthenticatedConnectionForReading() {
    return useAuthenticatedConnectionForReading;
  }

  public void setUseAuthenticatedConnectionForReading(
      boolean useAuthenticatedConnectionForReading) {
    this.useAuthenticatedConnectionForReading = useAuthenticatedConnectionForReading;
  }
}
