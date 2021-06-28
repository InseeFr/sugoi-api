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

import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import fr.insee.sugoi.store.ldap.LdapReaderStore;
import fr.insee.sugoi.store.ldap.LdapStoreBeans;
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
public class LdapReaderStoreTest {

  @Value("${fr.insee.sugoi.ldap.default.organizationsource:}")
  private String organizationSource;

  @Value("${fr.insee.sugoi.ldap.default.appsource:}")
  private String appSource;

  @Value("${fr.insee.sugoi.ldap.default.usersource:}")
  private String userSource;

  @Value("${fr.insee.sugoi.ldap.default.addresssource:}")
  private String addressSource;

  @Value("${fr.insee.sugoi.ldap.default.groupsourcepattern:}")
  private String groupSourcePattern;

  @Value("${fr.insee.sugoi.ldap.default.groupfilterpattern:}")
  private String groupFilterPattern;

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

  @BeforeEach
  public void setup() {
    ldapReaderStore = (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
  }

  @Test
  public void testGetOrganization() {
    Organization organization = ldapReaderStore.getOrganization("testo");
    assertThat("Should get testo", organization.getIdentifiant(), is("testo"));
    assertThat(
        "Should get address first line", organization.getAddress().get("Ligne1"), is("Insee"));
    assertThat(
        "Should get address second line",
        organization.getAddress().get("Ligne4"),
        is("88 AVE VERDIER"));
    assertThat(
        "Should have description Insee",
        organization.getAttributes().get("description"),
        is("Insee"));
    Organization suborga = organization.getOrganization();
    assertThat("Should have sub organization testi", suborga.getIdentifiant(), is("testi"));
    assertThat("Suborga must have address", suborga.getAddress().get("Ligne1"), is("Insee"));
  }

  @Test
  public void testGetNonexistentOrganization() {
    assertThat("Should get null", ldapReaderStore.getOrganization("nottesto"), is(nullValue()));
  }

  @Test
  public void testSearchAllOrganizations() {
    PageableResult pageableResult = new PageableResult();
    List<Organization> organizations =
        ldapReaderStore.searchOrganizations(new Organization(), pageableResult, "AND").getResults();
    assertThat(
        "Should contain testi",
        organizations.stream().anyMatch(orga -> orga.getIdentifiant().equals("testi")));
    assertThat(
        "Should contain testo",
        organizations.stream().anyMatch(orga -> orga.getIdentifiant().equals("testo")));
  }

  @Test
  public void testSearchOrganizationsWithMatchingDescription() {
    PageableResult pageableResult = new PageableResult();
    Map<String, String> searchProperties = new HashMap<>();
    searchProperties.put("description", "Insee");
    Organization organizationFilter = new Organization();
    organizationFilter.addAttributes("description", "Insee");
    List<Organization> organizations =
        ldapReaderStore.searchOrganizations(organizationFilter, pageableResult, "AND").getResults();
    assertThat("Should find one result", organizations.size(), is(1));
    assertThat(
        "First element found should be testo", organizations.get(0).getIdentifiant(), is("testo"));
  }

  @Test
  public void testGetUser() {
    User user = ldapReaderStore.getUser("testc");
    assertThat("Should get testc", user.getUsername(), is("testc"));
    assertThat("Should get address first line", user.getAddress().get("Ligne1"), is("Insee"));
    assertThat(
        "Should get address second line", user.getAddress().get("Ligne4"), is("88 AVE VERDIER"));
    assertThat(
        "Should have organization testo", user.getOrganization().getIdentifiant(), is("testo"));
    assertThat(
        "testo should have address",
        user.getOrganization().getAddress().get("Ligne1"),
        is("Insee"));
    assertThat(
        "Should have a group",
        user.getGroups().stream()
            .anyMatch(group -> group.getName().equals("Utilisateurs_Applitest")));
    assertThat(
        "Should have an alternative mail",
        user.getAttributes().get("additionalMail"),
        is("other@insee.fr"));
  }

  @Test
  public void testGetNonexistentUser() {
    assertThat("Should get null", ldapReaderStore.getUser("nottestc"), is(nullValue()));
  }

  @Test
  public void testSearchAllUsers() {
    PageableResult pageableResult = new PageableResult();
    List<User> users = ldapReaderStore.searchUsers(new User(), pageableResult, "AND").getResults();
    assertThat(
        "Should contain testo",
        users.stream().anyMatch(user -> user.getUsername().equals("testo")));
    assertThat(
        "Should contain testc",
        users.stream().anyMatch(user -> user.getUsername().equals("testc")));
  }

  @Test
  public void testSearchUserWithMatchingMail() {
    PageableResult pageableResult = new PageableResult();
    User testUser = new User();
    testUser.setMail("test@test.fr");
    List<User> users = ldapReaderStore.searchUsers(testUser, pageableResult, "AND").getResults();
    assertThat("Should find one result", users.size(), is(1));
    assertThat("First element found should be testc", users.get(0).getUsername(), is("testc"));
  }

  @Test
  public void testGetApplication() {
    Application application = ldapReaderStore.getApplication("Applitest");
    assertThat("Should get applitest", application.getName(), is("Applitest"));
    List<Group> groups = application.getGroups();
    assertThat(
        "Should have group named Administrateurs_Applitest with description toto",
        groups.stream()
            .anyMatch(
                group ->
                    group.getName().equals("Administrateurs_Applitest")
                        && group.getDescription().equals("toto")));
    assertThat(
        "Should have group named Utilisateurs_Applitest with description tata",
        groups.stream()
            .anyMatch(
                group ->
                    group.getName().equals("Utilisateurs_Applitest")
                        && group.getDescription().equals("tata")));
  }

  @Test
  public void testGetNonexistentApplication() {
    assertThat("Should get null", ldapReaderStore.getApplication("nottestc"), is(nullValue()));
  }

  @Test
  public void testSearchAllApplications() {
    PageableResult pageableResult = new PageableResult();
    List<Application> applications =
        ldapReaderStore.searchApplications(new Application(), pageableResult, "AND").getResults();
    assertThat(
        "Should contain applitest",
        applications.stream().anyMatch(appli -> appli.getName().equals("Applitest")));
    assertThat(
        "Should contain webservicesldap",
        applications.stream().anyMatch(appli -> appli.getName().equals("WebServicesLdap")));
  }

  @Test
  public void testSearchApplicationWithMatchingName() {
    PageableResult pageableResult = new PageableResult();
    Application applicationFilter = new Application();
    applicationFilter.setName("Applitest");
    List<Application> applications =
        ldapReaderStore.searchApplications(applicationFilter, pageableResult, "AND").getResults();
    assertThat("Should find one result", applications.size(), is(1));
    assertThat(
        "First element found should be Applitest", applications.get(0).getName(), is("Applitest"));
  }

  @Test
  public void testGetGroup() {
    Group group = ldapReaderStore.getGroup("Applitest", "Utilisateurs_Applitest");
    assertThat(
        "Should be Utilisateurs_Applitest group", group.getName(), is("Utilisateurs_Applitest"));
    assertThat(
        "Should have user Testd",
        group.getUsers().stream().anyMatch(user -> user.getUsername().equals("Testd")));
    assertThat(
        "Should have user testc",
        group.getUsers().stream().anyMatch(user -> user.getUsername().equals("testc")));
  }

  @Test
  public void testSearchAllGroups() {
    PageableResult pageableResult = new PageableResult();
    List<Group> groups =
        ldapReaderStore.searchGroups("Applitest", new Group(), pageableResult, "AND").getResults();
    assertThat(
        "Should contain utilisateurs",
        groups.stream().anyMatch(group -> group.getName().equals("Utilisateurs_Applitest")));
    assertThat(
        "Should contain administrateur",
        groups.stream().anyMatch(group -> group.getName().equals("Administrateurs_Applitest")));
  }

  @Test
  public void testGetUsersInGroup() {
    List<User> users =
        ldapReaderStore.getUsersInGroup("Applitest", "Reader_Applitest").getResults();
    assertThat("Should find 2 elements", users.size(), is(2));
    assertThat("Should be readers", users.get(0).getUsername(), is("testc"));
  }

  @Test
  public void validateCredential() {
    User user = ldapReaderStore.getUser("testc");
    assertThat(
        "Password should be validated",
        ldapReaderStore.validateCredentials(user, "testc"),
        is(true));
    assertThat(
        "Password should not be validated",
        ldapReaderStore.validateCredentials(user, "testc2"),
        is(false));
    assertThat(
        "Password should not be validated",
        ldapReaderStore.validateCredentials(user, null),
        is(false));
  }

  @Test
  public void validateShaCredentialTest() {
    User user = ldapReaderStore.getUser("shapassword2");
    assertThat(
        "Hash password should be validated",
        ldapReaderStore.validateCredentials(user, "{SHA}c3q3RSeNwMY7E09Ve9oBHw+MVXg="));
  }

  @Test
  public void validateNoPasswordTest() {
    User user = ldapReaderStore.getUser("nopassword");
    assertThat(
        "Not having a password should not lead to password validation",
        !ldapReaderStore.validateCredentials(user, null));
  }

  @Test
  public void searchUsersInNonExistantGroupTest() {
    PageResult<User> usersPageResult =
        ldapReaderStore.getUsersInGroup("Applitest", "FalseGroup_Applitest");
    assertThat(
        "PageResult should exist but without users", usersPageResult.getResults().size(), is(0));
  }
}
