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

import com.unboundid.ldap.sdk.DeleteRequest;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.ldap.utils.LdapFactory;
import fr.insee.sugoi.model.User;
import java.util.Map;

public class LdapWriterStore implements WriterStore {

  private LDAPConnection ldapConnection;
  private LDAPConnectionPool ldapPoolConnection;
  private LdapReaderStore ldapReader;

  private Map<String, String> config;

  public LdapWriterStore(Map<String, String> config) {
    try {
      this.ldapConnection = LdapFactory.getSingleConnection(config);
      this.ldapPoolConnection = LdapFactory.getConnectionPool(config);
      this.ldapReader = new LdapReaderStore(config);
      this.config = config;
    } catch (LDAPException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String deleteUser(String domain, String id) {
    try {
      DeleteRequest dr = new DeleteRequest("uid=" + id + "," + config.get("user_branch"));
      ldapPoolConnection.delete(dr);
    } catch (LDAPException e) {
      throw new RuntimeException("Impossible de supprimer l'utilisateur");
    }
    return null;
  }

  @Override
  public User createUser(User user) {
    // TODO Auto-generated method stub
    return null;
  }
}
