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
package fr.insee.sugoi.ldap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.core.exceptions.InvalidPasswordException;
import fr.insee.sugoi.core.exceptions.StoragePolicyNotMetException;
import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.model.ProviderResponse.ProviderResponseStatus;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Habilitation;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.store.ldap.LdapReaderStore;
import fr.insee.sugoi.store.ldap.LdapStoreBeans;
import fr.insee.sugoi.store.ldap.LdapWriterStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {EmbeddedLdapAutoConfiguration.class, LdapStoreBeans.class})
@TestPropertySource(locations = "classpath:/application.properties")
public class LdapWriterStoreTest {

  @Value("${fr.insee.sugoi.ldap.default.organizationsource:}")
  private String organizationSource;

  @Value("${fr.insee.sugoi.ldap.default.appsource:}")
  private String appSource;

  @Value("${fr.insee.sugoi.ldap.default.usersource:}")
  private String userSource;

  @Value("${fr.insee.sugoi.ldap.default.groupsourcepattern:}")
  private String groupSourcePattern;

  @Value("${fr.insee.sugoi.ldap.default.groupfilterpattern:}")
  private String groupFilterPattern;

  @Value("${fr.insee.sugoi.ldap.default.addresssource:}")
  private String addressSource;

  @Value("${fr.insee.sugoi.ldap.default.app_managed_attribute_pattern:}")
  private String appManagedAttributePattern;

  @Value("${fr.insee.sugoi.ldap.default.app_managed_attribute_key:}")
  private String appManagedAttributeKey;

  @Bean
  public UserStorage userStorage() {
    UserStorage us = new UserStorage();
    us.setOrganizationSource(organizationSource);
    us.setUserSource(userSource);
    us.setAddressSource(addressSource);
    us.setName("default");
    us.addProperty("group_filter_pattern", groupFilterPattern);
    us.addProperty("group_source_pattern", groupSourcePattern);
    us.addProperty(
        LdapConfigKeys.USER_OBJECT_CLASSES,
        "top,inseeContact,inseeAttributsHabilitation,inseeAttributsCommunication");
    us.addProperty(LdapConfigKeys.ORGANIZATION_OBJECT_CLASSES, "top,inseeOrganisation");
    Map<String, Map<String, String>> mappings = new HashMap<>();

    Map<String, String> userMapping = new HashMap<>();
    userMapping.put("username", "uid,String,rw");
    userMapping.put("lastName", "sn,String,rw");
    userMapping.put("mail", "mail,String,rw");
    userMapping.put("firstName", "givenname,String,rw");
    userMapping.put("attributes.common_name", "cn,String,rw");
    userMapping.put("attributes.personal_title", "personalTitle,String,rw");
    userMapping.put("attributes.description", "description,String,rw");
    userMapping.put("attributes.phone_number", "telephoneNumber,String,rw");
    userMapping.put("habilitations", "inseeGroupeDefaut,list_habilitation,rw");
    userMapping.put("organization", "inseeOrganisationDN,organization,rw");
    userMapping.put("address", "inseeAdressePostaleDN,address,rw");
    userMapping.put("groups", "memberOf,list_group,ro");
    userMapping.put("attributes.insee_roles_applicatifs", "inseeRoleApplicatif,list_string,rw");
    userMapping.put("attributes.common_name", "cn,String,rw");
    userMapping.put("attributes.additionalMail", "inseeMailCorrespondant,String,rw");
    userMapping.put("attributes.passwordShouldBeReset", "pwdReset,String,ro");
    Map<String, String> organizationMapping = new HashMap<>();
    organizationMapping.put("identifiant", "uid,String,rw");
    organizationMapping.put("attributes.description", "description,String,rw");
    organizationMapping.put("attributes.mail", "mail,String,rw");
    organizationMapping.put("address", "inseeAdressePostaleDN,address,rw");
    organizationMapping.put("organization", "inseeOrganisationDN,organization,rw");
    Map<String, String> applicationMapping = new HashMap<>();
    applicationMapping.put("name", "ou,String,rw");
    Map<String, String> groupMapping = new HashMap<>();
    groupMapping.put("name", "cn,String,rw");
    groupMapping.put("description", "description,String,rw");
    groupMapping.put("users", "uniquemember,list_user,rw");

    mappings.put("userMapping", userMapping);
    mappings.put("organizationMapping", organizationMapping);
    mappings.put("applicationMapping", applicationMapping);
    mappings.put("groupMapping", groupMapping);

    us.setMappings(mappings);
    return us;
  }

