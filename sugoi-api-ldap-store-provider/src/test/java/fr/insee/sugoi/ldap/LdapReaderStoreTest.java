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
import static org.hamcrest.Matchers.nullValue;

import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.store.ldap.LdapReaderStore;
import fr.insee.sugoi.store.ldap.LdapStoreBeans;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class LdapReaderStoreTest {

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
  public void testGetOrganization() {
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    assertThat(
        "Should get testo", ldapReaderStore.getOrganization("testo").getIdentifiant(), is("testo"));
  }

  @Test
  public void testGetNonexistentOrganization() {
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    assertThat("Should get null", ldapReaderStore.getOrganization("nottesto"), is(nullValue()));
  }

  @Test
  public void testSearchAllOrganizations() {
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    PageableResult pageableResult = new PageableResult();
    List<Organization> organizations =
        ldapReaderStore.searchOrganizations(new Organization(), pageableResult, "").getResults();
    assertThat(
        "Should contain testi",
        organizations.stream().anyMatch(orga -> orga.getIdentifiant().equals("testi")));
    assertThat(
        "Should contain testo",
        organizations.stream().anyMatch(orga -> orga.getIdentifiant().equals("testo")));
  }

  @Test
  public void testSearchOrganizationsWithMatchingDescription() {
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    PageableResult pageableResult = new PageableResult();
    Map<String, String> searchProperties = new HashMap<>();
    searchProperties.put("description", "Insee");
    Organization organizationFilter = new Organization();
    organizationFilter.addAttributes("description", "Insee");
    List<Organization> organizations =
        ldapReaderStore.searchOrganizations(organizationFilter, pageableResult, "").getResults();
    assertThat("Should find one result", organizations.size(), is(1));
    assertThat(
        "First element found should be testo", organizations.get(0).getIdentifiant(), is("testo"));
  }

  @Test
  public void testGetUser() {
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    assertThat("Should get testc", ldapReaderStore.getUser("testc").getUsername(), is("testc"));
  }

  @Test
  public void testGetNonexistentUser() {
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    assertThat("Should get null", ldapReaderStore.getUser("nottestc"), is(nullValue()));
  }

  @Test
  public void testSearchAllUsers() {
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    PageableResult pageableResult = new PageableResult();
    List<User> users = ldapReaderStore.searchUsers(new User(), pageableResult, "et").getResults();
    assertThat(
        "Should contain testo",
        users.stream().anyMatch(user -> user.getUsername().equals("testo")));
    assertThat(
        "Should contain testc",
        users.stream().anyMatch(user -> user.getUsername().equals("testc")));
  }

  @Test
  public void testSearchUserWithMatchingMail() {
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    PageableResult pageableResult = new PageableResult();
    User testUser = new User();
    testUser.setMail("test@test.fr");
    List<User> users = ldapReaderStore.searchUsers(testUser, pageableResult, "et").getResults();
    assertThat("Should find one result", users.size(), is(1));
    assertThat("First element found should be testc", users.get(0).getUsername(), is("testc"));
  }

  @Test
  public void testGetApplication() {
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    assertThat(
        "Should get applitest",
        ldapReaderStore.getApplication("Applitest").getName(),
        is("Applitest"));
  }

  @Test
  public void testGetNonexistentApplication() {
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    assertThat("Should get null", ldapReaderStore.getApplication("nottestc"), is(nullValue()));
  }

  @Test
  public void testSearchAllApplications() {
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    PageableResult pageableResult = new PageableResult();
    List<Application> applications =
        ldapReaderStore.searchApplications(new Application(), pageableResult, "").getResults();
    assertThat(
        "Should contain applitest",
        applications.stream().anyMatch(appli -> appli.getName().equals("Applitest")));
    assertThat(
        "Should contain webservicesldap",
        applications.stream().anyMatch(appli -> appli.getName().equals("WebServicesLdap")));
  }

  @Test
  public void testSearchApplicationWithMatchingName() {
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    PageableResult pageableResult = new PageableResult();
    Application applicationFilter = new Application();
    applicationFilter.setName("Applitest");
    List<Application> applications =
        ldapReaderStore.searchApplications(applicationFilter, pageableResult, "").getResults();
    assertThat("Should find one result", applications.size(), is(1));
    assertThat(
        "First element found should be Applitest", applications.get(0).getName(), is("Applitest"));
  }

  @Test
  public void testGetGroup() {
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    Group group = ldapReaderStore.getGroup("Applitest", "Utilisateurs_Applitest");
    assertThat(
        "Should be Utilisateurs_Applitest group", group.getName(), is("Utilisateurs_Applitest"));
  }

  @Test
  public void testSearchAllGroups() {
    // we should also check if there is more complexe cases like cases with organizationalGroup
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    PageableResult pageableResult = new PageableResult();
    List<Group> groups =
        ldapReaderStore.searchGroups("Applitest", new Group(), pageableResult, "").getResults();
    assertThat("Should find 3 elements", groups.size(), is(3));
    assertThat(
        "Should contain utilisateurs",
        groups.stream().anyMatch(group -> group.getName().equals("Utilisateurs_Applitest")));
    assertThat(
        "Should contain SuperGroup",
        groups.stream().anyMatch(group -> group.getName().equals("SuperGroup")));
  }

  @Test
  public void testGetUsersInGroup() {
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    List<User> users =
        ldapReaderStore.getUsersInGroup("Applitest", "Utilisateurs_Applitest").getResults();
    assertThat("Should find 2 elements", users.size(), is(2));
    // for now if a user in a group is not in the current realm, the user is null
    // TODO : decide what to do
    assertThat("Is null, may be not the expected behavior", users.get(0), is(nullValue()));
    assertThat("Should be administrateurs", users.get(1).getUsername(), is("testc"));
  }
}
