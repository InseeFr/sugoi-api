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

import com.unboundid.ldap.sdk.Attribute;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = GroupLdapMapper.class)
public class GroupLdapMapperFromObjectTest {

  GroupLdapMapper groupLdapMapper;
  List<Attribute> mappedAttributes;
  Group group;

  @BeforeEach
  public void setup() {

    Map<String, String> config = new HashMap<>();
    config.put("address_source", "ou=address,o=insee,c=fr");
    config.put("user_source", "ou=contacts,o=insee,c=fr");
    groupLdapMapper = new GroupLdapMapper(config);

    group = new Group();
  }

  @Test
  public void getSimpleGroupAttributesFromJavaObject() {

    group.setName("group");
    group.setDescription("le groupe");
    List<Attribute> mappedAttributes = groupLdapMapper.mapToAttributes(group);

    assertThat(
        "Should have name",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("cn") && attribute.getValue().equals("group")));
    assertThat(
        "Should have description",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("description")
                        && attribute.getValue().equals("le groupe")));
  }

  @Test
  public void getGroupGroupAttributesFromJavaObject() {

    User user1 = new User("user1");
    User user2 = new User("user2");
    List<User> users = new ArrayList<>();
    users.add(user1);
    users.add(user2);
    group.setUsers(users);
    List<Attribute> mappedAttributes = groupLdapMapper.mapToAttributes(group);

    assertThat(
        "Should not have user user1",
        !(mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("uniquemember")
                        && attribute.getValue().equals("uid=user1,ou=contacts,o=insee,c=fr"))));
    assertThat(
        "Should not have user user2",
        !(mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("uniquemember")
                        && attribute.getValue().equals("uid=user2,ou=contacts,o=insee,c=fr"))));
  }

  @Test
  public void emptyStringShouldBeRemoved() {
    group.setName("group");
    group.setDescription("");
    List<Attribute> mappedAttributes = groupLdapMapper.mapToAttributes(group);
    assertThat(
        "Should not have a description attribute",
        mappedAttributes.stream()
            .allMatch(attribute -> !(attribute.getName().equals("description"))));
  }
}
