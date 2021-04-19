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
package fr.insee.sugoi.seealso;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {EmbeddedLdapAutoConfiguration.class, LdapSeeAlsoDecorator.class})
@TestPropertySource(locations = "classpath:/application.properties")
public class LdapSeeAlsoDecoratorTest {

  @Autowired LdapSeeAlsoDecorator ldapSeeAlsoDecorator;

  @Test
  public void testGetStringResourceFromLdapUrl() {
    Object res =
        ldapSeeAlsoDecorator.getResourceFromUrl(
            "ldap://localhost:10389/uid=testc,ou=contacts,ou=clients_domaine1,o=insee,c=fr", "cn");
    assertThat("Resource should be Testy Test", res, is("Testy Test"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testGetListResourceFromLdapUrl() {
    Object res =
        ldapSeeAlsoDecorator.getResourceFromUrl(
            "ldap://localhost:10389/uid=testc,ou=contacts,ou=clients_domaine1,o=insee,c=fr",
            "inseeGroupeDefaut");
    assertThat("Should have 4 habilitations", ((List<String>) res).size(), is(4));
    assertThat(
        "Should have habilitations prop_role_application",
        ((List<String>) res)
            .stream().anyMatch(property -> property.equalsIgnoreCase("prop_role_applitest")));
  }
}
