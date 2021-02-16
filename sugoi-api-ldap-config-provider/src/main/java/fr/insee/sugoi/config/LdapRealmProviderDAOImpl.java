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

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import fr.insee.sugoi.core.exceptions.RealmNotFoundException;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.ldap.utils.LdapFilter;
import fr.insee.sugoi.ldap.utils.mapper.RealmLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.UserStorageLdapMapper;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

  @Value("${fr.insee.sugoi.config.ldap.profils.url:}")
  private String url;

  @Value("${fr.insee.sugoi.config.ldap.profils.port:}")
  private int port;

  @Value("${fr.insee.sugoi.config.ldap.profils.branche:}")
  private String baseDn;

  @Value("${fr.insee.sugoi.config.ldap.profils.pattern:cn=Profil_{realm}_WebServiceLdap}")
  private String realmEntryPattern;

  private static final Logger logger = LogManager.getLogger(LdapRealmProviderDAOImpl.class);

  @Override
  public Realm load(String realmName) {
    logger.info("Loading configuration from ldap://{}:{}/{}", url, port, baseDn);
    try (LDAPConnectionPool ldapConnection =
        new LDAPConnectionPool(new LDAPConnection(url, port), 1)) {
      SearchResultEntry realmEntry =
          ldapConnection.getEntry(realmEntryPattern.replace("{realm}", realmName) + "," + baseDn);
      logger.debug("Found entry {}", realmEntry.getDN());
      Realm realm = RealmLdapMapper.mapFromSearchEntry(realmEntry);
      logger.debug("Parsing as realm {}", realm);
      realm.setUserStorages(loadUserStorages(realmEntry, ldapConnection));
      return realm;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RealmNotFoundException("Erreur lors du chargement du realm " + realmName);
    }
  }

  @Override
  public List<Realm> findAll() {
    logger.info("Loading realms configurations from ldap://{}:{}/{}", url, port, baseDn);
    try (LDAPConnectionPool ldapConnection =
        new LDAPConnectionPool(new LDAPConnection(url, port), 1)) {
      SearchRequest searchRequest =
          new SearchRequest(
              baseDn, SearchScope.ONE, LdapFilter.create("(objectClass=*)"), "*", "+");
      SearchResult searchResult = ldapConnection.search(searchRequest);
      return searchResult.getSearchEntries().stream()
          .map(
              e -> {
                Realm realm = RealmLdapMapper.mapFromSearchEntry(e);
                realm.setUserStorages(loadUserStorages(e, ldapConnection));
                return realm;
              })
          .collect(Collectors.toList());
    } catch (Exception e) {
      e.printStackTrace();
      throw new RealmNotFoundException("Impossible de charger les realms");
    }
  }

  private List<UserStorage> loadUserStorages(
      SearchResultEntry realmEntry, LDAPConnectionPool ldapConnection) {
    try {
      String baseName = realmEntry.getAttribute("cn").getValue();
      SearchResult userStoragesResult =
          ldapConnection.search(
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
}
