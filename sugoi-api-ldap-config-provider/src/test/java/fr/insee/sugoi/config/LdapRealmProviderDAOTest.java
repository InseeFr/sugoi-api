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
package fr.insee.sugoi.config;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import fr.insee.sugoi.model.Realm;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {EmbeddedLdapAutoConfiguration.class, LdapRealmProviderDAOImpl.class})
@TestPropertySource(locations = "classpath:/application.properties")
public class LdapRealmProviderDAOTest {

  @Autowired LdapRealmProviderDAOImpl ldapRealmProviderDAOImpl;

  @Test
  public void loadUniStorage() {
    Realm realm = ldapRealmProviderDAOImpl.load("domaine1");
    assertThat("Should have appSource", realm.getAppSource(), is("applitest"));
    assertThat(
        "Default userstorage should have usersource",
        realm.getUserStorages().get(0).getUserSource(),
        is("ou=contacts,ou=clients_domaine1,o=insee,c=fr"));
  }

  @Test
  public void loadMultiStorage() {
    Realm realm = ldapRealmProviderDAOImpl.load("domaine2");
    assertThat("Should have two userstorages", realm.getUserStorages().size(), is(2));
    assertThat(
        "First userstorage is monUserStorage",
        realm.getUserStorages().get(1).getName(),
        is("monUserStorage"));
    assertThat(
        "Second userstorage is autreUserStorage",
        realm.getUserStorages().get(0).getName(),
        is("autreUserStorage"));
  }

  @Test
  public void findAllStorages() {
    List<Realm> realms = ldapRealmProviderDAOImpl.findAll();
    assertThat("Should have three realm", realms.size(), is(3));
    assertThat(
        "Should have userstorages",
        realms.get(1).getUserStorages().get(0).getName(),
        is("autreUserStorage"));
  }
}