  @Bean(name = "Realm")
  public Realm realm() {
    Realm realm = new Realm();
    realm.setName("domaine1");
    realm.setUrl("localhost");
    realm.setAppSource(appSource);
    realm.addProperty(GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_KEYS_LIST, appManagedAttributeKey);
    realm.addProperty(
        GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_PATTERNS_LIST, appManagedAttributePattern);
    realm.addProperty(LdapConfigKeys.UNIQUE_EMAILS, "false");

    Map<String, Map<String, String>> mappings = new HashMap<>();
    Map<String, String> applicationMapping = new HashMap<>();
    applicationMapping.put("name", "ou,String,rw");
    Map<String, String> groupMapping = new HashMap<>();
    groupMapping.put("name", "cn,String,rw");
    groupMapping.put("description", "description,String,rw");
    groupMapping.put("users", "uniquemember,list_user,rw");
    mappings.put("applicationMapping", applicationMapping);
    mappings.put("groupMapping", groupMapping);
    realm.setMappings(mappings);

    return realm;
  }

  @Autowired ApplicationContext context;

  LdapReaderStore ldapReaderStore;
  LdapWriterStore ldapWriterStore;

  @BeforeEach
  public void setup() {
    ldapWriterStore = (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    ldapReaderStore = (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
  }

  @Test
  public void testCreateOrganization() {
    Organization organization = new Organization();
    organization.setIdentifiant("Titi");
    organization.addAttributes("description", "titi le test");
    Map<String, String> address = new HashMap<>();
    address.put("line1", "Orga");
    address.put("line2", "Chez orga");
    organization.setAddress(address);
    ldapWriterStore.createOrganization(organization, null);
    Organization retrievedOrga = ldapReaderStore.getOrganization("Titi").get();

    assertThat("Titi should have been added", retrievedOrga, not(nullValue()));
    assertThat("Titi should have an address", retrievedOrga.getAddress().get("line1"), is("Orga"));
  }

  @Test
  public void testUpdateOrganization() {
    Organization organization = ldapReaderStore.getOrganization("amodifier").get();
    organization.addAttributes("description", "nouvelle description");
    Map<String, String> address = new HashMap<>();
    address.put("line1", "Orga");
    address.put("line2", "Chez orga");
    organization.setAddress(address);
    ldapWriterStore.updateOrganization(organization, null);
    Organization retrievedOrga = ldapReaderStore.getOrganization("amodifier").get();
    assertThat(
        "amodifier should have a new description",
        retrievedOrga.getAttributes().get("description"),
        is("nouvelle description"));
    assertThat(
        "amodifier should have an address", retrievedOrga.getAddress().get("line1"), is("Orga"));
  }

  @Test
  public void testDeleteOrganization() {
    ldapWriterStore.deleteOrganization("asupprimer", null);
    assertThat(
        "asupprimer should have been deleted",
        ldapReaderStore.getOrganization("asupprimer").isEmpty());
  }

  @Test
  public void testCreateUser() {
    User user = new User();
    user.setUsername("Titi");
    user.setLastName("Test");
    user.setFirstName("Petit");
    user.setMail("petittest@titi.fr");
    Map<String, String> address = new HashMap<>();
    address.put("line1", "Toto");
    address.put("line2", "Chez Toto");
    user.setAddress(address);
    user.addAttributes("additionalMail", "other@insee.fr");
    user.addHabilitation(new Habilitation("application", "role", "property"));
    ldapWriterStore.createUser(user, null);
    User retrievedUser = ldapReaderStore.getUser("Titi").get();
    assertThat("Titi should have been added", retrievedUser, not(nullValue()));
    assertThat("Titi should have an address", retrievedUser.getAddress().get("line1"), is("Toto"));
    assertThat(
        "Should have an alternative mail",
        retrievedUser.getAttributes().get("additionalMail"),
        is("other@insee.fr"));
  }

  @Test
  public void testCreateUserReturnEntityId() {
    User user = new User();
    user.setUsername("TitiReturn");
    user.setFirstName("Petit");
    ProviderResponse providerResponse = ldapWriterStore.createUser(user, null);
    assertThat("Response should contain id", providerResponse.getEntityId(), is("TitiReturn"));
  }

  @Test
  public void testCreateUserWithoutAddress() {
    User user = new User();
    user.setUsername("TitiNoAddress");
    user.setLastName("Test");
    user.setFirstName("Petit");
    user.setMail("petittest@titi.fr");
    user.getAttributes().put("common_name", "Petit Test");
    ldapWriterStore.createUser(user, null);
    User retrievedUser = ldapReaderStore.getUser("TitiNoAddress").get();
    assertThat("TitiNoAddress should have been added", retrievedUser, not(nullValue()));
    assertThat("TitiNoAddress shouldn't have an address", retrievedUser.getAddress().size(), is(0));
  }

  @Test
  public void testUpdateUser() {
    User user = ldapReaderStore.getUser("testo").get();
    user.setMail("nvtest@insee.fr");
    Map<String, String> address = new HashMap<>();
    address.put("line1", "Toto");
    address.put("line2", "Chez Toto");
    user.setAddress(address);
    ldapWriterStore.updateUser(user, new ProviderRequest(null, false, null));
    User modifiedUser = ldapReaderStore.getUser("testo").get();
    assertThat("testo should have a new mail", modifiedUser.getMail(), is("nvtest@insee.fr"));
    assertThat("testo should have an address", modifiedUser.getAddress().get("line1"), is("Toto"));
  }

  @Test
  public void testUpdateUserWithSameMailWithoutUnicityNeeded() {
    User user = ldapReaderStore.getUser("testo").get();
    user.setMail("test1@test.fr");
    ldapWriterStore.updateUser(user, new ProviderRequest(null, false, null));
    User modifiedUser = ldapReaderStore.getUser("testo").get();
    assertThat("testo should have a new mail", modifiedUser.getMail(), is("test1@test.fr"));
  }

  @Test
  public void testUpdateUserWithEmptyValue() {
    User user = ldapReaderStore.getUser("testo").get();
    user.setMail("");
    ldapWriterStore.updateUser(user, new ProviderRequest(null, false, null));
    User modifiedUser = ldapReaderStore.getUser("testo").get();
    assertThat("testo should not have a mail anymore", modifiedUser.getMail(), is(nullValue()));
    // updating the same way a second time should lead to the same result
    ldapWriterStore.updateUser(user, new ProviderRequest(null, false, null));
    assertThat("testo should not have a mail anymore", modifiedUser.getMail(), is(nullValue()));
  }

  @DisplayName(
      "Given a user that has an attribute of type list which contains element,"
          + " updating this list with an empty list should delete the former values")
  @Test
  public void testUpdateUserWithEmptyList() {
    User user = ldapReaderStore.getUser("userwithlist").get();
    assertThat(
        "The user has attributes roles applicatifs",
        ((List<?>) user.getAttributes().get("insee_roles_applicatifs")).size(),
        greaterThan(0));
    user.getAttributes().put("insee_roles_applicatifs", List.of());
    ldapWriterStore.updateUser(user, new ProviderRequest(null, false, null));
    User modifiedUser = ldapReaderStore.getUser("userwithlist").get();
    assertThat(
        "The user has no more roles applicatifs",
        !modifiedUser.getAttributes().containsKey("insee_roles_applicatifs"));
  }

  @DisplayName(
      "Given a user that has habilitations,"
          + " updating these habilitations with an empty list should delete the former values")
  @Test
  public void testUpdateUserWithoutHabilitations() {
    User user = ldapReaderStore.getUser("userwithlist").get();
    assertThat("The user has habilitations", user.getHabilitations().size(), greaterThan(0));
    user.getHabilitations().clear();
    ldapWriterStore.updateUser(user, new ProviderRequest(null, false, null));
    User modifiedUser = ldapReaderStore.getUser("userwithlist").get();
    assertThat("The user has no more habilitations", modifiedUser.getHabilitations().size(), is(0));
  }

  @Test
  public void testDeleteUser() {
    assertThat(
        "byebye is in Utilisateurs_Applitest",
        ldapReaderStore.getGroup("Applitest", "Utilisateurs_Applitest").get().getUsers().stream()
            .anyMatch(user -> user.getUsername().equalsIgnoreCase("byebye")));
    assertThat(
        "byebye is in Utilisateurs_Applitest",
        ldapReaderStore.getUsersInGroup("Applitest", "Utilisateurs_Applitest").getResults().stream()
            .anyMatch(user -> user.getUsername().equalsIgnoreCase("byebye")));
    ldapWriterStore.deleteUser("byebye", null);
    assertThat("byebye should have been deleted", ldapReaderStore.getUser("byebye").isEmpty());
    assertThat(
        "byebye should no more be in Utilisateurs_Applitest",
        !ldapReaderStore.getGroup("Applitest", "Utilisateurs_Applitest").get().getUsers().stream()
            .anyMatch(user -> user.getUsername().equalsIgnoreCase("byebye")));
    assertThat(
        "byebye is in Utilisateurs_Applitest",
        !ldapReaderStore
            .getUsersInGroup("Applitest", "Utilisateurs_Applitest")
            .getResults()
            .stream()
            .anyMatch(user -> user.getUsername().equalsIgnoreCase("byebye")));
  }

  @Test
  public void testCreateApplication() {
    Application application = new Application();
    application.setName("MyApplication");
    List<Group> groups = new ArrayList<>();
    Group group1 = new Group();
    group1.setName("Group1_MyApplication");
    Group group2 = new Group();
    group2.setName("Group2_MyApplication");
    groups.add(group1);
    groups.add(group2);
    application.setGroups(groups);
    ldapWriterStore.createApplication(application, null);
    Application retrievedApp = ldapReaderStore.getApplication("MyApplication").get();
    assertThat("MyApplication should have been added", retrievedApp, not(nullValue()));
    assertThat(
        "My application should have groups",
        retrievedApp.getGroups().get(0).getName(),
        is("Group1_MyApplication"));
  }

  @Test
  public void testUpdateApplicationWithGroupAdding() {
    Application application = ldapReaderStore.getApplication("Applitest").get();
    Group group1 = new Group();
    group1.setName("Group1_Applitest");
    application.getGroups().add(group1);
    ldapWriterStore.updateApplication(application, null);
    Application retrievedApplication = ldapReaderStore.getApplication("Applitest").get();
    assertThat(
        "Applitest should have group1",
        retrievedApplication.getGroups().stream()
            .anyMatch(group -> group.getName().equals("Group1_Applitest")));
  }

  @Test
  public void testUpdateApplicationWithGroupRemoving() {
    Application application = ldapReaderStore.getApplication("Applitest").get();
    Group adminApplitestGroup =
        application.getGroups().stream()
            .filter(group -> group.getName().equalsIgnoreCase("ToDelete_Applitest"))
            .findFirst()
            .get();
    application.getGroups().remove(adminApplitestGroup);
    ldapWriterStore.updateApplication(application, null);
    Application retrievedApplication = ldapReaderStore.getApplication("Applitest").get();
    assertThat(
        "Applitest should not have todelete",
        !retrievedApplication.getGroups().stream()
            .anyMatch(group -> group.getName().equals("ToDelete_Applitest")));
  }

  @Test
  public void testUpdateApplicationWithGroupModifying() {
    Application application = ldapReaderStore.getApplication("Applitest").get();
    Group adminApplitestGroup =
        application.getGroups().stream()
            .filter(group -> group.getName().equalsIgnoreCase("ToUpdate_Applitest"))
            .findFirst()
            .get();
    adminApplitestGroup.setDescription("new description");
    ldapWriterStore.updateApplication(application, null);
    Application retrievedApplication = ldapReaderStore.getApplication("Applitest").get();
    assertThat(
        "ToUpdate should have description new description",
        retrievedApplication.getGroups().stream()
            .anyMatch(
                group ->
                    group.getName().equalsIgnoreCase("ToUpdate_Applitest")
                        && group.getDescription().equalsIgnoreCase("new description")));
    assertThat(
        "ToUpdate should still have users",
        retrievedApplication.getGroups().stream()
            .anyMatch(
                group ->
                    group.getName().equalsIgnoreCase("ToUpdate_Applitest")
                        && group.getUsers().size() > 0));
  }

  @Test
  public void testDeleteApplication() {
    Application application = new Application();
    application.setName("NotEmptyApplication");
    List<Group> groups = new ArrayList<>();
    Group group1 = new Group();
    group1.setName("Group1_NotEmptyApplication");
    application.setGroups(groups);
    ldapWriterStore.createApplication(application, null);
    ldapWriterStore.deleteApplication("NotEmptyApplication", null);
    assertThat(
        "NotEmptyApplication should have been deleted",
        ldapReaderStore.getApplication("NotEmptyApplication").isEmpty());
    assertThat(
        "Group1 should not exist in NotEmptyApplication",
        ldapReaderStore.getGroup("NotEmptyApplication", "Group1_NotEmptyApplication").isEmpty());
  }

  @Test
  public void testCreateGroup() {
    Group group = new Group();
    group.setName("Groupy_Applitest");
    group.setDescription("Super groupy de test");
    User user = new User();
    user.setUsername("usery");
    List<User> users = new ArrayList<>();
    users.add(user);
    group.setUsers(users);
    ldapWriterStore.createGroup("Applitest", group, null);
    assertThat(
        "Should retrieve Groupy",
        ldapReaderStore.getGroup("Applitest", "Groupy_Applitest").get().getName(),
        is("Groupy_Applitest"));
  }

  @Test
  public void testDeleteGroup() {
    Group group = new Group();
    group.setName("Asupprimer_WebServicesLdap");
    group.setDescription("supprime ce groupe");
    ldapWriterStore.createGroup("WebServicesLdap", group, null);
    ldapWriterStore.deleteGroup("WebServicesLdap", "Asupprimer_WebServicesLdap", null);
    assertThat(
        "Should have been deleted",
        ldapReaderStore.getGroup("WebServicesLdap", "Asupprimer_WebServicesLdap").isEmpty());
  }

  @Test
  public void testAddUserInGroup() {
    ldapWriterStore.addUserToGroup("Applitest", "Administrateurs_Applitest", "testc", null);
    assertThat(
        "Group should contain testc",
        ldapReaderStore
            .getUsersInGroup("Applitest", "Administrateurs_Applitest")
            .getResults()
            .stream()
            .anyMatch(user -> user.getUsername().equals("testc")));
  }

  @Test
  public void testDeleteUserInGroup() {
    assertThat(
        "Group should be empty",
        ldapReaderStore
            .getUsersInGroup("Applitest", "Administrateurs_Applitest")
            .getResults()
            .size(),
        is(0));
    ldapWriterStore.addUserToGroup("Applitest", "Administrateurs_Applitest", "agarder", null);
    ldapWriterStore.addUserToGroup("Applitest", "Administrateurs_Applitest", "asupprimer", null);
    ldapWriterStore.deleteUserFromGroup(
        "Applitest", "Administrateurs_Applitest", "asupprimer", null);
    assertThat(
        "Group should not contain asupprimer",
        ldapReaderStore
            .getUsersInGroup("Applitest", "Administrateurs_Applitest")
            .getResults()
            .stream()
            .allMatch(
                user ->
                    user == null
                        || user.getUsername() == null
                        || !user.getUsername().equals("asupprimer")));
    assertThat(
        "Group should contain agarder",
        ldapReaderStore
            .getUsersInGroup("Applitest", "Administrateurs_Applitest")
            .getResults()
            .stream()
            .anyMatch(
                user ->
                    user != null
                        && user.getUsername() != null
                        && user.getUsername().equals("agarder")));
  }

  @Test
  public void testUpdateGroup() {
    Group group = ldapReaderStore.getGroup("Applitest", "Amodifier_Applitest").get();
    group.setDescription("new description");
    ldapWriterStore.updateGroup("Applitest", group, null);
    assertThat(
        "SuperGroup description should be new description",
        ldapReaderStore.getGroup("Applitest", "Amodifier_Applitest").get().getDescription(),
        is("new description"));
  }

  @Test
  public void testGroupShouldMatchGroupPattern() {
    Group groupWontCreate = new Group();
    groupWontCreate.setName("bad_group_badsuffix");
    StoragePolicyNotMetException e =
        assertThrows(
            StoragePolicyNotMetException.class,
            () -> ldapWriterStore.createGroup("WebServicesLdap", groupWontCreate, null));
    assertThat("Should get correct message", e.getMessage(), is("Group pattern won't match"));
    Group groupToCreate = new Group();
    groupToCreate.setName("good_group_webServicesLdap");
    ldapWriterStore.createGroup("WebServicesLdap", groupToCreate, null);
  }

  @Test
  public void testInitPasswordNullFails() {
    assertThrows(Exception.class, () -> ldapWriterStore.initPassword("testc", null, true, null));
  }

  @Test
  public void testInitPassword() {
    ldapWriterStore.initPassword("testo", "toto", false, null);
    assertThat(
        "Password toto should be validated",
        ldapReaderStore.validateCredentials(ldapReaderStore.getUser("testo").get(), "toto"));
    assertThat(
        "Password testc should not be validated",
        !ldapReaderStore.validateCredentials(ldapReaderStore.getUser("testo").get(), "testc"));
  }

  @Test
  public void testReinitPassword() {
    Map<String, String> voidMap = new HashMap<>();
    assertThat(
        "Password should not be reinit",
        !ldapReaderStore.validateCredentials(ldapReaderStore.getUser("testo").get(), "reinit"));
    ldapWriterStore.reinitPassword("testo", "reinit", true, voidMap, "", null);
    assertThat(
        "Password should be reinit",
        ldapReaderStore.validateCredentials(ldapReaderStore.getUser("testo").get(), "reinit"));
    assertThat(
        "Password should not be testo",
        !ldapReaderStore.validateCredentials(ldapReaderStore.getUser("testo").get(), "testo"));
    ldapWriterStore.reinitPassword("testo", "reinit2", true, voidMap, "", null);
    assertThat(
        "Password should be reinit2",
        ldapReaderStore.validateCredentials(ldapReaderStore.getUser("testo").get(), "reinit2"));
    assertThat(
        "Password should not be reinit",
        !ldapReaderStore.validateCredentials(ldapReaderStore.getUser("testo").get(), "reinit"));
  }

  @Test
  @DisplayName(
      "When a password is renitialized on a user that doesn't have the flag reinit, "
          + "with the request indicating password must be changed, "
          + "then a flag pwdReset on the user can be retrieved and set to true")
  public void reinitPasswordSetsReinitUserNotHavingPwdReset() {
    ldapWriterStore.reinitPassword("nopwdreset", "reinit", true, new HashMap<>(), "", null);
    assertThat(
        "user should have an attribute passwordShouldBeReset",
        ((String)
                ldapReaderStore
                    .getUser("nopwdreset")
                    .get()
                    .getAttributes()
                    .get("passwordShouldBeReset"))
            .toLowerCase(),
        is("true"));
  }

  @Test
  @DisplayName(
      "When a password is renitialized on a user that already has the flag reinit, "
          + "with the request indicating password must be changed, "
          + "then a flag pwdReset on the user can be retrieved and set to true")
  public void reinitPasswordSetsReinitUseHavingPwdReset() {
    ldapWriterStore.reinitPassword("havepwdreset", "reinit", true, new HashMap<>(), "", null);
    assertThat(
        "user should have an attribute passwordShouldBeReset",
        ((String)
                ldapReaderStore
                    .getUser("havepwdreset")
                    .get()
                    .getAttributes()
                    .get("passwordShouldBeReset"))
            .toLowerCase(),
        is("true"));
  }

  @Test
  @DisplayName(
      "When a password is initalized on a user that doesn't have the flag reinit, "
          + "with the request indicating password must be changed, "
          + "then a flag pwdReset on the user can be retrieved and set to true")
  public void initPasswordSetsReinitUserNotHavingPwdReset() {
    ldapWriterStore.initPassword("nopwdreset", "reinit", true, null);
    assertThat(
        "user should have an attribute passwordShouldBeReset",
        ((String)
                ldapReaderStore
                    .getUser("nopwdreset")
                    .get()
                    .getAttributes()
                    .get("passwordShouldBeReset"))
            .toLowerCase(),
        is("true"));
  }

  @Test
  public void testChangePasswordWithFalseOld() {
    assertThrows(
        InvalidPasswordException.class,
        () ->
            ldapWriterStore.changePassword(
                "rawpassword", "falsepassword", "newpassword", null, null, null));
    assertThat(
        "Password should not be newpassword",
        !ldapReaderStore.validateCredentials(
            ldapReaderStore.getUser("rawpassword").get(), "newpassword"));
  }

  @Test
  public void testChangePasswordWithTrueOld() {
    ProviderResponse response =
        ldapWriterStore.changePassword(
            "rawpassword", "truepassword", "newpassword", null, null, null);
    assertThat(
        "Response should have status OK", response.getStatus(), is(ProviderResponseStatus.OK));
    assertThat(
        "Should have a new password",
        ldapReaderStore.validateCredentials(
            ldapReaderStore.getUser("rawpassword").get(), "newpassword"));
    assertThat(
        "Password should no more be truepassword",
        !ldapReaderStore.validateCredentials(
            ldapReaderStore.getUser("rawpassword").get(), "truepassword"));
  }

  @Test
  public void testChangePasswordWithoutOld() {
    assertThrows(
        InvalidPasswordException.class,
        () -> ldapWriterStore.changePassword("nopassword", "", "newpassword", null, null, null));
    ProviderResponse response =
        ldapWriterStore.changePassword("nopassword", null, "newpassword", null, null, null);
    assertThat(
        "Response should have status 204", response.getStatus(), is(ProviderResponseStatus.OK));
    assertThat(
        "Should have a new password",
        ldapReaderStore.validateCredentials(
            ldapReaderStore.getUser("nopassword").get(), "newpassword"));
  }

  @Test
  public void testChangeShaPassword() {
    ldapWriterStore.changePassword(
        "shapassword", "{SHA}c3q3RSeNwMY7E09Ve9oBHw+MVXg=", "newpassword", null, null, null);
    assertThat(
        "Should have a new password",
        ldapReaderStore.validateCredentials(
            ldapReaderStore.getUser("shapassword").get(), "newpassword"));
  }

  @Test
  public void testAddAppManagedAttribute() {
    User user = new User();
    user.setUsername("testAppManagedAdd");
    user.setLastName("Test");
    user.setFirstName("Petit");
    user.setMail("petittest@titi.fr");
    user.getAttributes().put("common_name", "Petit test");
    ldapWriterStore.createUser(user, null);
    ldapWriterStore.addAppManagedAttribute(
        "testAppManagedAdd", "inseeGroupeDefaut", "prop_role_appli", null);
    User retrievedUser = ldapReaderStore.getUser("testAppManagedAdd").get();
    assertThat(
        "Should have a new habilitation",
        retrievedUser.getHabilitations().get(0).getId(),
        is("prop_role_appli"));
  }

  @Test
  public void testDeleteAppManagedAttribute() {
    User user = new User();
    user.setUsername("testAppManagedDelete");
    user.setLastName("Test");
    user.setFirstName("Petit");
    user.getAttributes().put("common_name", "Petit test");
    user.setMail("petittest@titi.fr");
    user.addHabilitation(new Habilitation("application", "role", "property"));
    ldapWriterStore.createUser(user, null);
    ldapWriterStore.deleteAppManagedAttribute(
        "testAppManagedDelete", "inseeGroupeDefaut", "property_role_application", null);
    User retrievedUser = ldapReaderStore.getUser("testAppManagedDelete").get();
    assertThat(
        "Should have a delete one habilitation", retrievedUser.getHabilitations().size(), is(0));
  }
}
