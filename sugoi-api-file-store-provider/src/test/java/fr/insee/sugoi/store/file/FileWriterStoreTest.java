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

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.UserStorage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;

@SpringBootTest(classes = {FileWriterStore.class, FileStoreBeans.class})
public class FileWriterStoreTest {

  @Autowired ResourceLoader resourceLoader;
  @Autowired ApplicationContext context;

  FileReaderStore fileReaderStore;
  FileWriterStore fileWriterStore;

  private ObjectMapper mapper = new ObjectMapper();

  @Bean
  public UserStorage userStorage() {
    String resourceUrl = getResourceUrl();
    UserStorage us = new UserStorage();
    us.setOrganizationSource(resourceUrl + "orgs/");
    us.setUserSource(resourceUrl + "users/");
    us.setName("default");
    return us;
  }

  @Bean(name = "Realm")
  public Realm realm() {
    String resourceUrl = getResourceUrl();
    Realm realm = new Realm();
    realm.setAppSource(resourceUrl + "apps/");
    realm.setName("domaine1");
    realm.setUrl("localhost");
    return realm;
  }

  @BeforeEach
  public void setup() {
    try {
      fileReaderStore =
          (FileReaderStore) context.getBean("FileReaderStore", realm(), userStorage());
      fileWriterStore =
          (FileWriterStore) context.getBean("FileWriterStore", realm(), userStorage());

      Map<String, String> testoOrgAdress = new HashMap<>();
      testoOrgAdress.put("Ligne1", "Insee");
      testoOrgAdress.put("Ligne4", "88 AVE VERDIER");

      Group utilisateursApplitest = new Group("Applitest", "Utilisateurs_Applitest");
      Group adminApplitest = new Group("Applitest", "Administrateurs_Applitest");
      adminApplitest.setDescription("toto");
      User simpleTestcUser = new User();
      simpleTestcUser.setUsername("testc");
      User simpleTestDUser = new User();
      simpleTestDUser.setUsername("Testd");
      utilisateursApplitest.setUsers(List.of(simpleTestDUser, simpleTestcUser));
      utilisateursApplitest.setDescription("tata");
      Application applitestApp = new Application();
      applitestApp.setName("Applitest");
      applitestApp.setGroups(List.of(utilisateursApplitest, adminApplitest));
      FileWriter applitestWriter =
          new FileWriter(
              resourceLoader.getResource("classpath:/sugoi-file-tests/apps/Applitest").getFile());
      applitestWriter.write(mapper.writeValueAsString(applitestApp));
      applitestWriter.close();
      Application webserviceldapApp = new Application();
      webserviceldapApp.setName("WebServicesLdap");
      FileWriter webserviceWriter =
          new FileWriter(
              resourceLoader
                  .getResource("classpath:/sugoi-file-tests/apps/WebServicesLdap")
                  .getFile());
      webserviceWriter.write(mapper.writeValueAsString(webserviceldapApp));
      webserviceWriter.close();

      User testoUser = new User();
      testoUser.setUsername("testo");
      FileWriter testoUserWriter =
          new FileWriter(
              resourceLoader.getResource("classpath:/sugoi-file-tests/users/testo").getFile());
      testoUserWriter.write(mapper.writeValueAsString(testoUser));
      testoUserWriter.close();

      Organization testoOrg = new Organization();
      testoOrg.setIdentifiant("testo");
      testoOrg.setAddress(testoOrgAdress);
      testoOrg.setAttributes(Map.of("description", "Insee"));
      Organization testiOrg = new Organization();
      testiOrg.setIdentifiant("testi");
      testiOrg.setAddress(testoOrgAdress);
      testoOrg.setOrganization(testiOrg);
      FileWriter testoOrgWriter =
          new FileWriter(
              resourceLoader.getResource("classpath:/sugoi-file-tests/orgs/testo").getFile());
      testoOrgWriter.write(mapper.writeValueAsString(testoOrg));
      testoOrgWriter.close();

      User testcUser = new User();
      testcUser.setUsername("testc");
      testcUser.setAddress(testoOrgAdress);
      testcUser.setGroups(List.of(utilisateursApplitest));
      testcUser.setMail("test@test.fr");
      FileWriter testcUserWriter =
          new FileWriter(
              resourceLoader.getResource("classpath:/sugoi-file-tests/users/testc").getFile());
      testcUser.setOrganization(testoOrg);
      testcUserWriter.write(mapper.writeValueAsString(testcUser));
      testcUserWriter.close();

      FileWriter testiOrgWriter =
          new FileWriter(
              resourceLoader.getResource("classpath:/sugoi-file-tests/orgs/testi").getFile());
      testiOrgWriter.write(mapper.writeValueAsString(testiOrg));
      testiOrgWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
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
    fileWriterStore.createOrganization(organization);
    fileReaderStore.getOrganization("testo");
    Organization retrievedOrga = fileReaderStore.getOrganization("Titi");

    assertThat("Titi should have been added", retrievedOrga, not(nullValue()));
    assertThat("Titi should have an address", retrievedOrga.getAddress().get("Ligne1"), is("Orga"));
  }

  @Test
  public void testUpdateOrganization() {
    Organization organization = fileReaderStore.getOrganization("testo");
    organization.addAttributes("description", "nouvelle description");
    Map<String, String> address = new HashMap<>();
    address.put("Ligne1", "Orga");
    address.put("Ligne2", "Chez orga");
    organization.setAddress(address);
    fileWriterStore.updateOrganization(organization);
    Organization retrievedOrga = fileReaderStore.getOrganization("testo");
    assertThat(
        "testo should have a new description",
        retrievedOrga.getAttributes().get("description"),
        is("nouvelle description"));
    assertThat(
        "amodifier should have an address", retrievedOrga.getAddress().get("Ligne1"), is("Orga"));
  }

  @Test
  public void testDeleteOrganization() {
    Organization organization = new Organization();
    organization.setIdentifiant("asupprimer");
    fileWriterStore.createOrganization(organization);
    assertThat(
        "asupprimer should have been created",
        fileReaderStore.getOrganization("asupprimer"),
        is(not(nullValue())));
    fileWriterStore.deleteOrganization("asupprimer");
    assertThat(
        "asupprimer should have been deleted",
        fileReaderStore.getOrganization("asupprimer"),
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
    fileWriterStore.createUser(user);
    User retrievedUser = fileReaderStore.getUser("Titi");
    assertThat("Titi should have been added", retrievedUser, not(nullValue()));
    assertThat("Titi should have an address", retrievedUser.getAddress().get("Ligne1"), is("Toto"));
  }

  @Test
  public void testCreateUserWithoutAddress() {
    User user = new User();
    user.setUsername("TitiNoAddress");
    user.setLastName("Test");
    user.setFirstName("Petit");
    user.setMail("petittest@titi.fr");
    fileWriterStore.createUser(user);
    User retrievedUser = fileReaderStore.getUser("TitiNoAddress");
    assertThat("TitiNoAddress should have been added", retrievedUser, not(nullValue()));
    assertThat("TitiNoAddress shouldn't have an address", retrievedUser.getAddress().size(), is(0));
  }

  @Test
  public void testUpdateUser() {
    User user = fileReaderStore.getUser("testo");
    user.setMail("nvtest@insee.fr");
    Map<String, String> address = new HashMap<>();
    address.put("Ligne1", "Toto");
    address.put("Ligne2", "Chez Toto");
    user.setAddress(address);
    fileWriterStore.updateUser(user,false,null);
    User modifiedUser = fileReaderStore.getUser("testo");
    assertThat("testo should have a new mail", modifiedUser.getMail(), is("nvtest@insee.fr"));
    assertThat("testo should have an address", modifiedUser.getAddress().get("Ligne1"), is("Toto"));
  }

  @Test
  public void testDeleteUser() {
    User user = new User();
    user.setUsername("byebye");
    user.setMail("byebye@toto.fr");
    fileWriterStore.createUser(user);
    assertThat(
        "byebye should have been added", fileReaderStore.getUser("byebye"), not(nullValue()));
    fileWriterStore.deleteUser("byebye");
    assertThat(
        "byebye should have been deleted", fileReaderStore.getUser("byebye"), is(nullValue()));
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
    fileWriterStore.createApplication(application);
    Application retrievedApp = fileReaderStore.getApplication("MyApplication");
    assertThat("MyApplication should have been added", retrievedApp, not(nullValue()));
    assertThat(
        "My application should have groups",
        retrievedApp.getGroups().get(0).getName(),
        is("Group1_MyApplication"));
  }

  @Test
  public void testUpdateApplication() {
    Application application = fileReaderStore.getApplication("Applitest");
    List<Group> groups = new ArrayList<>();
    Group group1 = new Group();
    group1.setName("Group1_Applitest");
    groups.add(group1);
    application.setGroups(groups);
    fileWriterStore.updateApplication(application);
    Application retrievedApplication = fileReaderStore.getApplication("Applitest");
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
    fileWriterStore.createApplication(application);
    assertThat(
        "NotEmptyApplication should exist",
        fileReaderStore.getApplication("NotEmptyApplication"),
        is(not(nullValue())));
    fileWriterStore.deleteApplication("NotEmptyApplication");
    assertThat(
        "NotEmptyApplication should have been deleted",
        fileReaderStore.getApplication("NotEmptyApplication"),
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
    fileWriterStore.createGroup("Applitest", group);
    assertThat(
        "Should retrieve Groupy",
        fileReaderStore.getGroup("Applitest", "Groupy_Applitest").getName(),
        is("Groupy_Applitest"));
  }

  @Test
  public void testDeleteGroup() {
    Group group = new Group();
    group.setName("Asupprimer_WebServicesLdap");
    group.setDescription("supprime ce groupe");
    fileWriterStore.createGroup("WebServicesLdap", group);
    fileWriterStore.deleteGroup("WebServicesLdap", "Asupprimer_WebServicesLdap");
    assertThat(
        "Should have been deleted",
        fileReaderStore.getGroup("WebServicesLdap", "Asupprimer_WebServicesLdap"),
        is(nullValue()));
  }

  @Test
  public void testAddUserInGroup() {
    fileWriterStore.addUserToGroup("Applitest", "Administrateurs_Applitest", "testc");
    assertThat(
        "Group should contain testc",
        fileReaderStore
            .getUsersInGroup("Applitest", "Administrateurs_Applitest")
            .getResults()
            .stream()
            .anyMatch(user -> user.getUsername().equals("testc")));
  }

  @Test
  public void testDeleteUserInGroup() {
    assertThat(
        "Group should be empty",
        fileReaderStore.getUsersInGroup("Applitest", "Administrateurs_Applitest").getResults(),
        is(nullValue()));
    fileWriterStore.addUserToGroup("Applitest", "Administrateurs_Applitest", "testc");
    fileWriterStore.addUserToGroup("Applitest", "Administrateurs_Applitest", "testo");
    fileWriterStore.deleteUserFromGroup("Applitest", "Administrateurs_Applitest", "testo");
    assertThat(
        "Group should not contain testo",
        fileReaderStore
            .getUsersInGroup("Applitest", "Administrateurs_Applitest")
            .getResults()
            .stream()
            .allMatch(
                user ->
                    user == null
                        || user.getUsername() == null
                        || !user.getUsername().equals("testo")));
    assertThat(
        "Group should contain testc",
        fileReaderStore
            .getUsersInGroup("Applitest", "Administrateurs_Applitest")
            .getResults()
            .stream()
            .anyMatch(
                user ->
                    user != null
                        && user.getUsername() != null
                        && user.getUsername().equals("testc")));
  }

  @Test
  public void testUpdateGroup() {
    Group group = fileReaderStore.getGroup("Applitest", "Administrateurs_Applitest");
    group.setDescription("new description");
    fileWriterStore.updateGroup("Applitest", group);
    assertThat(
        "SuperGroup description should be new description",
        fileReaderStore.getGroup("Applitest", "Administrateurs_Applitest").getDescription(),
        is("new description"));
  }

  private String getResourceUrl() {
    try {
      String markUrl =
          resourceLoader.getResource("classpath:/sugoi-file-tests/test").getURL().getPath();
      return "file:" + markUrl.substring(0, markUrl.lastIndexOf("/") + 1);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
