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
import fr.insee.sugoi.model.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = UserLdapMapper.class)
public class UserLdapMapperFromAttributesTest {

  UserLdapMapper userLdapMapper;

  @BeforeEach
  public void setup() {

    Map<String, String> config = new HashMap<>();
    config.put("organization_source", "ou=organisations,ou=clients_domaine1,o=insee,c=fr");
    config.put(
        "group_source_pattern",
        "ou={appliname}_Objets,ou={appliname},ou=Applications,o=insee,c=fr");
    Map<String, String> mapping = new HashMap<>();
    mapping.put("username", "uid,String,rw");
    mapping.put("lastName", "sn,String,rw");
    mapping.put("mail", "mail,String,rw");
    mapping.put("firstName", "givenname,String,rw");
    mapping.put("attributes.common_name", "cn,String,rw");
    mapping.put("attributes.personal_title", "personalTitle,String,rw");
    mapping.put("attributes.description", "description,String,rw");
    mapping.put("attributes.phone_number", "telephoneNumber,String,rw");
    mapping.put("habilitations", "inseeGroupeDefaut,list_habilitation,rw");
    mapping.put("organization", "inseeOrganisationDN,organization,rw");
    mapping.put("address", "inseeAdressePostaleDN,address,rw");
    mapping.put("groups", "memberOf,list_group,ro");
    mapping.put("attributes.insee_roles_applicatifs", "inseeRoleApplicatif,list_string,rw");
    mapping.put("attributes.hasPassword", "userPassword,exists,ro");
    userLdapMapper = new UserLdapMapper(config, mapping);
  }

  @Test
  public void getSimpleUserFromAttributes() {

    Attribute lastNameAttribute = new Attribute("sn", "Toto");
    Attribute firstNameAttribute = new Attribute("givenName", "Tata");
    Attribute mailAttribute = new Attribute("mail", "toto@tata.insee.fr");
    Attribute usernameAttribute = new Attribute("uid", "totoid");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(lastNameAttribute);
    attributes.add(firstNameAttribute);
    attributes.add(mailAttribute);
    attributes.add(usernameAttribute);
    User mappedUser = userLdapMapper.mapFromAttributes(attributes);

    assertThat("Should have a username", mappedUser.getUsername(), is("totoid"));
    assertThat("Should have a lastname", mappedUser.getLastName(), is("Toto"));
    assertThat("Should have a firstname", mappedUser.getFirstName(), is("Tata"));
    assertThat("Should have a mail", mappedUser.getMail(), is("toto@tata.insee.fr"));
  }

  @Test
  public void getUserAttributesFromAttributes() {

    Attribute commonNameAttribute = new Attribute("cn", "Toto Tata");
    Attribute personalTitleAttribute = new Attribute("personalTitle", "Camarade");
    Attribute descriptionAttribute = new Attribute("description", "ma description");
    Attribute telAttribute = new Attribute("telephoneNumber", "012345678");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(commonNameAttribute);
    attributes.add(personalTitleAttribute);
    attributes.add(descriptionAttribute);
    attributes.add(telAttribute);
    User mappedUser = userLdapMapper.mapFromAttributes(attributes);

    assertThat("Should hava a cn", mappedUser.getAttributes().get("common_name"), is("Toto Tata"));
    assertThat(
        "Should hava a phone_number",
        mappedUser.getAttributes().get("phone_number"),
        is("012345678"));
    assertThat(
        "Should hava a personalTitle",
        mappedUser.getAttributes().get("personal_title"),
        is("Camarade"));
    assertThat(
        "Should hava a description",
        mappedUser.getAttributes().get("description"),
        is("ma description"));
  }

