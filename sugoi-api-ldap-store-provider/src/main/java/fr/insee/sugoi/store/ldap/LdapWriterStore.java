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
import fr.insee.sugoi.ldap.utils.mapper.properties.ApplicationLdap;
import fr.insee.sugoi.ldap.utils.mapper.properties.GroupLdap;
import fr.insee.sugoi.ldap.utils.mapper.properties.LdapObjectClass;
import fr.insee.sugoi.ldap.utils.mapper.properties.OrganizationLdap;
import fr.insee.sugoi.ldap.utils.mapper.properties.UserLdap;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import java.util.Map;

public class LdapWriterStore implements WriterStore {

  private LDAPConnectionPool ldapPoolConnection;
  private Map<String, String> config;

  private UserLdapMapper userLdapMapper;
  private OrganizationLdapMapper organizationLdapMapper;
  private GroupLdapMapper groupLdapMapper;
  private ApplicationLdapMapper applicationLdapMapper;

  private LdapReaderStore ldapReaderStore;

  public LdapWriterStore(Map<String, String> config) {
    try {
      this.ldapPoolConnection = LdapFactory.getConnectionPoolAuthenticated(config);
      this.config = config;
      userLdapMapper = new UserLdapMapper(config);
      organizationLdapMapper = new OrganizationLdapMapper(config);
      groupLdapMapper = new GroupLdapMapper(config);
      applicationLdapMapper = new ApplicationLdapMapper(config);
      ldapReaderStore = new LdapReaderStore(config);
    } catch (LDAPException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deleteUser(String id) {
    try {
      DeleteRequest dr =
          new DeleteRequest(
              String.format(
                  "%s=%s,%s",
                  UserLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
                  id,
                  config.get("user_source")));
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
              String.format(
                  "%s=%s,%s",
                  UserLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
                  user.getUsername(),
                  config.get("user_source")),
              userLdapMapper.mapToAttributes(user));
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
              String.format(
                  "%s=%s,%s",
                  UserLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
                  updatedUser.getUsername(),
                  config.get("user_source")),
              userLdapMapper.createMods(updatedUser));
      ldapPoolConnection.modify(mr);
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to update user while writing to LDAP", e);
    }
    return updatedUser;
  }

  @Override
  public void deleteGroup(String appName, String groupName) {
    try {
      if (ldapReaderStore.getGroup(appName, groupName) != null) {
        DeleteRequest dr =
            new DeleteRequest(
                String.format(
                    "%s=%s,%s",
                    GroupLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
                    groupName,
                    config.get("group_source_pattern").replace("{appliname}", appName)));
        ldapPoolConnection.delete(dr);
      } else {
        throw new RuntimeException(groupName + "is not a group");
      }
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to delete group " + groupName, e);
    }
  }

  @Override
  public Group createGroup(String appName, Group group) {
    try {

      AddRequest ar =
          new AddRequest(
              String.format(
                  "%s=%s,%s",
                  GroupLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
                  group.getName(),
                  config.get("group_source_pattern").replace("{appliname}", appName)),
              groupLdapMapper.mapToAttributes(group));
      ldapPoolConnection.add(ar);
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to create group " + group.getName(), e);
    }
    return group;
  }

  @Override
  public Group updateGroup(String appName, Group updatedGroup) {
    try {
      if (ldapReaderStore.getGroup(appName, updatedGroup.getName()) != null) {
        ModifyRequest mr =
            new ModifyRequest(
                String.format(
                    "%s=%s,%s",
                    GroupLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
                    updatedGroup.getName(),
                    config.get("group_source_pattern").replace("{appliname}", appName)),
                groupLdapMapper.createMods(updatedGroup));
        ldapPoolConnection.modify(mr);
      } else {
        throw new RuntimeException(updatedGroup.getName() + "is not a group");
      }
    } catch (LDAPException e) {
      throw new RuntimeException(
          "Failed to update group " + updatedGroup.getName() + " while writing to LDAP", e);
    }
    return updatedGroup;
  }

  @Override
  public void deleteOrganization(String name) {
    try {
      DeleteRequest dr =
          new DeleteRequest(
              String.format(
                  "%s=%s,%s",
                  OrganizationLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
                  name,
                  config.get("organization_source")));
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
              String.format(
                  "%s=%s,%s",
                  OrganizationLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
                  organization.getIdentifiant(),
                  config.get("organization_source")),
              organizationLdapMapper.mapToAttributes(organization));
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
              String.format(
                  "%s=%s,%s",
                  OrganizationLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
                  updatedOrganization.getIdentifiant(),
                  config.get("organization_source")),
              organizationLdapMapper.createMods(updatedOrganization));
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
            String.format(
                "%s=%s,%s",
                GroupLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
                groupName,
                config.get("group_source_pattern").replace("{appliname}", appName)),
            new Modification(
                ModificationType.DELETE,
                "uniqueMember",
                String.format(
                    "%s=%s,%s",
                    UserLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
                    userId,
                    config.get("user_source"))));
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
            String.format(
                "%s=%s,%s",
                GroupLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
                groupName,
                config.get("group_source_pattern").replace("{appliname}", appName)),
            new Modification(
                ModificationType.ADD,
                "uniqueMember",
                String.format(
                    "%s=%s,%s",
                    UserLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
                    userId,
                    config.get("user_source"))));
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
              String.format(
                  "%s=%s,%s",
                  ApplicationLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
                  application.getName(),
                  config.get("app_source")),
              applicationLdapMapper.mapToAttributes(application));
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
              String.format(
                  "%s=%s,%s",
                  ApplicationLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
                  updatedApplication.getName(),
                  config.get("app_source")),
              applicationLdapMapper.createMods(updatedApplication));
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
          new DeleteRequest(
              String.format(
                  "%s=%s,%s",
                  ApplicationLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
                  applicationName,
                  config.get("app_source")));
      ldapPoolConnection.delete(dr);

    } catch (LDAPException e) {
      throw new RuntimeException("Failed to delete application " + applicationName, e);
    }
  }
}
