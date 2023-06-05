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
package fr.insee.sugoi.config;

import com.unboundid.ldap.sdk.AddRequest;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ModifyRequest;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.core.configuration.UiMappingService;
import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.model.ProviderResponse.ProviderResponseStatus;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.ldap.utils.LdapFactory;
import fr.insee.sugoi.ldap.utils.LdapFilter;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.ldap.utils.exception.LdapStoreConnectionFailedException;
import fr.insee.sugoi.ldap.utils.mapper.RealmLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.UserStorageLdapMapper;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.Realm.UIMappingType;
import fr.insee.sugoi.model.RealmConfigKeys;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.model.exceptions.RealmAlreadyExistException;
import fr.insee.sugoi.model.exceptions.RealmNotFoundException;
import fr.insee.sugoi.model.exceptions.RealmWriteFailureException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
/*
 * This class load the realm configuration from an ldap
 */
@ConditionalOnProperty(
    value = "fr.insee.sugoi.realm.config.type",
    havingValue = "ldap",
    matchIfMissing = false)
public class LdapRealmProviderDAOImpl implements RealmProvider {

  @Value("${fr.insee.sugoi.ldap.default.username:}")
  private String defaultUsername;

  @Value("${fr.insee.sugoi.ldap.default.password:}")
  private String defaultPassword;

  @Value("${fr.insee.sugoi.ldap.default.pool:}")
  private String defaultPoolSize;

  @Value("${fr.insee.sugoi.ldap.default.use-authenticated-connection-for-reading:true}")
  private boolean useAuthenticatedConnectionForReading;

  @Value("${fr.insee.sugoi.config.ldap.profils.url:}")
  private String url;

  @Value("${fr.insee.sugoi.config.ldap.profils.port:}")
  private int port;

  @Value("${fr.insee.sugoi.config.ldap.profils.branche:}")
  private String baseDn;

  @Value("${fr.insee.sugoi.config.ldap.profils.timeout:30000}")
  private String connectionTimeout;

  @Value("${fr.insee.sugoi.store.defaultReader:}")
  private String defaultReader;

  @Value("${fr.insee.sugoi.store.defaultWriter:}")
  private String defaultWriter;

  @Value("${fr.insee.sugoi.config.ldap.profils.pattern:cn=Profil_{realm}_Sugoi}")
  private String realmEntryPattern;

  @Value("${fr.insee.sugoi.default.app_managed_attribute_keys:}")
  private String defaultAppManagedAttributeKeyList;

  @Value("${fr.insee.sugoi.default.app_managed_attribute_patterns:}")
  private String defaultAppManagedAttributePatternList;

  @Value("${fr.insee.sugoi.config.ldap.default.sortKey:}")
  private String defaultSortKey;

  @Autowired UiMappingService uiMappingService;

  private static final Logger logger = LoggerFactory.getLogger(LdapRealmProviderDAOImpl.class);

  private LDAPConnectionPool ldapConnectionPoolAuthenticated;

  private LDAPConnectionPool ldapConnectionPool;

  @Value("${fr.insee.sugoi.users.maxoutputsize:1000}")
  private String defaultUserMaxOutputSize;

  @Value("${fr.insee.sugoi.applications.maxoutputsize:1000}")
  private String defaultApplicationMaxOutputSize;

  @Value("${fr.insee.sugoi.groups.maxoutputsize:1000}")
  private String defaultGroupMaxOutputSize;

  @Value("${fr.insee.sugoi.organizations.maxoutputsize:1000}")
  private String defaultOrganizationMaxOutputSize;

  @Override
  public Optional<Realm> load(String realmName) {
    logger.info("Loading configuration from ldap://{}:{}/{}", url, port, baseDn);
    try {
      SearchResultEntry realmEntry = ldapPoolConnection().getEntry(getRealmDn(realmName));
      if (realmEntry != null) {
        logger.debug("Found entry {}", realmEntry.getDN());
        return Optional.of(generateRealmFromSearchEntry(realmEntry));
      } else {
        return Optional.empty();
      }
    } catch (LDAPException e) {
      throw new RealmNotFoundException("Impossible de charger le realm " + realmName, e);
    }
  }

  @Override
  public List<Realm> findAll() {
    logger.info("Loading realms configurations from ldap://{}:{}/{}", url, port, baseDn);
    try {
      SearchRequest searchRequest =
          new SearchRequest(baseDn, SearchScope.ONE, LdapFilter.create(getRealmRDN("*")), "*", "+");
      List<Realm> realms = new ArrayList<>();
      for (SearchResultEntry searchEntry :
          ldapPoolConnection().search(searchRequest).getSearchEntries()) {
        realms.add(generateRealmFromSearchEntry(searchEntry));
      }
      return realms;
    } catch (LDAPException e) {
      throw new RealmNotFoundException("Impossible de charger les realms", e);
    }
  }

