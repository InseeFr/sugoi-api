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
package fr.insee.sugoi.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class SerializeTest {

  ObjectMapper objectMapper = new ObjectMapper();

  String realmToTest =
      "{"
          + "    \"name\": \"newrealm\","
          + "    \"url\": \"localhost\","
          + "    \"appSource\": \"ou=Applications,o=insee,c=fr\","
          + "    \"userStorages\": ["
          + "    {"
          + "        \"name\": \"Profil_domaine5_WebServiceLdap\","
          + "        \"userSource\": \"ou=contacts,ou=clients_domaine5,o=insee,c=fr\","
          + "        \"organizationSource\": \"ou=organisations,ou=clients_domaine5,o=insee,c=fr\","
          + "        \"addressSource\": \"ou=adresses,ou=clients_domaine5,o=insee,c=fr\","
          + "        \"properties\": {"
          + "        \"group_filter_pattern\": \"(cn={group}_{appliname})\","
          + "        \"organization_object_classes\": \"top,inseeOrganisation\","
          + "        \"user_object_classes\": \"top,inseeCompte,inseeContact,inseeAttributsAuthentification,inseeAttributsHabilitation,inseeAttributsCommunication\","
          + "        \"group_source_pattern\": \"ou={appliname}_Objets,ou={appliname},ou=Applications,o=insee,c=fr\""
          + "        },"
          + "        \"mappings\": {"
          + "        \"organizationMapping\": [],"
          + "        \"userMapping\": []"
          + "        }"
          + "    }"
          + "    ],"
          + "    \"properties\": {"
          + "        \"create_password_WITHUpperCase\": \"true\","
          + "        \"description\": \"Le profil domaine6<br/>Test <b>html</b> in description\""
          + "    },"
          + "    \"uiMapping\": {"
          + "    \"uiOrganizationMapping\": [],"
          + "    \"uiUserMapping\": ["
          + "    ]"
          + "    },"
          + "    \"readerType\": \"LdapReaderStore\","
          + "    \"writerType\": \"LdapWriterStore\""
          + "}";

  @Test
  public void serializeRealmTest() throws JsonProcessingException {
    Realm realm = objectMapper.readValue(realmToTest, Realm.class);
    assertThat("Should be domaine1", realm.getName(), is("newrealm"));
    assertThat(
        "Should have password property",
        realm.getProperties().get(PasswordPolicyConstants.CREATE_PASSWORD_WITH_UPPERCASE).get(0),
        is("true"));
  }
}
