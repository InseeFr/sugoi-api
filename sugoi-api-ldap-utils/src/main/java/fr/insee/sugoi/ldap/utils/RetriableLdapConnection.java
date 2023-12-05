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
package fr.insee.sugoi.ldap.utils;

import com.unboundid.ldap.sdk.*;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.model.RealmConfigKeys;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetriableLdapConnection {

  private final int maxRetries;
  private LDAPConnection ldapConnection;
  Map<RealmConfigKeys, String> config;
  public static final Logger logger = LoggerFactory.getLogger(RetriableLdapConnectionPool.class);

  public RetriableLdapConnection(Map<RealmConfigKeys, String> config) throws LDAPException {
    this.config = config;
    this.ldapConnection = createLdapConnectionFromConfig();
    this.maxRetries = Integer.parseInt(config.get(LdapConfigKeys.MAX_RETRIES));
  }

  private LDAPConnection createLdapConnectionFromConfig() throws LDAPException {
    LDAPConnection ldapConnection =
        new LDAPConnection(
            config.get(LdapConfigKeys.URL),
            Integer.parseInt(config.get(LdapConfigKeys.PORT)),
            config.get(LdapConfigKeys.USERNAME),
            config.get(LdapConfigKeys.PASSWORD));
    ldapConnection
        .getConnectionOptions()
        .setResponseTimeoutMillis(
            Integer.parseInt(config.get(LdapConfigKeys.LDAP_CONNECTION_TIMEOUT)));
    return ldapConnection;
  }

  public SearchResult search(SearchRequest searchRequest)
      throws RetriableLDAPException, LDAPException {
    int retryCount = 0;
    while (retryCount < maxRetries) {
      try {
        return ldapConnection.search(searchRequest);
      } catch (LDAPException e) {
        ldapConnection = createLdapConnectionFromConfig();
        logger.info("Failed to connect to ldap after " + retryCount + " try : " + e.getMessage());
        retryCount++;
      }
    }
    throw new RetriableLDAPException("Failed to retrieve entry after " + maxRetries + " retries.");
  }

  public void close() {
    ldapConnection.close();
  }
}