  private void addDefaultProperties(
      UserStorage userstorage, Map<RealmConfigKeys, List<String>> defaultRealmProperties) {
    for (Entry<RealmConfigKeys, List<String>> entry : defaultRealmProperties.entrySet()) {
      userstorage.getProperties().putIfAbsent(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public ProviderResponse createRealm(Realm realm, ProviderRequest providerRequest) {
    if (load(realm.getName()).isEmpty()) {
      try {
        AddRequest addRequest =
            new AddRequest(
                getRealmDn(realm.getName()),
                RealmLdapMapper.mapToAttributes(realm, realmEntryPattern, baseDn));
        ldapConnectionPoolAuthenticated().add(addRequest);
        for (UserStorage userStorage : realm.getUserStorages()) {
          AddRequest userStorageAddRequest =
              new AddRequest(
                  getUserStorageDn(userStorage.getName(), realm.getName()),
                  UserStorageLdapMapper.mapToAttributes(userStorage));
          ldapConnectionPoolAuthenticated().add(userStorageAddRequest);
        }
        ProviderResponse response = new ProviderResponse();
        response.setStatus(ProviderResponseStatus.OK);
        response.setEntityId(realm.getName());
        return response;
      } catch (LDAPException e) {
        throw new RealmWriteFailureException("Failed to create realm " + realm.getName(), e);
      }
    } else {
      throw new RealmAlreadyExistException(
          String.format("Realm %s already exist", realm.getName()));
    }
  }

  @Override
  public ProviderResponse updateRealm(Realm realm, ProviderRequest providerRequest) {
    if (load(realm.getName()).isPresent()) {
      try {
        for (UserStorage userStorage : realm.getUserStorages()) {
          ModifyRequest userStorageModifyRequest =
              new ModifyRequest(
                  getUserStorageDn(userStorage.getName(), realm.getName()),
                  UserStorageLdapMapper.createMods(userStorage));
          ldapConnectionPoolAuthenticated().modify(userStorageModifyRequest);
        }
        ModifyRequest modifyRequest =
            new ModifyRequest(
                getRealmDn(realm.getName()),
                RealmLdapMapper.createMods(realm, realmEntryPattern, baseDn));
        ldapConnectionPoolAuthenticated().modify(modifyRequest);
        ProviderResponse response = new ProviderResponse();
        response.setStatus(ProviderResponseStatus.OK);
        response.setEntityId(realm.getName());
        return response;
      } catch (LDAPException e) {
        throw new RealmWriteFailureException("Failed to update realm " + realm.getName(), e);
      }
    } else {
      throw new RealmNotFoundException(realm.getName());
    }
  }

  @Override
  public ProviderResponse deleteRealm(String realmName, ProviderRequest providerRequest) {
    try {
      List<UserStorage> userStorages =
          load(realmName)
              .orElseThrow(() -> new RealmNotFoundException(realmName))
              .getUserStorages();
      for (UserStorage userStorage : userStorages) {
        if (ldapConnectionPoolAuthenticated()
                .getEntry(getUserStorageDn(userStorage.getName(), realmName), "+", "*")
            != null) {
          ldapConnectionPoolAuthenticated()
              .delete(getUserStorageDn(userStorage.getName(), realmName));
        }
      }
      ldapConnectionPoolAuthenticated().delete(getRealmDn(realmName));
      ProviderResponse response = new ProviderResponse();
      response.setStatus(ProviderResponseStatus.OK);
      response.setEntityId(realmName);
      return response;
    } catch (LDAPException e) {
      throw new RealmWriteFailureException("Failed to delete realm " + realmName, e);
    }
  }

  private List<UserStorage> loadUserStorages(
      String realmName, Map<RealmConfigKeys, List<String>> defaultRealmProperties)
      throws LDAPException {
    List<UserStorage> userstorages =
        ldapPoolConnection()
            .search(
                getRealmDn(realmName),
                SearchScope.SUBORDINATE_SUBTREE,
                Filter.create("objectClass=*"))
            .getSearchEntries()
            .stream()
            .map(
                searchEntry -> UserStorageLdapMapper.mapFromAttributes(searchEntry.getAttributes()))
            .collect(Collectors.toList());
    userstorages.stream()
        .forEach(userStorage -> addDefaultProperties(userStorage, defaultRealmProperties));
    return userstorages;
  }

  private Realm generateRealmFromSearchEntry(SearchResultEntry searchEntry) throws LDAPException {
    Realm realm = RealmLdapMapper.mapFromSearchEntry(searchEntry);
    realm.setUserStorages(loadUserStorages(realm.getName(), realm.getProperties()));
    addDefaultOnRealm(realm);
    sortUiLists(realm);
    return realm;
  }

  private void sortUiLists(Realm realm) {
    try {
      // sort ui list of field by order
      Collections.sort(realm.getUiMapping().get(UIMappingType.UI_USER_MAPPING));
      Collections.sort(realm.getUiMapping().get(UIMappingType.UI_ORGANIZATION_MAPPING));
    } catch (Exception e) {
      logger.debug("ui fields are not sorted");
    }
  }

  private void addDefaultOnRealm(Realm realm) {
    if (realm.getReaderType() == null) {
      realm.setReaderType(defaultReader);
    }
    if (realm.getWriterType() == null) {
      realm.setWriterType(defaultWriter);
    }
    realm
        .getProperties()
        .putIfAbsent(
            GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_KEYS_LIST,
            List.of(defaultAppManagedAttributeKeyList));
    realm
        .getProperties()
        .putIfAbsent(
            GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_PATTERNS_LIST,
            List.of(defaultAppManagedAttributePatternList));
    realm
        .getUiMapping()
        .putIfAbsent(UIMappingType.UI_USER_MAPPING, uiMappingService.getUserUiDefaultField());
    realm
        .getUiMapping()
        .putIfAbsent(
            UIMappingType.UI_ORGANIZATION_MAPPING,
            uiMappingService.getOrganizationUiDefaultField());
    realm.getProperties().putIfAbsent(LdapConfigKeys.SORT_KEY, List.of(defaultSortKey));
    realm
        .getProperties()
        .putIfAbsent(GlobalKeysConfig.USERS_MAX_OUTPUT_SIZE, List.of(defaultUserMaxOutputSize));
    realm
        .getProperties()
        .putIfAbsent(
            GlobalKeysConfig.APPLICATIONS_MAX_OUTPUT_SIZE,
            List.of(defaultApplicationMaxOutputSize));
    realm
        .getProperties()
        .putIfAbsent(GlobalKeysConfig.GROUPS_MAX_OUTPUT_SIZE, List.of(defaultGroupMaxOutputSize));
    realm
        .getProperties()
        .putIfAbsent(
            GlobalKeysConfig.ORGANIZATIONS_MAX_OUTPUT_SIZE,
            List.of(defaultOrganizationMaxOutputSize));
  }

  private LDAPConnectionPool ldapPoolConnection() {
    try {
      if (ldapConnectionPool == null) {
        if (useAuthenticatedConnectionForReading) {
          ldapConnectionPool = ldapConnectionPoolAuthenticated();
        } else {
          Map<RealmConfigKeys, String> config = new HashMap<>();
          config.put(LdapConfigKeys.URL, url);
          config.put(LdapConfigKeys.PORT, String.valueOf(port));
          config.put(LdapConfigKeys.POOL_SIZE, defaultPoolSize);
          config.put(LdapConfigKeys.LDAP_CONNECTION_TIMEOUT, connectionTimeout);
          ldapConnectionPool = LdapFactory.getConnectionPool(config);
        }
      }
      return ldapConnectionPool;
    } catch (LDAPException e) {
      throw new LdapStoreConnectionFailedException(
          String.format("Failed connection to ldap realm store %s:%d", url, port), e);
    }
  }

  private LDAPConnectionPool ldapConnectionPoolAuthenticated() {
    try {
      if (ldapConnectionPoolAuthenticated == null) {
        Map<RealmConfigKeys, String> config = new HashMap<>();
        config.put(LdapConfigKeys.URL, url);
        config.put(LdapConfigKeys.PORT, String.valueOf(port));
        config.put(LdapConfigKeys.POOL_SIZE, defaultPoolSize);
        config.put(LdapConfigKeys.USERNAME, defaultUsername);
        config.put(LdapConfigKeys.PASSWORD, defaultPassword);
        config.put(LdapConfigKeys.LDAP_CONNECTION_TIMEOUT, connectionTimeout);
        ldapConnectionPool = LdapFactory.getConnectionPoolAuthenticated(config);
      }
      return ldapConnectionPool;
    } catch (LDAPException e) {
      throw new LdapStoreConnectionFailedException(
          String.format("Failed authenticated connection to ldap realm store %s:%d", url, port), e);
    }
  }

  private String getRealmDn(String realmName) {
    return getRealmRDN(realmName) + "," + baseDn;
  }

  private String getRealmRDN(String realmName) {
    return realmEntryPattern.replace("{realm}", realmName);
  }

  private String getUserStorageDn(String userStorageName, String realmName) {
    return String.format("cn=%s,%s", userStorageName, getRealmDn(realmName));
  }
}
