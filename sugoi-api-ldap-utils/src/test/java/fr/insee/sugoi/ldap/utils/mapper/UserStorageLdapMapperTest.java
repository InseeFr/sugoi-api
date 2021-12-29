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

import com.unboundid.ldap.sdk.Attribute;
import fixtures.StoreMappingFixture;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.model.technics.ModelType;
import fr.insee.sugoi.model.technics.StoreMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = UserStorageLdapMapper.class)
public class UserStorageLdapMapperTest {

  UserStorageLdapMapper userStorageLdapMapper = new UserStorageLdapMapper();

  @BeforeEach
  public void setup() {
  }

  @Test
  public void getUserStorageObjectFromAttributes() {
    String[] values = {
      "brancheContact$ou=contacts,ou=clients_domaine1,o=insee,c=fr",
      "brancheAdresse$ou=adresses,ou=clients_domaine1,o=insee,c=fr",
      "brancheOrganisation$ou=organisations,ou=clients_domaine1,o=insee,c=fr",
      "groupSourcePattern$ou={appliname}_Objets,ou={appliname},ou=Applications,o=insee,c=fr",
      "groupFilterPattern$(cn={group}_{appliname})"
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
    assertThat(
        "Should have group source pattern",
        userStorage.getProperties().get("group_source_pattern"),
        is("ou={appliname}_Objets,ou={appliname},ou=Applications,o=insee,c=fr"));
    assertThat(
        "Should have group filter pattern",
        userStorage.getProperties().get("group_filter_pattern"),
        is("(cn={group}_{appliname})"));
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
            userStorage.getUserMappings().stream().anyMatch(v -> v.equals(new StoreMapping("username", "uid", ModelType.STRING, true))));
    assertThat(
            "userMapping should have a mail mapping",
            userStorage.getUserMappings().stream().anyMatch(v -> v.equals(new StoreMapping("mail", "mail", ModelType.STRING, true))));
    assertThat(
            "organizationMapping should have a mail mapping",
            userStorage.getOrganizationMappings().stream().anyMatch(v -> v.equals(new StoreMapping("mail", "mail", ModelType.STRING, true))));
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
                                            .equalsIgnoreCase(
                                                    "organizationMapping$identifiant:uid,String,rw")));
  }

}
