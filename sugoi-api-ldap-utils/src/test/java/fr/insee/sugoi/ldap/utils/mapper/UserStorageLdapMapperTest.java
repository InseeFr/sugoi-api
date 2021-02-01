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
}
