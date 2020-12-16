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
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.ldap.utils.LdapFactory;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import java.util.Map;

public class LdapWriterStore implements WriterStore {

  private LDAPConnectionPool ldapPoolConnection;
  private Map<String, String> config;

  public LdapWriterStore(Map<String, String> config) {
    try {
      LdapFactory.getSingleConnection(config);
      this.ldapPoolConnection = LdapFactory.getConnectionPool(config);
      new LdapReaderStore(config);
      this.config = config;
    } catch (LDAPException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deleteUser(String id) {
    try {
      DeleteRequest dr = new DeleteRequest("uid=" + id + "," + config.get("user_source"));
      ldapPoolConnection.delete(dr);
    } catch (LDAPException e) {
      throw new RuntimeException("Impossible de supprimer l'utilisateur");
    }
  }

  @Override
  public User createUser(User user) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public User updateUser(User updatedUser) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteGroup(String name) {
    // TODO Auto-generated method stub

  }

  @Override
  public Group createGroup(Group group) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Group updateGroup(Group updatedGroup) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteOrganization(String name) {
    // TODO Auto-generated method stub

  }

  @Override
  public Organization createOrganization(Organization organization) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Organization updateOrganization(Organization updatedOrganization) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteUserFromGroup(String groupName, String userId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addUserToGroup(String groupName, String userId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void reinitPassword(User user) {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPassword(User user, String password) {
    // TODO Auto-generated method stub

  }

  @Override
  public void changePasswordResetStatus(User user, boolean isReset) {
    // TODO Auto-generated method stub

  }
}