  @Test
  public void getUserHabilitationsFromAttributes() {

    Attribute habilitationAttribute1 =
        new Attribute("inseeGroupeDefaut", "property_role_application");
    Attribute habilitationWithoutPropAttribute =
        new Attribute("inseeGroupeDefaut", "role_application2");
    Attribute malformedHabilitation = new Attribute("inseeGroupeDefaut", "toto");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(habilitationAttribute1);
    attributes.add(habilitationWithoutPropAttribute);
    attributes.add(malformedHabilitation);
    User mappedUser = userLdapMapper.mapFromAttributes(attributes);

    assertThat(
        "Should have habilitation habilitation1",
        mappedUser.getHabilitations().stream()
            .anyMatch(habilitation -> habilitation.getApplication().equals("application")));
    assertThat(
        "Should have habilitation without prop",
        mappedUser.getHabilitations().stream()
            .anyMatch(
                habilitation ->
                    habilitation.getApplication().equals("application2")
                        && habilitation.getProperty() == null));
    assertThat(
        "All habilitation have role",
        mappedUser.getHabilitations().stream()
            .allMatch(habilitation -> habilitation.getRole() != null));
  }

  @Test
  public void getUserOrganizationFromAttributes() {

    Attribute organizationAttribute =
        new Attribute(
            "inseeOrganisationDN", "uid=monOrga,ou=organisations,ou=clients_domaine1,o=insee,c=fr");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(organizationAttribute);
    User mappedUser = userLdapMapper.mapFromAttributes(attributes);

    assertThat(
        "Should have organization", mappedUser.getOrganization().getIdentifiant(), is("monOrga"));
  }

  @Test
  public void getUserAddressFromAttributes() {

    Attribute addressAttribute =
        new Attribute("inseeAdressePostaleDN", "l=generatedBefore,ou=address,o=insee,c=fr");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(addressAttribute);
    User mappedUser = userLdapMapper.mapFromAttributes(attributes);

    assertThat("Should have address id", mappedUser.getAddress().get("id"), is("generatedBefore"));
  }

  @Test
  public void getUserGroupFromAttributes() {

    Attribute groupAttributes1 =
        new Attribute(
            "memberOf", "cn=admin,ou=monappli_Objets,ou=monappli,ou=Applications,o=insee,c=fr");
    Attribute groupAttributes2 =
        new Attribute(
            "memberOf", "cn=reader,ou=monappli_Objets,ou=monappli,ou=Applications,o=insee,c=fr");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(groupAttributes1);
    attributes.add(groupAttributes2);
    User mappedUser = userLdapMapper.mapFromAttributes(attributes);

    assertThat(
        "Should have admin group",
        mappedUser.getGroups().stream().anyMatch(group -> group.getName().equals("admin")));
    assertThat(
        "Admin group should have monappli app name",
        mappedUser.getGroups().stream().anyMatch(group -> group.getAppName().equals("monappli")));
    assertThat(
        "Should have admin group",
        mappedUser.getGroups().stream().anyMatch(group -> group.getName().equals("reader")));
  }

  @Test
  public void getInseeRolesApplicatifsFromAttributes() {

    Attribute inseeRoleAppAttribute1 = new Attribute("inseeRoleApplicatif", "toto");
    Attribute inseeRoleAppAttribute2 = new Attribute("inseeRoleApplicatif", "tata");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(inseeRoleAppAttribute1);
    attributes.add(inseeRoleAppAttribute2);
    User mappedUser = userLdapMapper.mapFromAttributes(attributes);
    @SuppressWarnings("unchecked")
    List<String> inseeRoleApplicatifs =
        (List<String>) mappedUser.getAttributes().get("insee_roles_applicatifs");

    assertThat(
        "Should have inseeRoleapplicatif tata",
        inseeRoleApplicatifs.stream().anyMatch(role -> role.equals("toto")));
    assertThat(
        "Should have inseeRoleapplicatif tata",
        inseeRoleApplicatifs.stream().anyMatch(role -> role.equals("tata")));
  }

  @Test
  public void getHasPasswordFromAttributeWhenPasswordIsSet() {
    Attribute passwordAttribute = new Attribute("userPassword", "mypassword");
    User mappedUser = userLdapMapper.mapFromAttributes(List.of(passwordAttribute));
    assertThat(mappedUser.getAttributes().get("hasPassword"), is(true));
  }

  @Test
  public void getHasPasswordFromAttributeWhenPasswordIsNotSet() {
    User mappedUser = userLdapMapper.mapFromAttributes(List.of());
    assertThat(mappedUser.getAttributes().get("hasPassword"), is(false));
  }
}
