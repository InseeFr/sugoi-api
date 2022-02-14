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
import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.RealmConfigKeys;
import fr.insee.sugoi.model.fixtures.StoreMappingFixture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = OrganizationLdapMapper.class)
public class OrganizationLdapMapperFromAttributesTest {

  OrganizationLdapMapper organizationLdapMapper;

  @BeforeEach
  public void setup() {

    Map<RealmConfigKeys, String> config = new HashMap<>();
    config.put(GlobalKeysConfig.ADDRESS_SOURCE, "ou=address,o=insee,c=fr");
    config.put(
        GlobalKeysConfig.ORGANIZATION_SOURCE, "ou=organisations,ou=clients_domaine1,o=insee,c=fr");

    organizationLdapMapper =
        new OrganizationLdapMapper(config, StoreMappingFixture.getOrganizationStoreMappings());
  }

  @Test
  public void getSimpleOrganizationFromAttributes() {

    Attribute idAttribute = new Attribute("uid", "orga");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(idAttribute);
    Organization mappedOrganization = organizationLdapMapper.mapFromAttributes(attributes);

    assertThat("Should have an id", mappedOrganization.getIdentifiant(), is("orga"));
  }

  @Test
  public void getOrganizationAttributesFromAttributes() {

    Attribute descriptionAttribute = new Attribute("description", "ma description");
    Attribute mailAttribute = new Attribute("mail", "orga@insee.fr");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(descriptionAttribute);
    attributes.add(mailAttribute);
    Organization mappedOrganization = organizationLdapMapper.mapFromAttributes(attributes);

    assertThat(
        "Should hava a mail", mappedOrganization.getAttributes().get("mail"), is("orga@insee.fr"));
    assertThat(
        "Should hava a description",
        mappedOrganization.getAttributes().get("description"),
        is("ma description"));
  }

  @Test
  public void getOrganizationOrganizationFromAttributes() {

    Attribute organizationAttribute =
        new Attribute(
            "inseeOrganisationDN",
            "uid=orgaDorga,ou=organisations,ou=clients_domaine1,o=insee,c=fr");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(organizationAttribute);
    Organization mappedOrganization = organizationLdapMapper.mapFromAttributes(attributes);

    assertThat(
        "Should have organization",
        mappedOrganization.getOrganization().getIdentifiant(),
        is("orgaDorga"));
  }

  @Test
  public void getOrganizationAddressFromAttributes() {

    Attribute addressAttribute =
        new Attribute("inseeAdressePostaleDN", "l=generatedBefore,ou=address,o=insee,c=fr");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(addressAttribute);
    Organization mappedOrganization = organizationLdapMapper.mapFromAttributes(attributes);

    assertThat(
        "Should have address id", mappedOrganization.getAddress().getId(), is("generatedBefore"));
  }
}
