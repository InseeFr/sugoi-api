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
import java.util.List;
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

  @Bean
  public UserStorage userStorage() {
    UserStorage us = new UserStorage();
    us.setOrganizationSource(organizationSource);
    us.setUserSource(userSource);
    us.setName("default");
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

  @Test
  public void testCreateOrganization() {
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    Organization organization = new Organization();
    organization.setIdentifiant("Titi");
    organization.addAttributes("description", "titi le test");
    ldapWriterStore.createOrganization(organization);
    assertThat(
        "Titi should have been added", ldapReaderStore.getOrganization("Titi"), not(nullValue()));
  }

  @Test
  public void testUpdateOrganizationDescription() {
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    Organization organization = ldapReaderStore.getOrganization("amodifier");
    organization.addAttributes("description", "nouvelle description");
    ldapWriterStore.updateOrganization(organization);
    assertThat(
        "amodifier should have a new description",
        ldapReaderStore.getOrganization("amodifier").getAttributes().get("description"),
        is("nouvelle description"));
  }

  @Test
  public void testDeleteOrganization() {
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    ldapWriterStore.deleteOrganization("asupprimer");
    assertThat(
        "asupprimer should have been deleted",
        ldapReaderStore.getOrganization("asupprimer"),
        is(nullValue()));
  }

  @Test
  public void testCreateUser() {
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    User user = new User();
    user.setUsername("Titi");
    user.setLastName("Test");
    user.setFirstName("Petit");
    user.setMail("petittest@titi.fr");
    ldapWriterStore.createUser(user);
    assertThat("Titi should have been added", ldapReaderStore.getUser("Titi"), not(nullValue()));
  }

  @Test
  public void testUpdateUserMail() {
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    User user = ldapReaderStore.getUser("testo");
    user.setMail("nvtest@insee.fr");
    ldapWriterStore.updateUser(user);
    assertThat(
        "testc should have a new mail",
        ldapReaderStore.getUser("testo").getMail(),
        is("nvtest@insee.fr"));
  }

  @Test
  public void testDeleteUser() {
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    ldapWriterStore.deleteUser("byebye");
    assertThat(
        "testc should have been deleted", ldapReaderStore.getUser("byebye"), is(nullValue()));
  }

  @Test
  public void testCreateApplication() {
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    Application application = new Application();
    application.setName("MyApplication");
    application.setOwner("Mine");
    ldapWriterStore.createApplication(application);
    assertThat(
        "MyApplication should have been added",
        ldapReaderStore.getApplication("MyApplication"),
        not(nullValue()));
  }

  @Test
  public void testUpdateApplicationOwner() {
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    Application application = ldapReaderStore.getApplication("Applitest");
    application.setOwner("Mine");
    ldapWriterStore.updateApplication(application);
    assertThat(
        "Applitest should have a new owner",
        ldapReaderStore.getApplication("Applitest").getOwner(),
        is("Mine"));
  }

  @Test
  public void testDeleteApplication() {
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    Application application = new Application();
    application.setName("EmptyApplication");
    application.setOwner("Mine");
    ldapWriterStore.createApplication(application);
    ldapWriterStore.deleteApplication("EmptyApplication");
    assertThat(
        "EmptyApplication should have been deleted",
        ldapReaderStore.getApplication("EmptyApplication"),
        is(nullValue()));
  }

  @Test
  public void testCreateGroup() {
    // for now groups are not created with their users
    // plus they are always created at the route of app_source
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    Group group = new Group();
    group.setName("Groupy");
    group.setDescription("Super groupy de test");
    User user = new User();
    user.setUsername("usery");
    List<User> users = new ArrayList<>();
    users.add(user);
    group.setUsers(users);
    ldapWriterStore.createGroup("Applitest", group);
    assertThat(
        "Should retrieve Groupy",
        ldapReaderStore.getGroup("Applitest", "Groupy").getName(),
        is("Groupy"));
  }

  @Test
  public void testDeleteGroup() {
    // can only delete first sub tree groups
    // TODO
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    Group group = new Group();
    group.setName("Asupprimer");
    group.setDescription("supprime ce groupe");
    ldapWriterStore.createGroup("Applitest", group);
    ldapWriterStore.deleteGroup("Applitest", "Asupprimer");
    assertThat(
        "Should have been deleted",
        ldapReaderStore.getGroup("Applitest", "Asupprimer"),
        is(nullValue()));
  }

  @Test
  public void testAddUserInGroup() {
    // we can only update first sub tree group
    // TODO
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    ldapWriterStore.addUserToGroup("Applitest", "SuperGroup", "testc");
    assertThat(
        "Group should contain testc",
        ldapReaderStore.getUsersInGroup("Applitest", "SuperGroup").getResults().stream()
            .anyMatch(user -> user.getUsername().equals("testc")));
  }

  @Test
  public void testDeleteUserInGroup() {
    // we can only update first sub tree group
    // TODO
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    assertThat(
        "Group should be empty",
        ldapReaderStore.getUsersInGroup("Applitest", "SuperGroup").getResults().size(),
        is(0));
    ldapWriterStore.addUserToGroup("Applitest", "SuperGroup", "agarder");
    ldapWriterStore.addUserToGroup("Applitest", "SuperGroup", "asupprimer");
    ldapWriterStore.deleteUserFromGroup("Applitest", "SuperGroup", "asupprimer");
    assertThat(
        "Group should not contain asupprimer",
        ldapReaderStore.getUsersInGroup("Applitest", "SuperGroup").getResults().stream()
            .allMatch(
                user ->
                    user == null
                        || user.getUsername() == null
                        || !user.getUsername().equals("asupprimer")));
    assertThat(
        "Group should contain agarder",
        ldapReaderStore.getUsersInGroup("Applitest", "SuperGroup").getResults().stream()
            .anyMatch(
                user ->
                    user != null
                        && user.getUsername() != null
                        && user.getUsername().equals("agarder")));
  }

  @Test
  public void testUpdateGroup() {
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    Group group = ldapReaderStore.getGroup("Applitest", "SuperGroup");
    group.setDescription("new description");
    ldapWriterStore.updateGroup("Applitest", group);
    assertThat(
        "SuperGroup description should be new description",
        ldapReaderStore.getGroup("Applitest", "SuperGroup").getDescription(),
        is("new description"));
  }
}
