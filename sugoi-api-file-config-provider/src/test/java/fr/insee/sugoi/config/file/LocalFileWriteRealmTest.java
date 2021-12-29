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
package fr.insee.sugoi.config.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.model.technics.ModelType;
import fr.insee.sugoi.model.technics.StoreMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = LocalFileRealmProviderDAO.class)
@TestPropertySource(
        properties = "fr.insee.sugoi.realm.config.local.path=classpath:/realms-write.json")
public class LocalFileWriteRealmTest {

  @Autowired
  ResourceLoader resourceLoader;
  @Autowired
  LocalFileRealmProviderDAO localFileConfig;

  private final ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  public void setup() {
    Resource realmsResource = resourceLoader.getResource("classpath:/realms-write.json");
    try {
      File file = realmsResource.getFile();
      FileWriter fWriter = new FileWriter(file);
      Realm toDeleteRealm = new Realm();
      toDeleteRealm.setName("todelete");
      toDeleteRealm.setUrl("localhost");
      UserStorage userStorage1 = new UserStorage();
      userStorage1.setName("toto");
      userStorage1.setUserSource("totosource");
      UserStorage userStorage2 = new UserStorage();
      userStorage2.setName("tata");
      userStorage2.setUserSource("tatasource");
      toDeleteRealm.setUserStorages(List.of(userStorage1, userStorage2));
      Realm toModifyRealm = new Realm();
      toModifyRealm.setName("tomodify");
      toModifyRealm.setUrl("localhost");
      UserStorage userStorageToModify = new UserStorage();
      userStorageToModify.setUserSource("usersource");
      toModifyRealm.setUserStorages(List.of(userStorageToModify));
      fWriter.write(mapper.writeValueAsString(List.of(toDeleteRealm, toModifyRealm)));
      fWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void addNewRealmWithOneUserStorageTest() {
    Realm realmToAdd = new Realm();
    realmToAdd.setName("toadd");
    realmToAdd.setUrl("localhost");
    UserStorage uniqueUserStorage = new UserStorage();
    uniqueUserStorage.setUserSource("ou=SSM,o=insee,c=fr");
    uniqueUserStorage.setOrganizationSource("ou=organisations,ou=clients_domaine2,o=insee,c=fr");
    uniqueUserStorage.addProperty("group_filter_pattern", "toto");
    realmToAdd.setUserStorages(List.of(uniqueUserStorage));
    assertThat("Realm should not exist", localFileConfig.load("toadd").isEmpty());
    localFileConfig.createRealm(realmToAdd, null);
    Realm retrievedRealm = localFileConfig.load("toadd").get();
    assertThat("Realm should be present", retrievedRealm.getName(), is("toadd"));
    assertThat("Realm should have an url", retrievedRealm.getUrl(), is("localhost"));
    assertThat(
        "Realm should have a usersource",
        retrievedRealm.getUserStorages().get(0).getUserSource(),
        is("ou=SSM,o=insee,c=fr"));
    assertThat(
        "Realm should have an organizationsource",
        retrievedRealm.getUserStorages().get(0).getOrganizationSource(),
        is("ou=organisations,ou=clients_domaine2,o=insee,c=fr"));
    assertThat(
        "Realm should have a groupfilterpattern",
        retrievedRealm.getUserStorages().get(0).getProperties().get("group_filter_pattern"),
        is("toto"));
  }

  @Test
  public void addNewRealmWithTwoUserStoragesTest() {
    Realm realmToAdd = new Realm();
    realmToAdd.setName("multistorage");
    realmToAdd.setUrl("localhost");
    UserStorage userStorage1 = new UserStorage();
    userStorage1.setName("first");
    userStorage1.setUserSource("ou=SSM,o=insee,c=fr");
    userStorage1.setOrganizationSource("ou=organisations,ou=clients_domaine2,o=insee,c=fr");
    userStorage1.addProperty("group_filter_pattern", "toto");
    UserStorage userStorage2 = new UserStorage();
    userStorage2.setName("second");
    userStorage2.setUserSource("ou=SSM,o=insee,c=fr");
    userStorage2.setOrganizationSource("ou=organisations,ou=clients_domaine2,o=insee,c=fr");
    userStorage2.addProperty("group_filter_pattern", "toto");
    List<UserStorage> userStorages = new ArrayList<>();
    userStorages.add(userStorage1);
    userStorages.add(userStorage2);
    realmToAdd.setUserStorages(userStorages);
    assertThat("Realm should not exist", localFileConfig.load("toadd").isEmpty());
    localFileConfig.createRealm(realmToAdd, null);
    Realm retrievedRealm = localFileConfig.load("multistorage").get();
    assertThat("Realm should be present", retrievedRealm.getName(), is("multistorage"));
    assertThat(
        "Realm should have two userstorages", retrievedRealm.getUserStorages().size(), is(2));
    assertThat(
        "First realm has usersource",
        retrievedRealm.getUserStorages().get(0).getUserSource(),
        is("ou=SSM,o=insee,c=fr"));
    assertThat(
        "Realm should have a groupfilterpattern",
        retrievedRealm.getUserStorages().get(0).getProperties().get("group_filter_pattern"),
        is("toto"));
  }

  @Test
  public void deleteRealmWithOneStorageTest() {
    assertThat("Realm should be present", localFileConfig.load("todelete").isPresent());
    localFileConfig.deleteRealm("todelete", null);
    assertThat("Realm should not exist", localFileConfig.load("toadd").isEmpty());
  }

  @Test
  public void changeRealmUserSourceTest() {
    Realm realmToModify = localFileConfig.load("tomodify").get();
    UserStorage userStorage = realmToModify.getUserStorages().get(0);
    userStorage.setUserSource("ou=contacts,ou=clients_domaine2,o=insee,c=fr");
    localFileConfig.updateRealm(realmToModify, null);
    assertThat(
        "User source should change to domaine2",
        localFileConfig.load("tomodify").get().getUserStorages().get(0).getUserSource(),
        is("ou=contacts,ou=clients_domaine2,o=insee,c=fr"));
  }

  @Test
  public void changeRealmUrlTest() {
    Realm realmToModify = localFileConfig.load("tomodify").get();
    realmToModify.setUrl("new_url");
    localFileConfig.updateRealm(realmToModify, null);
    assertThat(
        "Url should have changed", localFileConfig.load("tomodify").get().getUrl(), is("new_url"));
  }

  @Test
  public void addApplicationMappingTest() {
    Realm realmToModify = localFileConfig.load("tomodify").get();
    realmToModify.setApplicationMappings(new ArrayList<>());
    realmToModify.getApplicationMappings().add(new StoreMapping("name", "ou", ModelType.STRING, true));
    localFileConfig.updateRealm(realmToModify, null);
    assertThat(
            "Application mapping should have a name",
            localFileConfig.load("tomodify").get().getApplicationMappings().stream().anyMatch(v -> v.equals(new StoreMapping("name", "ou", ModelType.STRING, true))));
  }

  @Test
  public void addOrganizationMappingTest() {
    Realm realmToModify = localFileConfig.load("tomodify").get();
    realmToModify.getUserStorages().get(0).setOrganizationMappings(new ArrayList<>());
    realmToModify
            .getUserStorages()
            .get(0)
            .getOrganizationMappings().add(new StoreMapping("address", "inseeAdressePostaleDN", ModelType.ADDRESS, true));
    localFileConfig.updateRealm(realmToModify, null);
    assertThat(
            "Organization mapping should have an address",
            localFileConfig
                    .load("tomodify")
                    .get()
                    .getUserStorages()
                    .get(0)
                    .getOrganizationMappings().stream().anyMatch(v -> v.equals(new StoreMapping("address", "inseeAdressePostaleDN", ModelType.ADDRESS, true))));
  }
}
