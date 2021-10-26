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
import fr.insee.sugoi.converter.ouganext.InfoFormattageOuganext;
import fr.insee.sugoi.converter.ouganext.PasswordChangeRequestOuganext;
import fr.insee.sugoi.converter.utils.CustomObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PasswordChangeRequestTest {

  private static PasswordChangeRequestOuganext pcr;

  @BeforeAll
  private static void initialize() {
    pcr = new PasswordChangeRequestOuganext();
    pcr.setAncienMotDePasse("rrrèé~~~kfl)%");
    pcr.setNouveauMotDePasse("ffgjktiuyed_<gh>");
    pcr.setAdresseMessagerie("test@insee.fr");
    InfoFormattageOuganext info = new InfoFormattageOuganext();
    info.setChefSignataire("Moi");
    info.setUrlSite("https://entreprises.insee.fr");
    info.setNomApplicationLettre("ESA");
    pcr.setInfoFormattageEnvoi(info);
  }

  @Test
  public void TestJson() throws JsonProcessingException {
    System.out.println(CustomObjectMapper.JsonObjectMapper().writeValueAsString(pcr));
  }

  @Test
  public void testXMLJackson() throws JsonProcessingException {
    System.out.println(CustomObjectMapper.XMLObjectMapper().writeValueAsString(pcr));
  }
}
