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

import java.util.List;
import java.util.stream.Collectors;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import fr.insee.sugoi.core.configuration.RealmProvider;
import fr.insee.sugoi.core.exceptions.RealmNotFoundException;
import fr.insee.sugoi.ldap.utils.LdapFilter;
import fr.insee.sugoi.ldap.utils.mapper.RealmLdapMapper;
import fr.insee.sugoi.model.Realm;

@Component
/*
 * This class load the realm configuration from an ldap
 */
@ConditionalOnProperty(value = "fr.insee.sugoi.realm.config.type", havingValue = "ldap", matchIfMissing = false)
public class LdapRealmProviderDAOImpl implements RealmProvider {

  @Autowired
  private RealmLdapMapper realmMapper;

  @Value("${fr.insee.sugoi.config.ldap.profils.url:}")
  private String url;

  @Value("${fr.insee.sugoi.config.ldap.profils.port:}")
  private String port;

  @Value("${fr.insee.sugoi.config.ldap.profils.branche:}")
  private String baseDn;

  private static final Logger logger = LogManager.getLogger(LdapRealmProviderDAOImpl.class);

  @Override
  public Realm load(String realmName) {
    logger.info("Loading configuration from ldap://{}:{}/{}", url, port, baseDn);
    try (LDAPConnectionPool ldapConnection = new LDAPConnectionPool(new LDAPConnection(url, 389), 1)) {
      ;
      SearchResultEntry entry = ldapConnection.getEntry("cn=Profil_" + realmName + "_WebServiceLdap," + baseDn);
      logger.debug("Found entry {}", entry.getDN());
      Realm r = realmMapper.mapFromSearchEntry(entry);
      logger.debug("Parsing as realm {}", r);
      return r;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RealmNotFoundException("Erreur lors du chargement du realm " + realmName);
    }
  }

  @Override
  public List<Realm> findAll() {
    logger.info("Loading realms configurations from ldap://{}:{}/{}", url, port, baseDn);
    try (LDAPConnectionPool ldapConnection = new LDAPConnectionPool(new LDAPConnection(url, 389), 1)) {
      SearchRequest searchRequest = new SearchRequest(baseDn, SearchScope.ONE, LdapFilter.create("(objectClass=*)"),
          "*", "+");
      SearchResult searchResult = ldapConnection.search(searchRequest);
      return searchResult.getSearchEntries().stream().map(e -> realmMapper.mapFromSearchEntry(e))
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new RealmNotFoundException("Impossible de charger les realms");
    }
  }
}
