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
package fr.insee.sugoi.core.seealso;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import fr.insee.sugoi.core.seealso.SeeAlsoCredentialsConfiguration.SeeAlsoCredential;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {SeeAlsoCredentialsConfiguration.class})
@TestPropertySource(locations = "classpath:/application.properties")
@EnableConfigurationProperties(value = SeeAlsoCredentialsConfiguration.class)
public class SeeAlsoCredentialsConfigurationTest {

  @Autowired SeeAlsoCredentialsConfiguration seeAlsoCredentialsConfiguration;

  @Test
  public void shouldParseConfig() {
    Map<String, SeeAlsoCredential> credentialsByHost =
        seeAlsoCredentialsConfiguration.seeAlsoCredentialsByHost();
    assertThat("Should only contain 3 element", credentialsByHost.size(), is(3));
    SeeAlsoCredential localhostCredential = credentialsByHost.get("localhost");
    assertThat(
        "Localhost credential should have appropriate username",
        localhostCredential.getUsername(),
        is("user"));
    assertThat(
        "Localhost credential should have appropriate password",
        localhostCredential.getPassword(),
        is("pa:ssword"));
    SeeAlsoCredential localhostCredential2 = credentialsByHost.get("localhost2");
    assertThat(
        "Localhost2 credential should have appropriate username",
        localhostCredential2.getUsername(),
        is("toto"));
    assertThat(
        "Localhost2 credential should have appropriate password",
        localhostCredential2.getPassword(),
        is("$ti\\ti"));
    SeeAlsoCredential domainWithDot = credentialsByHost.get("insee.fr");
    assertThat(
        "Domain with a dot should have appropriate password",
        domainWithDot.getPassword(),
        is("pa;ssword"));
  }
}
