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
import fr.insee.sugoi.model.Habilitation;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = UserLdapMapper.class)
public class UserLdapMapperFromObjectTest {

  UserLdapMapper userLdapMapper;
  List<Attribute> mappedAttributes;
  User user;

  @BeforeEach
  public void setup() {

    Map<String, String> config = new HashMap<>();
    config.put("organization_source", "ou=organisations,o=insee,c=fr");
    config.put("address_source", "ou=address,o=insee,c=fr");
    userLdapMapper = new UserLdapMapper(config);

    user = new User();
  }

  @Test
  public void getSimpleUserAttributesFromJavaObject() {

    user.setUsername("Toto");
    user.setFirstName("Toto");
    user.setLastName("Tata");
    user.setMail("toto@insee.fr");
    List<Attribute> mappedAttributes = userLdapMapper.mapToAttributes(user);

    assertThat(
        "Should have username",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("uid") && attribute.getValue().equals("Toto")));
    assertThat(
        "Should have last name",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("sn") && attribute.getValue().equals("Tata")));
    assertThat(
        "Should have first name",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("givenName")
                        && attribute.getValue().equals("Toto")));
    assertThat(
        "Should have mail",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("mail")
                        && attribute.getValue().equals("toto@insee.fr")));
  }

  @Test
  public void getUserAttributesAttributesFromJavaObject() {

    user.addAttributes("common_name", "TotoCommun");
    user.addAttributes("phone_number", "012345678");
    user.addAttributes("description", "Ceci est un user");
    user.addAttributes("personal_title", "Camarade");
    List<Attribute> mappedAttributes = userLdapMapper.mapToAttributes(user);

    assertThat(
        "Should have cn",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("cn") && attribute.getValue().equals("TotoCommun")));

    assertThat(
        "Should have phone_number",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("telephoneNumber")
                        && attribute.getValue().equals("012345678")));

    assertThat(
        "Should have description",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("description")
                        && attribute.getValue().equals("Ceci est un user")));

    assertThat(
        "Should have personal title",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("personalTitle")
                        && attribute.getValue().equals("Camarade")));
  }

  @Test
  public void getUserAddressAttributesFromJavaObject() {

    Map<String, String> address = new HashMap<>();
    address.put("ligne1", "33 rue des Fleurs");
    address.put("ligne2", "56700 Fleurville");
    address.put("id", "generatedBefore");
    user.setAddress(address);
    List<Attribute> mappedAttributes = userLdapMapper.mapToAttributes(user);

    assertThat(
        "Should have address attribute",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("inseeAdressePostaleDN")
                        && attribute
                            .getValue()
                            .equals("l=generatedBefore,ou=address,o=insee,c=fr")));
  }

  @Test
  public void getUserHabilitationsAttributeFromJavaObject() {

    Habilitation habilitation1 = new Habilitation("property_role_application");
    Habilitation habilitation2 = new Habilitation("property_role_application2");
    List<Habilitation> habilitations = new ArrayList<>();
    habilitations.add(habilitation1);
    habilitations.add(habilitation2);
    user.setHabilitations(habilitations);
    List<Attribute> mappedAttributes = userLdapMapper.mapToAttributes(user);

    assertThat(
        "Should have first habilitation in inseeGroupeDefault",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("inseeGroupeDefaut")
                        && attribute.getValue().equals("property_role_application")));

    assertThat(
        "Should have second habilitation in inseeGroupeDefault",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("inseeGroupeDefaut")
                        && attribute.getValue().equals("property_role_application2")));
  }

  @Test
  public void getUserOrganizationAttributeFromJavaObject() {

    Organization organization = new Organization();
    organization.setIdentifiant("SuperOrga");
    user.setOrganization(organization);
    List<Attribute> mappedAttributes = userLdapMapper.mapToAttributes(user);

    assertThat(
        "Should have SuperOrga link",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("inseeOrganisationDN")
                        && attribute
                            .getValue()
                            .equals("uid=SuperOrga,ou=organisations,o=insee,c=fr")));
  }
}
