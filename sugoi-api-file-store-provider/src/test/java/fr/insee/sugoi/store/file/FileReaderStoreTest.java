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
package fr.insee.sugoi.store.file;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;

@SpringBootTest(classes = {FileReaderStore.class, FileStoreBeans.class})
public class FileReaderStoreTest {

  @Autowired ResourceLoader resourceLoader;
  @Autowired ApplicationContext context;

  FileReaderStore fileReaderStore;

  @Bean
  public UserStorage userStorage() {
    UserStorage us = new UserStorage();
    us.setOrganizationSource("classpath:/sugoi-file-tests/orgs/");
    us.setUserSource("classpath:/sugoi-file-tests/users/");
    us.setName("default");
    return us;
  }

  @Bean(name = "Realm")
  public Realm realm() {
    Realm realm = new Realm();
    realm.setAppSource("classpath:/sugoi-file-tests/apps/");
    realm.setName("domaine1");
    realm.setUrl("localhost");
    return realm;
  }

  @BeforeEach
  public void setup() {
    fileReaderStore = (FileReaderStore) context.getBean("FileReaderStore", realm(), userStorage());
  }

  @Test
  public void testGetOrganization() {
    Organization organization = fileReaderStore.getOrganization("testo");
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
  public void shouldNotPathtraversalOrganization() {
    Organization organization = fileReaderStore.getOrganization("testo/../testc");
    assertThat("Should not get testc", organization, is(nullValue()));
  }

  @Test
  public void testGetNonexistentOrganization() {
    assertThat("Should get null", fileReaderStore.getOrganization("nottesto"), is(nullValue()));
  }

  @Test
  public void testSearchAllOrganizations() {
    PageableResult pageableResult = new PageableResult();
    List<Organization> organizations =
        fileReaderStore.searchOrganizations(new Organization(), pageableResult, "AND").getResults();
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
        fileReaderStore.searchOrganizations(organizationFilter, pageableResult, "AND").getResults();
    assertThat("Should find one result", organizations.size(), is(1));
    assertThat(
        "First element found should be testo", organizations.get(0).getIdentifiant(), is("testo"));
  }

  @Test
  public void testGetUser() {
    User user = fileReaderStore.getUser("testc");
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
  }

  @Test
  public void testGetNonexistentUser() {
    assertThat("Should get null", fileReaderStore.getUser("nottestc"), is(nullValue()));
  }

  @Test
  public void testSearchAllUsers() {
    PageableResult pageableResult = new PageableResult();
    List<User> users = fileReaderStore.searchUsers(new User(), pageableResult, "AND").getResults();
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
    List<User> users = fileReaderStore.searchUsers(testUser, pageableResult, "AND").getResults();
    assertThat("Should find one result", users.size(), is(1));
    assertThat("First element found should be testc", users.get(0).getUsername(), is("testc"));
  }

  @Test
  public void testGetApplication() {
    Application application = fileReaderStore.getApplication("Applitest");
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
    assertThat("Should get null", fileReaderStore.getApplication("nottestc"), is(nullValue()));
  }

  @Test
  public void testSearchAllApplications() {
    PageableResult pageableResult = new PageableResult();
    List<Application> applications =
        fileReaderStore.searchApplications(new Application(), pageableResult, "AND").getResults();
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
        fileReaderStore.searchApplications(applicationFilter, pageableResult, "AND").getResults();
    assertThat("Should find one result", applications.size(), is(1));
    assertThat(
        "First element found should be Applitest", applications.get(0).getName(), is("Applitest"));
  }

  @Test
  public void testGetGroup() {
    Group group = fileReaderStore.getGroup("Applitest", "Utilisateurs_Applitest");
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
        fileReaderStore.searchGroups("Applitest", new Group(), pageableResult, "AND").getResults();
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
        fileReaderStore.getUsersInGroup("Applitest", "Utilisateurs_Applitest").getResults();
    assertThat("Should find 2 elements", users.size(), is(2));
    User testc =
        users.stream().filter(user -> user.getUsername().equalsIgnoreCase("testc")).findAny().get();
    assertThat("Information such as mail should be retrieved", testc.getMail(), is("test@test.fr"));
    assertThat(
        "Information such as organization's organization should be retrieved",
        testc.getOrganization().getOrganization().getIdentifiant(),
        is("testi"));
  }

  @Disabled
  @Test
  public void validateCredential() {
    User user = fileReaderStore.getUser("testc");
    assertThat(
        "Password should be validated",
        fileReaderStore.validateCredentials(user, "testc"),
        is(true));
    assertThat(
        "Password should not be validated",
        fileReaderStore.validateCredentials(user, "testc2"),
        is(false));
    assertThat(
        "Password should not be validated",
        fileReaderStore.validateCredentials(user, null),
        is(false));
  }

  @Test
  public void searchUsersInNonExistantGroupTest() {
    PageResult<User> usersPageResult =
        fileReaderStore.getUsersInGroup("Applitest", "FalseGroup_Applitest");
    assertThat(
        "PageResult should exist but without users", usersPageResult.getResults().size(), is(0));
  }
}
