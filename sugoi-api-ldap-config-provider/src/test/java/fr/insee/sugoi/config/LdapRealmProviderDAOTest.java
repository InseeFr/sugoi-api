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
package fr.insee.sugoi.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.core.configuration.UiMappingService;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.Realm.UIMappingType;
import fr.insee.sugoi.model.technics.ModelType;
import fr.insee.sugoi.model.technics.StoreMapping;
import fr.insee.sugoi.model.technics.UiField;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {EmbeddedLdapAutoConfiguration.class, LdapRealmProviderDAOImpl.class})
@TestPropertySource(locations = "classpath:/application.properties")
public class LdapRealmProviderDAOTest {

  @Autowired LdapRealmProviderDAOImpl ldapRealmProviderDAOImpl;

  @MockBean private UiMappingService uiMappingService;

  @Test
  public void loadUniStorage() {
    Realm realm = ldapRealmProviderDAOImpl.load("domaine1").get();
    assertThat("Should have appSource", realm.getAppSource(), is("applitest"));
    assertThat(
        "Default userstorage should have usersource",
        realm.getUserStorages().get(0).getUserSource(),
        is("ou=contacts,ou=clients_domaine1,o=insee,c=fr"));
  }

  @Test
  public void loadMultiStorage() {
    Realm realm = ldapRealmProviderDAOImpl.load("domaine2").get();
    assertThat("Should have two userstorages", realm.getUserStorages().size(), is(2));
    assertThat(
        "First userstorage is monUserStorage",
        realm.getUserStorages().get(1).getName(),
        is("monUserStorage"));
    assertThat(
        "Second userstorage is autreUserStorage",
        realm.getUserStorages().get(0).getName(),
        is("autreUserStorage"));
  }

  @Test
  public void findAllStorages() {
    List<Realm> realms = ldapRealmProviderDAOImpl.findAll();
    assertThat("Should have 4 realm", realms.size(), is(4));
    assertThat(
        "Should have userstorages",
        realms.get(1).getUserStorages().get(0).getName(),
        is("autreUserStorage"));
  }

  @Test
  public void testCaseInsensitivity() {
    List<Realm> realms = ldapRealmProviderDAOImpl.findAll();
    assertThat("Should have 4 realm", realms.size(), is(4));
    Realm uc =
        realms.stream()
            .filter(r -> r.getName().equalsIgnoreCase("uppercasetest"))
            .findFirst()
            .get();
    assertThat("Should be on localhost", uc.getUrl(), is("localhost"));
    assertThat(
        "userstorage should be on 'ou=contacts,ou=clients_domaine1,o=insee,c=fr'",
        uc.getUserStorages().get(0).getUserSource(),
        is("ou=contacts,ou=clients_domaine1,o=insee,c=fr"));
  }

  @Test
  public void loadRealmWithConfig() {
    Realm realm = ldapRealmProviderDAOImpl.load("domaine1").get();
    assertThat(
        "app_managed_attribute_key should be inseeGroupeDefaut",
        realm.getProperties().get(GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_KEYS_LIST),
        is("inseeGroupeDefaut"));
    assertThat(
        "app_managed_attribute_pattern should be (.*)_$(application)",
        realm.getProperties().get(GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_PATTERNS_LIST),
        is("(.*)_$(application)"));
  }

  @Test
  public void shouldHaveRealmMapping() {
    Realm realm = ldapRealmProviderDAOImpl.load("domaine1").get();
    assertThat(
        "Should have a name mapping",
        realm.getGroupMappings().stream()
            .anyMatch(v -> v.equals(new StoreMapping("name", "cn", ModelType.STRING, true))));

    assertThat(
        "should have a groups mapping",
        realm.getGroupMappings().stream()
            .anyMatch(
                v ->
                    v.equals(
                        new StoreMapping("users", "uniquemember", ModelType.LIST_USER, true))));

    assertThat(
        "Should have an application mapping",
        realm.getApplicationMappings().stream()
            .anyMatch(v -> v.equals(new StoreMapping("name", "ou", ModelType.STRING, true))));
  }

  @Test
  public void shouldHaveUsOneMapping() {
    Realm realm = ldapRealmProviderDAOImpl.load("domaine1").get();
    assertThat(
        "Should have the userMapping",
        realm.getUserStorages().get(0).getUserMappings().stream()
            .anyMatch(
                v -> v.equals(new StoreMapping("firstName", "givenname", ModelType.STRING, true))));

    assertThat(
        "Should have the userMapping",
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
        "Should have the organizationMapping",
        realm.getUserStorages().get(0).getOrganizationMappings().stream()
            .anyMatch(
                v ->
                    v.equals(
                        new StoreMapping(
                            "address", "inseeAdressePostaleDN", ModelType.ADDRESS, true))));

    assertThat(
        "Should have the userMapping",
        realm.getUserStorages().get(0).getUserMappings().stream()
            .anyMatch(
                v ->
                    v.equals(new StoreMapping("groups", "memberOf", ModelType.LIST_GROUP, false))));
  }

  @Test
  public void uiMappingShouldBeOrdered() {
    List<UiField> uiUserMapping =
        ldapRealmProviderDAOImpl
            .load("domaine1")
            .get()
            .getUiMapping()
            .get(UIMappingType.UI_USER_MAPPING);
    assertThat("Should be 25 found uiUserMapping", 25, is(uiUserMapping.size()));
    assertThat("First one should be the id", "Identifiant", is(uiUserMapping.get(0).getName()));
    assertThat(
        "Second one should be the password last modification date",
        "PasswordLastChange",
        is(uiUserMapping.get(1).getName()));
    assertThat("Third one should be the realm field", "Realm", is(uiUserMapping.get(2).getName()));
  }
}
