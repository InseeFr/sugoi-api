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
package fr.insee.sugoi.ldap.utils.mapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.unboundid.ldap.sdk.Attribute;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.model.fixtures.StoreMappingFixture;
import fr.insee.sugoi.model.technics.ModelType;
import fr.insee.sugoi.model.technics.StoreMapping;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = UserStorageLdapMapper.class)
public class UserStorageLdapMapperTest {

  @BeforeEach
  public void setup() {}

  @Test
  public void getUserStorageObjectFromAttributes() {
    String[] values = {
      "brancheContact$ou=contacts,ou=clients_domaine1,o=insee,c=fr",
      "brancheAdresse$ou=adresses,ou=clients_domaine1,o=insee,c=fr",
      "brancheOrganisation$ou=organisations,ou=clients_domaine1,o=insee,c=fr"
    };
    Attribute brancheContactAttribute = new Attribute("inseepropriete", values);
    Attribute cnAttribute = new Attribute("cn", "userStorageName");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(cnAttribute);
    attributes.add(brancheContactAttribute);
    UserStorage userStorage = UserStorageLdapMapper.mapFromAttributes(attributes);
    assertThat(
        "Should have userSource",
        userStorage.getUserSource(),
        is("ou=contacts,ou=clients_domaine1,o=insee,c=fr"));
    assertThat(
        "Should have addressSource",
        userStorage.getAddressSource(),
        is("ou=adresses,ou=clients_domaine1,o=insee,c=fr"));
    assertThat(
        "Should have organizationSource",
        userStorage.getOrganizationSource(),
        is("ou=organisations,ou=clients_domaine1,o=insee,c=fr"));
  }

  @Test
  public void getUserStorageMappingFromAttributes() {
    String[] values = {
      "userMapping$username:uid,string,rw",
      "organizationMapping$mail:mail,String,rw",
      "userMapping$mail:mail,String,rw",
    };
    Attribute cnAttribute = new Attribute("cn", "userStorageName");
    Attribute inseeProprieteAttributes = new Attribute("inseepropriete", values);
    UserStorage userStorage =
        UserStorageLdapMapper.mapFromAttributes(List.of(inseeProprieteAttributes, cnAttribute));
    assertThat(
        "userMapping should have a username mapping",
        userStorage.getUserMappings().stream()
            .anyMatch(v -> v.equals(new StoreMapping("username", "uid", ModelType.STRING, true))));
    assertThat(
        "userMapping should have a mail mapping",
        userStorage.getUserMappings().stream()
            .anyMatch(v -> v.equals(new StoreMapping("mail", "mail", ModelType.STRING, true))));
    assertThat(
        "organizationMapping should have a mail mapping",
        userStorage.getOrganizationMappings().stream()
            .anyMatch(v -> v.equals(new StoreMapping("mail", "mail", ModelType.STRING, true))));
  }

  @Test
  public void getUserStorageObjectClassFromAttributes() {
    String[] values = {
      "user_object_classes$top,person",
    };
    Attribute cnAttribute = new Attribute("cn", "userStorageName");
    Attribute inseeProprieteAttributes = new Attribute("inseepropriete", values);
    UserStorage userStorage =
        UserStorageLdapMapper.mapFromAttributes(List.of(inseeProprieteAttributes, cnAttribute));
    assertThat(
        "user storage should have  user object classes",
        userStorage.getProperties().get(LdapConfigKeys.USER_OBJECT_CLASSES).equals("top,person"));
  }

  @Test
  public void getMappingAttributesFromUserStorage() {
    UserStorage userStorage = new UserStorage();

    userStorage.setUserMappings(StoreMappingFixture.getUserStoreMappings());
    userStorage.setOrganizationMappings(StoreMappingFixture.getOrganizationStoreMappings());
    List<Attribute> attributes = UserStorageLdapMapper.mapToAttributes(userStorage);
    assertThat(
        "Should have inseePropriete with username",
        attributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equalsIgnoreCase("inseepropriete")
                        && attribute
                            .getValue()
                            .equalsIgnoreCase("userMapping$username:uid,String,rw")));
    assertThat(
        "Should have inseePropriete with lastname",
        attributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equalsIgnoreCase("inseepropriete")
                        && attribute
                            .getValue()
                            .equalsIgnoreCase("userMapping$lastName:sn,String,rw")));
    assertThat(
        "Should have inseepropriete with firstname",
        attributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equalsIgnoreCase("inseepropriete")
                        && attribute
                            .getValue()
                            .equalsIgnoreCase("organizationMapping$identifiant:uid,String,rw")));
  }

  @Test
  public void getObjectClassesAttributesFromUserStorage() {
    UserStorage userStorage = new UserStorage();
    userStorage.addProperty(LdapConfigKeys.USER_OBJECT_CLASSES, "top,person,other");
    List<Attribute> attributes = UserStorageLdapMapper.mapToAttributes(userStorage);
    assertThat(
        "Should have inseePropriete with user object classes",
        attributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equalsIgnoreCase("inseepropriete")
                        && attribute
                            .getValue()
                            .equalsIgnoreCase("user_object_classes$top,person,other")));
  }
}
