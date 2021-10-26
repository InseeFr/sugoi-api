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
package fr.insee.sugoi.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.sugoi.converter.ouganext.ProfilOuganext;
import fr.insee.sugoi.converter.utils.CustomObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ProfilXmlTest {

  private static ProfilOuganext profil;

  @BeforeAll
  public static void initialize() {
    profil = new ProfilOuganext();
    profil.setBrancheAdresse("ou=adresse,o=insee,c=fr");
    profil.setBrancheContact("ou=contact,o=insee,c=fr");
    profil.setBrancheOrganisation("ou=organisation,o=insee,c=fr");
    profil.setLdapUrl("localhost");
    profil.setMailUnicity(false);
    profil.setPasswordReleasedAllowed(false);
    profil.setLongueurMiniPassword(8);
    profil.setNomProfil("NomProfil");
    profil.setPwdResetAllowed(true);
    profil.setPort(10389);
    profil.setVlvSupported(true);
    profil.setPagingSupported(true);
  }

  @Test
  public void TestJson() throws JsonProcessingException {
    System.out.println(CustomObjectMapper.JsonObjectMapper().writeValueAsString(profil));
  }

  @Test
  public void testXMLJackson() throws JsonProcessingException {
    System.out.println(CustomObjectMapper.XMLObjectMapper().writeValueAsString(profil));
  }
}
