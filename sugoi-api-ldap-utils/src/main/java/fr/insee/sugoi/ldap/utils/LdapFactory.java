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

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LdapFactory {

  // private static final Logger logger = LogManager.getLogger(LdapUtils.class);

  private static final Map<String, LDAPConnectionPool> openLdapPoolConnection = new HashMap<>();
  private static final Map<String, LDAPConnection> openLdapMonoConnection = new HashMap<>();

  /**
   * Give a Ldap Connection Pool
   *
   * @param url
   * @return
   * @throws LDAPException
   */
  public static LDAPConnectionPool getConnectionPool(Map<String, String> config)
      throws LDAPException {
    // Check if a ldap connection pool already exist for this userStorage and create
    // it if it doesn't exist
    String key = config.get("realm_name") + "_" + config.get("name");
    if (!openLdapPoolConnection.containsKey(key)) {
      openLdapPoolConnection.put(
          key,
          new LDAPConnectionPool(
              new LDAPConnection(config.get("url"), Integer.valueOf(config.get("port"))),
              Integer.valueOf(config.get("pool_size"))));
    }
    return openLdapPoolConnection.get(key);
  }

  public static LDAPConnection getSingleConnection(Map<String, String> config)
      throws LDAPException {
    String key = config.get("realm_name") + "_" + config.get("name");
    if (!openLdapMonoConnection.containsKey(key)) {
      openLdapMonoConnection.put(
          key, new LDAPConnection(config.get("url"), Integer.valueOf(config.get("port"))));
    }
    return openLdapMonoConnection.get(key);
  }
}
