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
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.DeleteRequest;
import com.unboundid.ldap.sdk.ExtendedResult;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.ModifyRequest;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.extensions.PasswordModifyExtendedRequest;
import com.unboundid.util.SubtreeDeleter;
import fr.insee.sugoi.core.exceptions.AppManagedAttributeException;
import fr.insee.sugoi.core.exceptions.InvalidPasswordException;
import fr.insee.sugoi.core.exceptions.StoragePolicyNotMetException;
import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.ldap.utils.LdapFactory;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.ldap.utils.mapper.AddressLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.ApplicationLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.GroupLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.OrganizationLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.UserLdapMapper;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.paging.PasswordChangeRequest;
import fr.insee.sugoi.model.paging.SendMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class LdapWriterStore extends LdapStore implements WriterStore {

  private LDAPConnectionPool ldapPoolConnection;

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

  /** Delete a user and its address */
  @Override
  public void deleteUser(String id) {
    try {
      User currentUser = ldapReaderStore.getUser(id);
      currentUser
          .getGroups()
          .forEach(group -> deleteUserFromGroup(group.getAppName(), group.getName(), id));
      if (currentUser.getAddress().get("id") != null) {
        deleteAddress(currentUser.getAddress().get("id"));
      }
      DeleteRequest dr = new DeleteRequest(getUserDN(id));
      ldapPoolConnection.delete(dr);
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to delete user " + id, e);
    }
  }

  /**
   * Create a user in ldap. If the user has an address, a ldap resource address is generated with a
   * random value as id An organization link can be created but may not exist
   */
  @Override
  public User createUser(User user) {
    try {
      if (user.getAddress() != null && user.getAddress().size() > 0) {
        UUID addressUuid = createAddress(user.getAddress());
        user.addAddress("id", addressUuid.toString());
      }
      AddRequest userAddRequest =
          new AddRequest(getUserDN(user.getUsername()), userLdapMapper.mapToAttributes(user));
      ldapPoolConnection.add(userAddRequest);
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to create user", e);
    }
    return user;
  }

  /** Update the ldap properties of a user. Current user is read to retrieve user address link */
  @Override
  public User updateUser(User updatedUser) {
    try {
      User currentUser = ldapReaderStore.getUser(updatedUser.getUsername());
      if (updatedUser != null) {
        if (updatedUser.getAddress() != null && updatedUser.getAddress().size() > 0) {
          if (currentUser.getAddress() != null && currentUser.getAddress().containsKey("id")) {
            updateAddress(currentUser.getAddress().get("id"), updatedUser.getAddress());
          } else {
            Map<String, String> newAddress = new HashMap<>();
            newAddress.put("id", createAddress(updatedUser.getAddress()).toString());
            updatedUser.setAddress(newAddress);
            createAddress(updatedUser.getAddress());
          }
        }
        ModifyRequest mr =
            new ModifyRequest(
                getUserDN(updatedUser.getUsername()), userLdapMapper.createMods(updatedUser));
        ldapPoolConnection.modify(mr);
      }
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to update user while writing to LDAP", e);
    }
    return updatedUser;
  }

  @Override
  public void deleteGroup(String appName, String groupName) {
    try {
      if (ldapReaderStore.getGroup(appName, groupName) != null) {
        DeleteRequest dr = new DeleteRequest(getGroupDN(appName, groupName));
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
      if (matchGroupWildcardPattern(appName, group.getName())) {
        // check if parent entry exist
        if (ldapPoolConnection.getEntry(getGroupSource(appName)) == null) {
          AddRequest groupsAR =
              new AddRequest(
                  getGroupSource(appName),
                  new Attribute("objectClass", "top", "organizationalUnit"));
          ldapPoolConnection.add(groupsAR);
        }
        AddRequest ar =
            new AddRequest(
                getGroupDN(appName, group.getName()), groupLdapMapper.mapToAttributes(group));
        ldapPoolConnection.add(ar);
      } else {
        throw new StoragePolicyNotMetException("Group pattern won't match");
      }
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
                getGroupDN(appName, updatedGroup.getName()),
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

  /** Delete an organization and its address */
  @Override
  public void deleteOrganization(String name) {
    try {
      Organization currentOrganization = ldapReaderStore.getOrganization(name);
      if (currentOrganization.getAddress().get("id") != null) {
        deleteAddress(currentOrganization.getAddress().get("id"));
      }
      DeleteRequest dr = new DeleteRequest(getOrganizationDN(name));
      ldapPoolConnection.delete(dr);
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to delete organisation " + name, e);
    }
  }

  /**
   * Create an organization in ldap. If the organization has an address, a ldap resource address is
   * generated with a random value as id An organization link can be created but may not exist
   */
  @Override
  public Organization createOrganization(Organization organization) {
    try {
      if (organization.getAddress() != null && organization.getAddress().size() > 0) {
        UUID addressUuid = createAddress(organization.getAddress());
        organization.addAddress("id", addressUuid.toString());
      }
      AddRequest ar =
          new AddRequest(
              getOrganizationDN(organization.getIdentifiant()),
              organizationLdapMapper.mapToAttributes(organization));
      ldapPoolConnection.add(ar);
    } catch (LDAPException e) {
      throw new RuntimeException(
          "Failed to create organization " + organization.getIdentifiant(), e);
    }
    return organization;
  }

  /**
   * Update the ldap properties of a organization. Current organization is read to retrieve address
   * link
   */
  @Override
  public Organization updateOrganization(Organization updatedOrganization) {
    try {
      Organization currentOrganization =
          ldapReaderStore.getOrganization(updatedOrganization.getIdentifiant());
      if (updatedOrganization.getAddress() != null && updatedOrganization.getAddress().size() > 0) {
        if (currentOrganization.getAddress().containsKey("id")) {
          updateAddress(
              currentOrganization.getAddress().get("id"), updatedOrganization.getAddress());
        } else {
          Map<String, String> newAddress = new HashMap<>();
          newAddress.put("id", createAddress(updatedOrganization.getAddress()).toString());
          updatedOrganization.setAddress(newAddress);
        }
      }
      ModifyRequest mr =
          new ModifyRequest(
              getOrganizationDN(updatedOrganization.getIdentifiant()),
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
    try {
      ModifyRequest mr =
          new ModifyRequest(
              getGroupDN(appName, groupName),
              new Modification(ModificationType.DELETE, "uniqueMember", getUserDN(userId)));
      ldapPoolConnection.modify(mr);
    } catch (LDAPException e) {
      if (!e.getResultCode().equals(ResultCode.NO_SUCH_ATTRIBUTE)) {
        throw new RuntimeException("Failed to add user to group " + groupName, e);
      }
    }
  }

  @Override
  public void addUserToGroup(String appName, String groupName, String userId) {
    try {
      ModifyRequest mr =
          new ModifyRequest(
              getGroupDN(appName, groupName),
              new Modification(ModificationType.ADD, "uniqueMember", getUserDN(userId)));
      ldapPoolConnection.modify(mr);
    } catch (LDAPException e) {
      if (!e.getResultCode().equals(ResultCode.ATTRIBUTE_OR_VALUE_EXISTS)) {
        throw new RuntimeException("Failed to remove user to group " + groupName, e);
      }
    }
  }

  @Override
  public void reinitPassword(
      User user, String generatedPassword, PasswordChangeRequest pcr, List<SendMode> sendMode) {
    Modification mod =
        new Modification(ModificationType.REPLACE, "userPassword", generatedPassword);
    try {
      ldapPoolConnection.modify(
          "uid=" + user.getUsername() + "," + config.get(LdapConfigKeys.USER_SOURCE), mod);
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to reinit password for user " + user.getUsername(), e);
    }
  }

  @Override
  public void initPassword(
      User user, String password, PasswordChangeRequest pcr, List<SendMode> sendMode) {
    Modification mod = new Modification(ModificationType.REPLACE, "userPassword", password);
    try {
      ldapPoolConnection.modify(
          "uid=" + user.getUsername() + "," + config.get(LdapConfigKeys.USER_SOURCE), mod);
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to init password for user " + user.getUsername(), e);
    }
  }

  @Override
  public void changePassword(
      User user, String oldPassword, String newPassword, PasswordChangeRequest pcr) {
    try {
      PasswordModifyExtendedRequest pmer =
          new PasswordModifyExtendedRequest(
              "uid=" + user.getUsername() + "," + config.get(LdapConfigKeys.USER_SOURCE),
              oldPassword,
              newPassword);
      ExtendedResult result = ldapPoolConnection.processExtendedOperation(pmer);
      if (result.getResultCode().intValue() == ResultCode.INVALID_CREDENTIALS_INT_VALUE) {
        throw new InvalidPasswordException("Old password is not correct");
      } else if (result.getResultCode().intValue() != 0) {
        throw new RuntimeException("Unexpected error when changing password");
      }
    } catch (NumberFormatException | LDAPException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void changePasswordResetStatus(User user, boolean isReset) {
    Modification mod =
        new Modification(
            ModificationType.REPLACE, "pwdReset", Boolean.toString(isReset).toUpperCase());
    try {
      ldapPoolConnection.modify(
          "uid=" + user.getUsername() + "," + config.get(LdapConfigKeys.USER_SOURCE), mod);
    } catch (LDAPException e) {
      e.printStackTrace();
    }
  }

  /** Create a ldap ressource application and all the depending groups */
  @Override
  public Application createApplication(Application application) {
    try {
      AddRequest ar =
          new AddRequest(
              getApplicationDN(application.getName()),
              applicationLdapMapper.mapToAttributes(application));
      ldapPoolConnection.add(ar);
      AddRequest groupsAR =
          new AddRequest(
              getGroupSource(application.getName()),
              new Attribute("objectClass", "top", "organizationalUnit"));
      ldapPoolConnection.add(groupsAR);
      application.getGroups().stream().forEach(group -> createGroup(application.getName(), group));
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
              getApplicationDN(updatedApplication.getName()),
              applicationLdapMapper.createMods(updatedApplication));
      ldapPoolConnection.modify(mr);
      List<Group> alreadyExistingGroups =
          ldapReaderStore.getApplication(updatedApplication.getName()).getGroups();
      for (Group existingGroup : alreadyExistingGroups) {
        Optional<Group> optionalGroup =
            updatedApplication.getGroups().stream()
                .filter(group -> group.getName().equalsIgnoreCase(existingGroup.getName()))
                .findFirst();
        if (optionalGroup.isPresent()) {
          updateGroup(updatedApplication.getName(), optionalGroup.get());
        } else {
          deleteGroup(updatedApplication.getName(), existingGroup.getName());
        }
      }
      for (Group updatedGroup : updatedApplication.getGroups()) {
        if (alreadyExistingGroups.stream()
            .allMatch(group -> !group.getName().equalsIgnoreCase(updatedGroup.getName())))
          createGroup(updatedApplication.getName(), updatedGroup);
      }

    } catch (LDAPException e) {
      throw new RuntimeException(
          "Failed to update application " + updatedApplication.getName() + "while writing to LDAP",
          e);
    }
    return updatedApplication;
  }

  /** Delete application branch */
  @Override
  public void deleteApplication(String applicationName) {
    try {
      // DeleteRequest dr = new DeleteRequest(getApplicationDN(applicationName));
      // ldapPoolConnection.delete(dr);
      (new SubtreeDeleter()).delete(ldapPoolConnection, getApplicationDN(applicationName));
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to delete application " + applicationName, e);
    }
  }

  /**
   * create an ldap object address with a random id
   *
   * @param address
   * @return chosen id
   * @throws LDAPException
   */
  private UUID createAddress(Map<String, String> address) throws LDAPException {
    UUID addressUUID = UUID.randomUUID();
    AddRequest addressAddRequest =
        new AddRequest(
            getAddressDN(addressUUID.toString()), AddressLdapMapper.mapToAttributes(address));
    ldapPoolConnection.add(addressAddRequest);
    return addressUUID;
  }

  private void updateAddress(String id, Map<String, String> newAddress) throws LDAPException {
    ModifyRequest modifyRequest =
        new ModifyRequest(getAddressDN(id), AddressLdapMapper.createMods(newAddress));
    ldapPoolConnection.modify(modifyRequest);
  }

  private void deleteAddress(String id) throws LDAPException {
    DeleteRequest deleteRequest = new DeleteRequest(getAddressDN(id));
    ldapPoolConnection.delete(deleteRequest);
  }

  @Override
  public void addAppManagedAttribute(String userId, String attributeKey, String attributeValue) {
    try {
      ModifyRequest modifyAttributeRequest =
          new ModifyRequest(
              getUserDN(userId),
              new Modification(ModificationType.ADD, attributeKey, attributeValue));
      ldapPoolConnection.modify(modifyAttributeRequest);
    } catch (LDAPException e) {
      throw new RuntimeException(
          "Failed to update user attribute "
              + attributeKey
              + " with value "
              + attributeValue
              + " while writing to LDAP",
          e);
    }
  }

  @Override
  public void deleteAppManagedAttribute(String userId, String attributeKey, String attributeValue) {
    try {
      ModifyRequest modifyAttributeRequest =
          new ModifyRequest(
              getUserDN(userId),
              new Modification(ModificationType.DELETE, attributeKey, attributeValue));
      ldapPoolConnection.modify(modifyAttributeRequest);
    } catch (LDAPException e) {
      if (e.getResultCode().equals(ResultCode.NO_SUCH_ATTRIBUTE)) {
        throw new AppManagedAttributeException("Cannot delete, attribute not found", e);
      }
      throw new RuntimeException(
          "Failed to update user attribute "
              + attributeKey
              + " with value "
              + attributeValue
              + " while writing to LDAP",
          e);
    }
  }
}
