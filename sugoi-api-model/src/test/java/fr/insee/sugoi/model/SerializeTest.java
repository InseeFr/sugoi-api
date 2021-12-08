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
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

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
          + "        \"organizationMapping\": {"
          + "            \"address\": \"inseeAdressePostaleDN,address,rw\","
          + "            \"attributes.description\": \"description,String,rw\","
          + "            \"identifiant\": \"uid,String,rw\","
          + "            \"organization\": \"inseeOrganisationDN,organization,rw\","
          + "            \"attributes.mail\": \"mail,String,rw\","
          + "            \"gpgkey\": \"inseeClefChiffrement,byte_array,ro\""
          + "        },"
          + "        \"userMapping\": {"
          + "            \"attributes.identifiant_metier\": \"inseeIdentifiantMetier,String,rw\","
          + "            \"lastName\": \"sn,String,rw\","
          + "            \"address\": \"inseeAdressePostaleDN,address,rw\","
          + "            \"mail\": \"mail,String,rw\","
          + "            \"attributes.description\": \"description,String,rw\","
          + "            \"attributes.common_name\": \"cn,String,rw\","
          + "            \"attributes.phone_number\": \"telephoneNumber,String,rw\","
          + "            \"attributes.telephone_portable\": \"inseenumerotelephoneportable,String,rw\","
          + "            \"certificate\": \"userCertificate;binary,byte_array,ro\","
          + "            \"habilitations\": \"inseeGroupeDefaut,list_habilitation,rw\","
          + "            \"groups\": \"memberOf,list_group,ro\","
          + "            \"attributes.properties\": \"inseePropriete,list_string,rw\","
          + "            \"metadatas.modifyTimestamp\": \"modifyTimestamp,string,ro\","
          + "            \"attributes.additionalMail\": \"inseeMailCorrespondant,String,rw\","
          + "            \"attributes.seeAlsos\": \"seeAlso,list_string,ro,singl\","
          + "            \"attributes.personal_title\": \"personalTitle,String,rw\","
          + "            \"attributes.insee_timbre\": \"inseeTimbre,String,rw\","
          + "            \"attributes.repertoire_distribution\": \"inseerepertoirededistribution,String,rw\","
          + "            \"firstName\": \"givenname,String,rw\","
          + "            \"attributes.insee_organisme\": \"inseeOrganisme,String,rw\","
          + "            \"organization\": \"inseeOrganisationDN,organization,rw\","
          + "            \"attributes.hasPassword\": \"userPassword,exists,ro\","
          + "            \"attributes.insee_roles_applicatifs\": \"inseeRoleApplicatif,list_string,rw\","
          + "            \"username\": \"uid,String,rw\""
          + "        }"
          + "        }"
          + "    }"
          + "    ],"
          + "    \"properties\": {"
          + "        \"app-managed-attribute-keys-list\": \"inseegroupedefaut,inseeroleapplicatif\","
          + "        \"description\": \"Le profil domaine6<br/>Test <b>html</b> in description\""
          + "    },"
          + "    \"mappings\": {"
          + "    \"applicationMapping\": {"
          + "        \"name\": \"ou,String,rw\""
          + "    },"
          + "    \"groupMapping\": {"
          + "        \"name\": \"cn,String,rw\","
          + "        \"description\": \"description,String,rw\","
          + "        \"users\": \"uniquemember,list_user,rw\""
          + "    }"
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
  public void serializeRealmTest() throws JsonMappingException, JsonProcessingException {
    Realm realm = objectMapper.readValue(realmToTest, Realm.class);
    assertThat("Should be domaine1", realm.getName(), is("newrealm"));
  }
}
