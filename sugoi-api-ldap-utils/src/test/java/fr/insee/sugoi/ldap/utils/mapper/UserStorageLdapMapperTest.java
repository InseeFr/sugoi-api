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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import com.unboundid.ldap.sdk.Attribute;
import fr.insee.sugoi.model.UserStorage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = UserStorageLdapMapper.class)
public class UserStorageLdapMapperTest {

  UserStorageLdapMapper userStorageLdapMapper = new UserStorageLdapMapper();

  @BeforeEach
  public void setup() {}

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
        "Only userMapping and organizationMapping are in the map of map",
        userStorage.getMappings().size(),
        is(2));
    assertThat(
        "userMapping should have a username mapping",
        userStorage.getMappings().get("userMapping").get("username"),
        is("uid,string,rw"));
    assertThat(
        "userMapping should have a mail mapping",
        userStorage.getMappings().get("userMapping").get("mail"),
        is("mail,String,rw"));
    assertThat(
        "organizationMapping should have a mail mapping",
        userStorage.getMappings().get("organizationMapping").get("mail"),
        is("mail,String,rw"));
  }

  @Test
  public void getMappingAttributesFromUserStorage() {
    UserStorage userStorage = new UserStorage();
    Map<String, String> usermapping = new HashMap<>();
    Map<String, String> organizationmapping = new HashMap<>();
    usermapping.put("username", "uid,String,rw");
    usermapping.put("lastName", "sn,String,rw");
    organizationmapping.put("firstName", "givenname,String,rw");
    Map<String, Map<String, String>> mappings = new HashMap<>();
    mappings.put("userMapping", usermapping);
    mappings.put("organizationMapping", organizationmapping);
    userStorage.setMappings(mappings);
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
                                "organizationMapping$firstName:givenname,String,rw")));
  }
}
