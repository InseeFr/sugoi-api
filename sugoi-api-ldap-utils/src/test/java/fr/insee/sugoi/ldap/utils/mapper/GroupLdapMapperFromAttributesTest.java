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
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.fixtures.StoreMappingFixture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = GroupLdapMapper.class)
public class GroupLdapMapperFromAttributesTest {

  GroupLdapMapper groupLdapMapper;

  @BeforeEach
  public void setup() {

    Map<String, String> config = new HashMap<>();
    config.put("address_source", "ou=address,o=insee,c=fr");
    config.put("organization_source", "ou=organisations,ou=clients_domaine1,o=insee,c=fr");
    groupLdapMapper = new GroupLdapMapper(config, StoreMappingFixture.getGroupStoreMappings());
  }

  @Test
  public void getSimpleGroupFromAttributes() {

    Attribute groupAttribute = new Attribute("cn", "superGroupe");
    Attribute descriptionAttribute = new Attribute("description", "une description");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(groupAttribute);
    attributes.add(descriptionAttribute);
    Group mappedGroup = groupLdapMapper.mapFromAttributes(attributes);

    assertThat("Should have a name", mappedGroup.getName(), is("superGroupe"));
    assertThat("Should have a description", mappedGroup.getDescription(), is("une description"));
  }

  @Test
  public void getUsersInGroupFromAttributes() {

    Attribute userAttribute1 = new Attribute("uniqueMember", "uid=tata,ou=contact,o=insee,c=fr");
    Attribute userAttribute2 = new Attribute("uniquemember", "uid=toto,ou=contact,o=insee,c=fr");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(userAttribute1);
    attributes.add(userAttribute2);
    Group mappedGroup = groupLdapMapper.mapFromAttributes(attributes);

    assertThat(
        "Should have tata as user",
        mappedGroup.getUsers().stream().anyMatch(user -> user.getUsername().equals("tata")));
    assertThat(
        "Should have toto as user",
        mappedGroup.getUsers().stream().anyMatch(user -> user.getUsername().equals("toto")));
  }
}
