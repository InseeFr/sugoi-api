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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
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

  @Bean
  public UserStorage userStorage() {
    UserStorage us = new UserStorage();
    us.setOrganizationSource(organizationSource);
    us.setUserSource(userSource);
    us.setAddressSource(addressSource);
    us.setName("default");
    us.addProperty("group_filter_pattern", groupFilterPattern);
    us.addProperty("group_source_pattern", groupSourcePattern);
    return us;
  }

  @Bean(name = "Realm")
  public Realm realm() {
    Realm realm = new Realm();
    realm.setName("domaine1");
    realm.setUrl("localhost");
    realm.setAppSource(appSource);
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
    address.put("Ligne1", "Orga");
    address.put("Ligne2", "Chez orga");
    organization.setAddress(address);
    ldapWriterStore.createOrganization(organization);
    Organization retrievedOrga = ldapReaderStore.getOrganization("Titi");

    assertThat("Titi should have been added", retrievedOrga, not(nullValue()));
    assertThat("Titi should have an address", retrievedOrga.getAddress().get("Ligne1"), is("Orga"));
  }

  @Test
  public void testUpdateOrganization() {
    Organization organization = ldapReaderStore.getOrganization("amodifier");
    organization.addAttributes("description", "nouvelle description");
    Map<String, String> address = new HashMap<>();
    address.put("Ligne1", "Orga");
    address.put("Ligne2", "Chez orga");
    organization.setAddress(address);
    ldapWriterStore.updateOrganization(organization);
    Organization retrievedOrga = ldapReaderStore.getOrganization("amodifier");
    assertThat(
        "amodifier should have a new description",
        retrievedOrga.getAttributes().get("description"),
        is("nouvelle description"));
    assertThat(
        "amodifier should have an address", retrievedOrga.getAddress().get("Ligne1"), is("Orga"));
  }

  @Test
  public void testDeleteOrganization() {
    ldapWriterStore.deleteOrganization("asupprimer");
    assertThat(
        "asupprimer should have been deleted",
        ldapReaderStore.getOrganization("asupprimer"),
        is(nullValue()));
  }

  @Test
  public void testCreateUser() {
    User user = new User();
    user.setUsername("Titi");
    user.setLastName("Test");
    user.setFirstName("Petit");
    user.setMail("petittest@titi.fr");
    Map<String, String> address = new HashMap<>();
    address.put("Ligne1", "Toto");
    address.put("Ligne2", "Chez Toto");
    user.setAddress(address);
    user.addAttributes("additionalMail", "other@insee.fr");
    ldapWriterStore.createUser(user);
    User retrievedUser = ldapReaderStore.getUser("Titi");
    assertThat("Titi should have been added", retrievedUser, not(nullValue()));
    assertThat("Titi should have an address", retrievedUser.getAddress().get("Ligne1"), is("Toto"));
    assertThat(
        "Should have an alternative mail",
        retrievedUser.getAttributes().get("additionalMail"),
        is("other@insee.fr"));
  }

  @Test
  public void testCreateUserWithoutAddress() {
    User user = new User();
    user.setUsername("TitiNoAddress");
    user.setLastName("Test");
    user.setFirstName("Petit");
    user.setMail("petittest@titi.fr");
    ldapWriterStore.createUser(user);
    User retrievedUser = ldapReaderStore.getUser("TitiNoAddress");
    assertThat("TitiNoAddress should have been added", retrievedUser, not(nullValue()));
    assertThat("TitiNoAddress shouldn't have an address", retrievedUser.getAddress().size(), is(0));
  }

  @Test
  public void testUpdateUser() {
    User user = ldapReaderStore.getUser("testo");
    user.setMail("nvtest@insee.fr");
    Map<String, String> address = new HashMap<>();
    address.put("Ligne1", "Toto");
    address.put("Ligne2", "Chez Toto");
    user.setAddress(address);
    ldapWriterStore.updateUser(user,false,null);
    User modifiedUser = ldapReaderStore.getUser("testo");
    assertThat("testo should have a new mail", modifiedUser.getMail(), is("nvtest@insee.fr"));
    assertThat("testo should have an address", modifiedUser.getAddress().get("Ligne1"), is("Toto"));
  }

  @Test
  public void testDeleteUser() {
    ldapWriterStore.deleteUser("byebye");
    assertThat(
        "testc should have been deleted", ldapReaderStore.getUser("byebye"), is(nullValue()));
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
    ldapWriterStore.createApplication(application);
    Application retrievedApp = ldapReaderStore.getApplication("MyApplication");
    assertThat("MyApplication should have been added", retrievedApp, not(nullValue()));
    assertThat(
        "My application should have groups",
        retrievedApp.getGroups().get(0).getName(),
        is("Group1_MyApplication"));
  }

  @Test
  public void testUpdateApplication() {
    Application application = ldapReaderStore.getApplication("Applitest");
    List<Group> groups = new ArrayList<>();
    Group group1 = new Group();
    group1.setName("Group1_Applitest");
    groups.add(group1);
    application.setGroups(groups);
    ldapWriterStore.updateApplication(application);
    Application retrievedApplication = ldapReaderStore.getApplication("Applitest");
    assertThat(
        "Applitest should have group1",
        retrievedApplication.getGroups().stream()
            .anyMatch(group -> group.getName().equals("Group1_Applitest")));
  }

  @Test
  public void testDeleteApplication() {
    Application application = new Application();
    application.setName("NotEmptyApplication");
    List<Group> groups = new ArrayList<>();
    Group group1 = new Group();
    group1.setName("Group1_NotEmptyApplication");
    application.setGroups(groups);
    ldapWriterStore.createApplication(application);
    ldapWriterStore.deleteApplication("NotEmptyApplication");
    assertThat(
        "NotEmptyApplication should have been deleted",
        ldapReaderStore.getApplication("NotEmptyApplication"),
        is(nullValue()));
    assertThat(
        "Group1 should not exist in NotEmptyApplication",
        ldapReaderStore.getGroup("NotEmptyApplication", "Group1_NotEmptyApplication"),
        is(nullValue()));
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
    ldapWriterStore.createGroup("Applitest", group);
    assertThat(
        "Should retrieve Groupy",
        ldapReaderStore.getGroup("Applitest", "Groupy_Applitest").getName(),
        is("Groupy_Applitest"));
  }

  @Test
  public void testDeleteGroup() {
    Group group = new Group();
    group.setName("Asupprimer_WebServicesLdap");
    group.setDescription("supprime ce groupe");
    ldapWriterStore.createGroup("WebServicesLdap", group);
    ldapWriterStore.deleteGroup("WebServicesLdap", "Asupprimer_WebServicesLdap");
    assertThat(
        "Should have been deleted",
        ldapReaderStore.getGroup("WebServicesLdap", "Asupprimer_WebServicesLdap"),
        is(nullValue()));
  }

  @Test
  public void testAddUserInGroup() {
    ldapWriterStore.addUserToGroup("Applitest", "Administrateurs_Applitest", "testc");
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
    ldapWriterStore.addUserToGroup("Applitest", "Administrateurs_Applitest", "agarder");
    ldapWriterStore.addUserToGroup("Applitest", "Administrateurs_Applitest", "asupprimer");
    ldapWriterStore.deleteUserFromGroup("Applitest", "Administrateurs_Applitest", "asupprimer");
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
    Group group = ldapReaderStore.getGroup("Applitest", "Amodifier_Applitest");
    group.setDescription("new description");
    ldapWriterStore.updateGroup("Applitest", group);
    assertThat(
        "SuperGroup description should be new description",
        ldapReaderStore.getGroup("Applitest", "Amodifier_Applitest").getDescription(),
        is("new description"));
  }
}
