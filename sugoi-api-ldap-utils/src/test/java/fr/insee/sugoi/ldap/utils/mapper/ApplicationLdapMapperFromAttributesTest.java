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
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.fixtures.StoreMappingFixture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ApplicationLdapMapper.class)
public class ApplicationLdapMapperFromAttributesTest {

  ApplicationLdapMapper applicationLdapMapper;

  @BeforeEach
  public void setup() {

    Map<String, String> config = new HashMap<>();
    config.put("address_source", "ou=address,o=insee,c=fr");
    config.put("app_source", "ou=organisations,ou=clients_domaine1,o=inese,c=fr");

    applicationLdapMapper =
        new ApplicationLdapMapper(config, StoreMappingFixture.getApplicationStoreMappings());
  }

  @Test
  public void getSimpleApplicationFromAttributes() {

    Attribute nameAttribute = new Attribute("ou", "appli");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(nameAttribute);
    Application mappedApplication = applicationLdapMapper.mapFromAttributes(attributes);

    assertThat("Should have a name", mappedApplication.getName(), is("appli"));
  }
}
