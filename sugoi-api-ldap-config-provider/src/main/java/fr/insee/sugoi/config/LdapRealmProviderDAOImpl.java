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
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ModifyRequest;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.core.configuration.UiMappingService;
import fr.insee.sugoi.core.exceptions.LdapStoreConnectionFailedException;
import fr.insee.sugoi.core.exceptions.RealmAlreadyExistException;
import fr.insee.sugoi.core.exceptions.RealmNotFoundException;
import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.model.ProviderResponse.ProviderResponseStatus;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.ldap.utils.LdapFactory;
import fr.insee.sugoi.ldap.utils.LdapFilter;
import fr.insee.sugoi.ldap.utils.LdapUtils;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.ldap.utils.mapper.RealmLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.UserStorageLdapMapper;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.Realm.UIMappingType;
import fr.insee.sugoi.model.UserStorage;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  @Value("${fr.insee.sugoi.store.defaultReader:}")
  private String defaultReader;

  @Value("${fr.insee.sugoi.store.defaultWriter:}")
  private String defaultWriter;

  @Value("${fr.insee.sugoi.config.ldap.profils.pattern:cn=Profil_{realm}_WebServiceLdap}")
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

  @Override
  public Optional<Realm> load(String realmName) {
    logger.info("Loading configuration from ldap://{}:{}/{}", url, port, baseDn);
    try {
      SearchResultEntry realmEntry =
          ldapPoolConnection()
              .getEntry(realmEntryPattern.replace("{realm}", realmName) + "," + baseDn);
      if (realmEntry != null) {
        logger.debug("Found entry {}", realmEntry.getDN());
        Realm realm = RealmLdapMapper.mapFromSearchEntry(realmEntry);
        if (realm.getReaderType() == null) {
          realm.setReaderType(defaultReader);
        }
        if (realm.getWriterType() == null) {
          realm.setWriterType(defaultWriter);
        }
        logger.debug("Parsing as realm {}", realm);
        realm.setUserStorages(loadUserStorages(realmEntry));
        if (realm.getProperties().get(GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_KEYS_LIST) == null) {
          realm.addProperty(
              GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_KEYS_LIST, defaultAppManagedAttributeKeyList);
        }
        if (realm.getProperties().get(GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_PATTERNS_LIST)
            == null) {
          realm.addProperty(
              GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_PATTERNS_LIST,
              defaultAppManagedAttributePatternList);
        }
        realm
            .getUiMapping()
            .putIfAbsent(UIMappingType.UI_USER_MAPPING, uiMappingService.getUserUiDefaultField());
        realm
            .getUiMapping()
            .putIfAbsent(
                UIMappingType.UI_ORGANIZATION_MAPPING,
                uiMappingService.getOrganizationUiDefaultField());
        sortUiLists(realm);
        realm.getProperties().putIfAbsent(LdapConfigKeys.SORT_KEY, defaultSortKey);
        return Optional.of(realm);
      }
      return Optional.empty();
    } catch (Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  @Override
  public List<Realm> findAll() {
    logger.info("Loading realms configurations from ldap://{}:{}/{}", url, port, baseDn);
    try {
      SearchRequest searchRequest =
          new SearchRequest(
              baseDn, SearchScope.ONE, LdapFilter.create("(objectClass=*)"), "*", "+");
      SearchResult searchResult = ldapPoolConnection().search(searchRequest);
      return searchResult.getSearchEntries().stream()
          .map(
              e -> {
                Realm realm = RealmLdapMapper.mapFromSearchEntry(e);
                if (realm.getReaderType() == null) {
                  realm.setReaderType(defaultReader);
                }
                if (realm.getWriterType() == null) {
                  realm.setWriterType(defaultWriter);
                }
                if (realm.getProperties().get(GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_KEYS_LIST)
                    == null) {
                  realm.addProperty(
                      GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_KEYS_LIST,
                      defaultAppManagedAttributeKeyList);
                }
                if (realm.getProperties().get(GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_PATTERNS_LIST)
                    == null) {
                  realm.addProperty(
                      GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_PATTERNS_LIST,
                      defaultAppManagedAttributePatternList);
                }
                realm.setUserStorages(loadUserStorages(e));
                realm
                    .getUiMapping()
                    .putIfAbsent(
                        UIMappingType.UI_USER_MAPPING, uiMappingService.getUserUiDefaultField());
                realm
                    .getUiMapping()
                    .putIfAbsent(
                        UIMappingType.UI_ORGANIZATION_MAPPING,
                        uiMappingService.getOrganizationUiDefaultField());
                sortUiLists(realm);
                return realm;
              })
          .collect(Collectors.toList());
    } catch (Exception e) {
      e.printStackTrace();
      throw new RealmNotFoundException("Impossible de charger les realms", e);
    }
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

  private List<UserStorage> loadUserStorages(SearchResultEntry realmEntry) {
    try {
      String baseName = realmEntry.getAttribute("cn").getValue();
      SearchResult userStoragesResult =
          ldapPoolConnection()
              .search(
                  "cn=" + baseName + "," + baseDn,
                  SearchScope.SUBORDINATE_SUBTREE,
                  Filter.create("objectClass=*"));
      if (userStoragesResult.getEntryCount() > 0) {
        return userStoragesResult.getSearchEntries().stream()
            .map(
                searchEntry -> UserStorageLdapMapper.mapFromAttributes(searchEntry.getAttributes()))
            .collect(Collectors.toList());
      } else {
        return List.of(UserStorageLdapMapper.mapFromAttributes(realmEntry.getAttributes()));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
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
        if (realm.getUserStorages().size() == 1) {
          UserStorageLdapMapper.mapToAttributes(realm.getUserStorages().get(0)).stream()
              .forEach(attribute -> addRequest.addAttribute(attribute));
          ldapConnectionPoolAuthenticated().add(addRequest);
        } else {
          ldapConnectionPoolAuthenticated().add(addRequest);
          for (UserStorage userStorage : realm.getUserStorages()) {
            AddRequest userStorageAddRequest =
                new AddRequest(
                    String.format("cn=%s,%s", userStorage.getName(), getRealmDn(realm.getName())),
                    UserStorageLdapMapper.mapToAttributes(userStorage));
            ldapConnectionPoolAuthenticated().add(userStorageAddRequest);
          }
        }
        ProviderResponse response = new ProviderResponse();
        response.setStatus(ProviderResponseStatus.OK);
        response.setEntityId(realm.getName());
        return response;
      } catch (LDAPException e) {
        throw new RuntimeException("Failed to create realm " + realm.getName(), e);
      }
    }
    throw new RealmAlreadyExistException(String.format("Realm %s already exist", realm.getName()));
  }

  @Override
  public ProviderResponse updateRealm(Realm realm, ProviderRequest providerRequest) {
    if (load(realm.getName()).isPresent()) {
      try {
        ModifyRequest modifyRequest;
        if (realm.getUserStorages().size() == 1) {
          List<Attribute> realmAttributes =
              RealmLdapMapper.mapToAttributes(realm, realmEntryPattern, baseDn);
          List<Attribute> userStorageAttributes =
              UserStorageLdapMapper.mapToAttributes(realm.getUserStorages().get(0));
          realmAttributes.addAll(userStorageAttributes);
          modifyRequest =
              new ModifyRequest(
                  getRealmDn(realm.getName()),
                  LdapUtils.convertAttributesToModifications(realmAttributes));
          ldapConnectionPoolAuthenticated().modify(modifyRequest);
        } else {
          for (UserStorage userStorage : realm.getUserStorages()) {
            ModifyRequest userStorageModifyRequest =
                new ModifyRequest(
                    getUserStorageDn(userStorage.getName(), realm.getName()),
                    UserStorageLdapMapper.createMods(userStorage));
            ldapConnectionPoolAuthenticated().modify(userStorageModifyRequest);
          }
          modifyRequest =
              new ModifyRequest(
                  getRealmDn(realm.getName()),
                  RealmLdapMapper.createMods(realm, realmEntryPattern, baseDn));
        }
        ldapConnectionPoolAuthenticated().modify(modifyRequest);
        ProviderResponse response = new ProviderResponse();
        response.setStatus(ProviderResponseStatus.OK);
        response.setEntityId(realm.getName());
        return response;
      } catch (LDAPException e) {
        throw new RuntimeException("Failed to update realm " + realm.getName(), e);
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
      throw new RuntimeException("Failed to delete realm " + realmName, e);
    }
  }

  private LDAPConnectionPool ldapPoolConnection() {
    try {
      if (ldapConnectionPool == null) {
        if (useAuthenticatedConnectionForReading) {
          ldapConnectionPool = ldapConnectionPoolAuthenticated();
        } else {
          Map<String, String> config = new HashMap<>();
          config.put(LdapConfigKeys.URL, url);
          config.put(LdapConfigKeys.PORT, String.valueOf(port));
          config.put(LdapConfigKeys.POOL_SIZE, defaultPoolSize);
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
        Map<String, String> config = new HashMap<>();
        config.put(LdapConfigKeys.URL, url);
        config.put(LdapConfigKeys.PORT, String.valueOf(port));
        config.put(LdapConfigKeys.POOL_SIZE, defaultPoolSize);
        config.put(LdapConfigKeys.USERNAME, defaultUsername);
        config.put(LdapConfigKeys.PASSWORD, defaultPassword);
        ldapConnectionPool = LdapFactory.getConnectionPoolAuthenticated(config);
      }
      return ldapConnectionPool;
    } catch (LDAPException e) {
      throw new LdapStoreConnectionFailedException(
          String.format("Failed authenticated connection to ldap realm store %s:%d", url, port), e);
    }
  }

  private String getRealmDn(String realmName) {
    return realmEntryPattern.replace("{realm}", realmName) + "," + baseDn;
  }

  private String getUserStorageDn(String userStorageName, String realmName) {
    return String.format("cn=%s,%s", userStorageName, getRealmDn(realmName));
  }
}
