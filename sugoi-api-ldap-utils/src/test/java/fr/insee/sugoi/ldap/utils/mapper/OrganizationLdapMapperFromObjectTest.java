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

import com.unboundid.ldap.sdk.Attribute;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.PostalAddress;
import fr.insee.sugoi.model.fixtures.StoreMappingFixture;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = OrganizationLdapMapper.class)
public class OrganizationLdapMapperFromObjectTest {

  OrganizationLdapMapper organizationLdapMapper;
  List<Attribute> mappedAttributes;
  Organization organization;

  @BeforeEach
  public void setup() {

    Map<String, String> config = new HashMap<>();
    config.put("organization_source", "ou=organisations,o=insee,c=fr");
    config.put("address_source", "ou=address,o=insee,c=fr");
    organizationLdapMapper =
        new OrganizationLdapMapper(config, StoreMappingFixture.getOrganizationStoreMappings());

    organization = new Organization();
  }

  @Test
  public void getSimpleOrganizationAttributesFromJavaObject() {

    organization.setIdentifiant("orga");
    List<Attribute> mappedAttributes =
        organizationLdapMapper.mapToAttributesForCreation(organization);

    assertThat(
        "Should have id",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("uid") && attribute.getValue().equals("orga")));
  }

  @Test
  public void getOrganizationAttributesAttributesFromJavaObject() {

    organization.addAttributes("description", "Ceci est une organization");
    organization.addAttributes("mail", "orga@insee.fr");
    List<Attribute> mappedAttributes =
        organizationLdapMapper.mapToAttributesForCreation(organization);

    assertThat(
        "Should have description",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("description")
                        && attribute.getValue().equals("Ceci est une organization")));

    assertThat(
        "Should have mail",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("mail")
                        && attribute.getValue().equals("orga@insee.fr")));
  }

  @Test
  public void getOrganizationAddressAttributesFromJavaObject() {

    PostalAddress address = new PostalAddress("generatedBefore");
    address.setLines(new String[] {"33 rue des Fleurs", "56700 Fleurville"});

    organization.setAddress(address);
    List<Attribute> mappedAttributes =
        organizationLdapMapper.mapToAttributesForCreation(organization);

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
  public void getOrganizationOrganizationAttributesFromJavaObject() {

    Organization organizationOfOrganization = new Organization();
    organizationOfOrganization.setIdentifiant("orgaDorga");
    organization.setOrganization(organizationOfOrganization);
    List<Attribute> mappedAttributes =
        organizationLdapMapper.mapToAttributesForCreation(organization);

    assertThat(
        "Should have dependent organization",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("inseeOrganisationDN")
                        && attribute
                            .getValue()
                            .equals("uid=orgaDorga,ou=organisations,o=insee,c=fr")));
  }
}
