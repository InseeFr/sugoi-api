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
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.fixtures.StoreMappingFixture;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ApplicationLdapMapper.class)
public class ApplicationLdapMapperFromObjectTest {

  ApplicationLdapMapper applicationLdapMapper;
  List<Attribute> mappedAttributes;
  Application application;

  @BeforeEach
  public void setup() {

    Map<String, String> config = new HashMap<>();
    config.put("app_source", "ou=monappli,ou=Applications,o=insee,c=fr");
    config.put("address_source", "ou=address,o=insee,c=fr");
    applicationLdapMapper =
        new ApplicationLdapMapper(config, StoreMappingFixture.getApplicationStoreMappings());
    application = new Application();
  }

  @Test
  public void getSimpleApplicationAttributesFromJavaObject() {

    application.setName("application");
    List<Attribute> mappedAttributes = applicationLdapMapper.mapToAttributes(application);

    assertThat(
        "Should have name",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("ou")
                        && attribute.getValue().equals("application")));
  }
}
