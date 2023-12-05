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

import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.model.RealmConfigKeys;
import java.util.HashMap;
import java.util.Map;

public class LdapFactory {

  private static final Map<String, RetriableLdapConnectionPool> openLdapPoolConnection =
      new HashMap<>();
  private static final Map<String, String> openLdapPoolConnectionConfig = new HashMap<>();
  private static final Map<String, String> openLdapMonoConnectionConfig = new HashMap<>();
  private static final Map<String, RetriableLdapConnection> openLdapMonoConnection =
      new HashMap<>();

  /**
   * Give an unauthenticated Ldap Connection Pool
   *
   * @param url
   * @return
   * @throws LDAPException
   */
  public static RetriableLdapConnectionPool getConnectionPool(Map<RealmConfigKeys, String> config)
      throws LDAPException {
    // Check if a ldap connection pool already exist for this userStorage and create
    // it if it doesn't exist
    String key =
        config.get(LdapConfigKeys.REALM_NAME)
            + "_"
            + config.get(LdapConfigKeys.USERSTORAGE_NAME)
            + "_"
            + config.hashCode()
            + "_R";
    String name =
        config.get(LdapConfigKeys.REALM_NAME)
            + "_"
            + config.get(LdapConfigKeys.USERSTORAGE_NAME)
            + "_R";

    if (!openLdapPoolConnectionConfig.containsKey(key)) {
      if (openLdapPoolConnection.containsKey(name)) {
        openLdapPoolConnection.get(name).close();
      }
      LDAPConnection ldapConnection =
          new LDAPConnection(
              config.get(LdapConfigKeys.URL), Integer.parseInt(config.get(LdapConfigKeys.PORT)));
      setConnectionTimeout(ldapConnection, config);
      openLdapPoolConnection.put(
          name,
          new RetriableLdapConnectionPool(
              ldapConnection,
              Integer.parseInt(config.get(LdapConfigKeys.POOL_SIZE)),
              Integer.parseInt(config.get(LdapConfigKeys.MAX_RETRIES))));
      // Only put key if ldap connection correctly open
      openLdapPoolConnectionConfig.put(key, name);
    }
    return openLdapPoolConnection.get(name);
  }

  public static RetriableLdapConnection getSingleConnection(Map<RealmConfigKeys, String> config)
      throws LDAPException {
    String key =
        config.get(LdapConfigKeys.REALM_NAME)
            + "_"
            + config.get(LdapConfigKeys.USERSTORAGE_NAME)
            + "_"
            + config.hashCode()
            + "_R";
    String name =
        config.get(LdapConfigKeys.REALM_NAME)
            + "_"
            + config.get(LdapConfigKeys.USERSTORAGE_NAME)
            + "_R";

    if (!openLdapMonoConnectionConfig.containsKey(key)) {
      if (openLdapMonoConnection.containsKey(name) && openLdapMonoConnection.get(name) != null) {
        openLdapMonoConnection.get(name).close();
      }
      openLdapMonoConnection.put(name, new RetriableLdapConnection(config));
      // Only put key if ldap connection correctly open
      openLdapMonoConnectionConfig.put(key, name);
    }
    return openLdapMonoConnection.get(name);
  }

  /**
   * Give a Ldap Connection Pool
   *
   * @param url
   * @return
   * @throws LDAPException
   */
  public static RetriableLdapConnectionPool getConnectionPoolAuthenticated(
      Map<RealmConfigKeys, String> config) throws LDAPException {
    // Check if a ldap connection pool already exist for this userStorage and create
    // it if it doesn't exist
    String name =
        config.get(LdapConfigKeys.REALM_NAME)
            + "_"
            + config.get(LdapConfigKeys.USERSTORAGE_NAME)
            + "_RW";
    String key =
        config.get(LdapConfigKeys.REALM_NAME)
            + "_"
            + config.get(LdapConfigKeys.USERSTORAGE_NAME)
            + "_"
            + config.hashCode()
            + "_RW";

    if (!openLdapPoolConnectionConfig.containsKey(key)) {
      if (openLdapPoolConnection.containsKey(name)) {
        openLdapPoolConnection.get(name).close();
      }
      LDAPConnection ldapConnection =
          new LDAPConnection(
              config.get(LdapConfigKeys.URL),
              Integer.parseInt(config.get(LdapConfigKeys.PORT)),
              config.get(LdapConfigKeys.USERNAME),
              config.get(LdapConfigKeys.PASSWORD));
      setConnectionTimeout(ldapConnection, config);
      openLdapPoolConnection.put(
          name,
          new RetriableLdapConnectionPool(
              ldapConnection,
              Integer.parseInt(config.get(LdapConfigKeys.POOL_SIZE)),
              Integer.parseInt(config.get(LdapConfigKeys.MAX_RETRIES))));
      // Only put key if ldap connection correctly open
      openLdapPoolConnectionConfig.put(key, name);
    }
    return openLdapPoolConnection.get(name);
  }

  public static RetriableLdapConnection getSingleConnectionAuthenticated(
      Map<RealmConfigKeys, String> config) throws LDAPException {
    String name =
        config.get(LdapConfigKeys.REALM_NAME)
            + "_"
            + config.get(LdapConfigKeys.USERSTORAGE_NAME)
            + "_RW";
    String key =
        config.get(LdapConfigKeys.REALM_NAME)
            + "_"
            + config.get(LdapConfigKeys.USERSTORAGE_NAME)
            + "_"
            + config.hashCode()
            + "_RW";
    if (!openLdapMonoConnectionConfig.containsKey(key)) {
      if (openLdapMonoConnection.containsKey(name) && openLdapMonoConnection.get(name) != null) {
        openLdapMonoConnection.get(name).close();
      }
      openLdapMonoConnection.put(name, new RetriableLdapConnection(config));
      // Only put key if ldap connection correctly open
      openLdapMonoConnectionConfig.put(key, name);
    }
    return openLdapMonoConnection.get(name);
  }

  public static boolean validateUserPassword(
      Map<RealmConfigKeys, String> config, String userdn, String password) throws LDAPException {
    try (LDAPConnection conn =
        new LDAPConnection(
            config.get(LdapConfigKeys.URL), Integer.valueOf(config.get(LdapConfigKeys.PORT)))) {
      BindResult result = conn.bind(userdn, password);
      if (result.getResultCode().intValue() != 0) {
        return false;
      }
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  private static void setConnectionTimeout(
      LDAPConnection connection, Map<RealmConfigKeys, String> config) {
    connection
        .getConnectionOptions()
        .setResponseTimeoutMillis(
            Integer.parseInt(config.get(LdapConfigKeys.LDAP_CONNECTION_TIMEOUT)));
  }
}
