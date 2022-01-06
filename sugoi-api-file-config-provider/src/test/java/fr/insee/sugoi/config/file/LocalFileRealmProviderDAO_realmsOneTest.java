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

import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.technics.ModelType;
import fr.insee.sugoi.model.technics.StoreMapping;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

@SpringBootTest(classes = LocalFileRealmProviderDAO.class)
@TestPropertySource(
    properties = "fr.insee.sugoi.realm.config.local.path=classpath:/realms-one.json")
public class LocalFileRealmProviderDAO_realmsOneTest {

  @Autowired RealmProvider localFileConfig;

  @Test
  public void shouldBeLocalFileRealmProvider() {
    assertThat(
        "RealmProvider should be an instance of LocalFileRealmProviderDAO",
        localFileConfig,
        isA(LocalFileRealmProviderDAO.class));
  }

  @Test
  public void shouldHaveOnlyOneRealm() {
    assertThat("There should be only one realm", localFileConfig.findAll().size(), is(1));
  }

  @Test
  public void shouldFetchTestRealm() {
    assertThat(
        "We should have a realm test", localFileConfig.load("test").get().getName(), is("test"));
    assertThat(
        "We should have a realm TeSt", localFileConfig.load("TeSt").get().getName(), is("test"));
  }

  @Test
  public void shouldHaveTwoUserstorages() {
    Realm realm = localFileConfig.load("test").get();
    assertThat(
        "We should have a userstorage default",
        realm.getUserStorages().stream()
            .anyMatch(userstorage -> userstorage.getName().equals("default")));
    assertThat(
        "We should have a userstorage other",
        realm.getUserStorages().stream()
            .anyMatch(userstorage -> userstorage.getName().equals("other")));
  }

  @Test
  public void shouldHaveRealmMapping() {
    Realm realm = localFileConfig.load("test").get();
    assertThat(
        "Should have groupMapping",
        realm.getGroupMappings().stream()
            .anyMatch(v -> v.equals(new StoreMapping("name", "cn", ModelType.STRING, true))));

    assertThat(
        "Should have groupMapping",
        realm.getGroupMappings().stream()
            .anyMatch(
                v ->
                    v.equals(
                        new StoreMapping("users", "uniquemember", ModelType.LIST_USER, true))));

    assertThat(
        "Should have applicationMapping",
        realm.getApplicationMappings().stream()
            .anyMatch(v -> v.equals(new StoreMapping("name", "ou", ModelType.STRING, true))));
  }

  @Test
  public void shouldHaveUsOneMapping() {
    Realm realm = localFileConfig.load("test").get();
    assertThat(
        "Should have the userMapping",
        realm.getUserStorages().get(0).getUserMappings().stream()
            .anyMatch(
                v -> v.equals(new StoreMapping("firstName", "givenname", ModelType.STRING, true))));

    assertThat(
        "Should have userMapping",
        realm.getUserStorages().get(0).getUserMappings().stream()
            .anyMatch(
                v ->
                    v.equals(
                        new StoreMapping(
                            "attributes.insee_roles_applicatifs",
                            "inseeRoleApplicatif",
                            ModelType.LIST_STRING,
                            true))));

    assertThat(
        "Should have the organizationMapping",
        realm.getUserStorages().get(0).getOrganizationMappings().stream()
            .anyMatch(
                v ->
                    v.equals(new StoreMapping("attributes.mail", "mail", ModelType.STRING, true))));
    assertThat(
        "Should have organizationMapping",
        realm.getUserStorages().get(0).getOrganizationMappings().stream()
            .anyMatch(
                v ->
                    v.equals(
                        new StoreMapping(
                            "address", "inseeAdressePostaleDN", ModelType.ADDRESS, true))));
  }
}
