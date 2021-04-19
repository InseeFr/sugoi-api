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
package fr.insee.sugoi.core.service;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import fr.insee.sugoi.core.seealso.MapProtocolSeeAlsoDecorator;
import fr.insee.sugoi.core.seealso.SeeAlsoService;
import fr.insee.sugoi.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {SeeAlsoService.class, MapProtocolSeeAlsoDecorator.class})
public class SeeAlsoServiceTest {

  @Mock MapProtocolSeeAlsoDecorator mapProtocolSeeAlsoDecorator;
  @InjectMocks SeeAlsoService seeAlsoService;

  @BeforeEach
  public void setup() {
    when(mapProtocolSeeAlsoDecorator.getResourceFromUrl(any(), any(), any())).thenReturn("toto");
  }

  @Test
  public void decorateWithSeeAlsoTest() {
    User user = new User();
    seeAlsoService.decorateWithSeeAlso(
        user,
        "ldap://localhost:10389/uid=testc,ou=contacts,ou=clients_domaine1,o=insee,c=fr|cn|ldap_string");
    assertThat(
        "Should add toto to ldap_string", user.getAttributes().get("ldap_string"), is("toto"));
  }
}
