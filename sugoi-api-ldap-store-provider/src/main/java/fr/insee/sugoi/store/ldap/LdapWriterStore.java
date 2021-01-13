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

import com.unboundid.ldap.sdk.AddRequest;
import com.unboundid.ldap.sdk.DeleteRequest;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.ModifyRequest;
import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.ldap.utils.LdapFactory;
import fr.insee.sugoi.ldap.utils.mapper.ApplicationLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.GroupLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.OrganizationLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.UserLdapMapper;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import java.util.Map;

public class LdapWriterStore implements WriterStore {

  private LDAPConnectionPool ldapPoolConnection;
  private Map<String, String> config;

  private UserLdapMapper userLdapMapper = new UserLdapMapper();
  private OrganizationLdapMapper organizationLdapMapper = new OrganizationLdapMapper();
  private GroupLdapMapper groupLdapMapper = new GroupLdapMapper();
  private ApplicationLdapMapper applicationLdapMapper = new ApplicationLdapMapper();

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
      throw new RuntimeException("Failed to delete user " + id, e);
    }
  }

  @Override
  public User createUser(User user) {
    try {
      AddRequest ar =
          new AddRequest(
              "uid=" + user.getUsername() + "," + config.get("user_source"),
              userLdapMapper.mapToAttribute(user));
      ldapPoolConnection.add(ar);
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to create user", e);
    }
    return user;
  }

  @Override
  public User updateUser(User updatedUser) {
    try {
      ModifyRequest mr =
          new ModifyRequest(
              "uid=" + updatedUser.getUsername() + "," + config.get("user_source"),
              UserLdapMapper.createMods(updatedUser));
      ldapPoolConnection.modify(mr);
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to update user while writing to LDAP", e);
    }
    return updatedUser;
  }

  @Override
  public void deleteGroup(String appName, String groupName) {
    try {
      DeleteRequest dr =
          new DeleteRequest("cn=" + groupName + ",ou=" + appName + "," + config.get("app_source"));
      ldapPoolConnection.delete(dr);
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to delete group " + groupName, e);
    }
  }

  @Override
  public Group createGroup(String appName, Group group) {
    try {
      AddRequest ar =
          new AddRequest(
              "cn=" + group.getName() + ",ou=" + appName + "," + config.get("app_source"),
              groupLdapMapper.mapToAttribute(group));
      ldapPoolConnection.add(ar);
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to create group " + group.getName(), e);
    }
    return group;
  }

  @Override
  public Group updateGroup(String appName, Group updatedGroup) {
    try {
      ModifyRequest mr =
          new ModifyRequest(
              "cn=" + updatedGroup.getName() + ",ou=" + appName + "," + config.get("app_source"),
              GroupLdapMapper.createMods(updatedGroup));
      ldapPoolConnection.modify(mr);
    } catch (LDAPException e) {
      throw new RuntimeException(
          "Failed to update group " + updatedGroup.getName() + " while writing to LDAP", e);
    }
    return updatedGroup;
  }

  @Override
  public void deleteOrganization(String name) {
    try {
      DeleteRequest dr = new DeleteRequest("uid=" + name + "," + config.get("organization_source"));
      ldapPoolConnection.delete(dr);
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to delete organisation " + name, e);
    }
  }

  @Override
  public Organization createOrganization(Organization organization) {
    try {
      AddRequest ar =
          new AddRequest(
              "uid=" + organization.getIdentifiant() + "," + config.get("organization_source"),
              organizationLdapMapper.mapToAttribute(organization));
      ldapPoolConnection.add(ar);
    } catch (LDAPException e) {
      throw new RuntimeException(
          "Failed to create organization " + organization.getIdentifiant(), e);
    }
    return organization;
  }

  @Override
  public Organization updateOrganization(Organization updatedOrganization) {
    try {
      ModifyRequest mr =
          new ModifyRequest(
              "uid="
                  + updatedOrganization.getIdentifiant()
                  + ","
                  + config.get("organization_source"),
              OrganizationLdapMapper.createMods(updatedOrganization));
      ldapPoolConnection.modify(mr);
    } catch (LDAPException e) {
      throw new RuntimeException(
          "Failed to update organization "
              + updatedOrganization.getIdentifiant()
              + "while writing to LDAP",
          e);
    }
    return updatedOrganization;
  }

  @Override
  public void deleteUserFromGroup(String appName, String groupName, String userId) {
    ModifyRequest mr =
        new ModifyRequest(
            "cn=" + groupName + ",ou=" + appName + "," + config.get("app_source"),
            new Modification(
                ModificationType.DELETE,
                "uniqueMember",
                "uid=" + userId + ",ou=" + appName + "," + config.get("app_source")));
    try {
      ldapPoolConnection.modify(mr);
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to delete user from group " + groupName, e);
    }
  }

  @Override
  public void addUserToGroup(String appName, String groupName, String userId) {
    ModifyRequest mr =
        new ModifyRequest(
            "cn=" + groupName + ",ou=" + appName + "," + config.get("app_source"),
            new Modification(
                ModificationType.ADD,
                "uniqueMember",
                "uid=" + userId + ",ou=" + appName + "," + config.get("app_source")));
    try {
      ldapPoolConnection.modify(mr);
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to add user to group " + groupName, e);
    }
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

  @Override
  public Application createApplication(Application application) {
    try {
      AddRequest ar =
          new AddRequest(
              "ou=" + application.getName() + "," + config.get("app_source"),
              applicationLdapMapper.mapToAttribute(application));
      ldapPoolConnection.add(ar);
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to create application" + application.getName(), e);
    }
    return application;
  }

  @Override
  public Application updateApplication(Application updatedApplication) {
    try {
      ModifyRequest mr =
          new ModifyRequest(
              "ou=" + updatedApplication.getName() + "," + config.get("app_source"),
              ApplicationLdapMapper.createMods(updatedApplication));
      ldapPoolConnection.modify(mr);
    } catch (LDAPException e) {
      throw new RuntimeException(
          "Failed to update application " + updatedApplication.getName() + "while writing to LDAP",
          e);
    }
    return updatedApplication;
  }

  @Override
  public void deleteApplication(String applicationName) {
    try {
      // the application must be empty to be deleted
      DeleteRequest dr =
          new DeleteRequest("ou=" + applicationName + "," + config.get("app_source"));
      ldapPoolConnection.delete(dr);

    } catch (LDAPException e) {
      throw new RuntimeException("Failed to delete application " + applicationName, e);
    }
  }
}
