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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.sugoi.converter.mapper.OuganextSugoiMapper;
import fr.insee.sugoi.converter.ouganext.ApplicationOuganext;
import fr.insee.sugoi.converter.ouganext.HabilitationsOuganext;
import fr.insee.sugoi.converter.ouganext.RoleOuganext;
import fr.insee.sugoi.converter.utils.CustomObjectMapper;
import fr.insee.sugoi.model.Habilitation;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

public class HabilitationsTest {

  private static HabilitationsOuganext generateHabilitations() {
    RoleOuganext roleUpload = new RoleOuganext();
    roleUpload.setName("upload");
    RoleOuganext roleDownload = new RoleOuganext();
    roleDownload.setName("download");
    RoleOuganext rolededfze = new RoleOuganext();
    rolededfze.setName("dedfze");
    roleUpload.getPropriete().add("UP2012");
    RoleOuganext roleDoublon = new RoleOuganext();
    roleDoublon.setName("download");
    roleDoublon.getPropriete().add("DW2012");
    List<RoleOuganext> roles = new ArrayList<RoleOuganext>();
    roles.add(roleDoublon);
    roles.add(roleDownload);
    roles.add(roleUpload);
    roles.add(rolededfze);
    ApplicationOuganext application1 = new ApplicationOuganext();
    ApplicationOuganext application2 = new ApplicationOuganext();
    application1.setName("app1");
    application2.setName("app2");
    application1.addRole(roleDoublon);
    application2.addRole(roleDownload);
    application1.addRole(roleUpload);
    application2.addRole(rolededfze);
    HabilitationsOuganext habs = new HabilitationsOuganext();
    habs.addApplication(application1);
    habs.addApplication(application2);
    return habs;
  }

  public List<Habilitation> generateHabilitationsSugoi() {
    Habilitation sugoiHabilitation1 = new Habilitation();
    sugoiHabilitation1.setApplication("App1");
    sugoiHabilitation1.setProperty("important");
    sugoiHabilitation1.setRole("admin");
    Habilitation sugoiHabilitation2 = new Habilitation();
    sugoiHabilitation2.setApplication("App2");
    sugoiHabilitation2.setProperty("truc");
    sugoiHabilitation2.setRole("consultant");
    Habilitation sugoiHabilitation3 = new Habilitation();
    sugoiHabilitation3.setApplication("App1");
    sugoiHabilitation3.setProperty("toto");
    sugoiHabilitation3.setRole("admin");
    List<Habilitation> sugoiHabilitations = new ArrayList<Habilitation>();
    sugoiHabilitations.add(sugoiHabilitation1);
    sugoiHabilitations.add(sugoiHabilitation2);
    sugoiHabilitations.add(sugoiHabilitation3);
    return sugoiHabilitations;
  }

  @Test
  public void testJson() throws JsonProcessingException {
    HabilitationsOuganext habs = generateHabilitations();
    String jsonExpected =
        "{\"application\":[{\"name\":\"app1\",\"role\":[{\"name\":\"download\",\"propriete\":[\"DW2012\"]},{\"name\":\"upload\",\"propriete\":[\"UP2012\"]}]},{\"name\":\"app2\",\"role\":[{\"name\":\"download\",\"propriete\":[]},{\"name\":\"dedfze\",\"propriete\":[]}]}]}";
    System.out.println(CustomObjectMapper.JsonObjectMapper().writeValueAsString(habs));
    assertEquals(jsonExpected, CustomObjectMapper.JsonObjectMapper().writeValueAsString(habs));
  }

  @Test
  public void testXMLJackson() throws JsonProcessingException {
    HabilitationsOuganext habs = generateHabilitations();
    String xmlExpected =
        "<?xml version='1.0' encoding='UTF-8'?>\r\n"
            + "<ns1:Habilitations xmlns:ns1=\"http://xml.insee.fr/schema/annuaire\">\r\n"
            + "  <application name=\"app1\">\r\n"
            + "    <role name=\"download\">\r\n"
            + "      <propriete>DW2012</propriete>\r\n"
            + "    </role>\r\n"
            + "    <role name=\"upload\">\r\n"
            + "      <propriete>UP2012</propriete>\r\n"
            + "    </role>\r\n"
            + "  </application>\r\n"
            + "  <application name=\"app2\">\r\n"
            + "    <role name=\"download\"/>\r\n"
            + "    <role name=\"dedfze\"/>\r\n"
            + "  </application>\r\n"
            + "</ns1:Habilitations>\r\n";
    String xml = CustomObjectMapper.XMLObjectMapper().writeValueAsString(habs);
    System.out.println(xml);
    System.out.println(xmlExpected);
    Diff myDiff =
        DiffBuilder.compare(xmlExpected)
            .checkForSimilar()
            .withTest(CustomObjectMapper.XMLObjectMapper().writeValueAsString(habs))
            .build();
    assertFalse(myDiff.hasDifferences());
  }

  @Test
  public void testConvertHabilitationToApplicationXml() {
    String expectedXml =
        "<?xml version='1.0' encoding='UTF-8'?>\r\n"
            + "<ns1:Habilitations xmlns:ns1=\"http://xml.insee.fr/schema/annuaire\">\r\n"
            + "  <application name=\"App2\">\r\n"
            + "    <role name=\"consultant\">\r\n"
            + "      <propriete>truc</propriete>\r\n"
            + "    </role>\r\n"
            + "  </application>\r\n"
            + "  <application name=\"App1\">\r\n"
            + "    <role name=\"admin\">\r\n"
            + "      <propriete>important</propriete>\r\n"
            + "      <propriete>toto</propriete>\r\n"
            + "    </role>\r\n"
            + "  </application>\r\n"
            + "</ns1:Habilitations>\r\n";
    List<Habilitation> habilitations = generateHabilitationsSugoi();
    HabilitationsOuganext habilitationsOuganext =
        OuganextSugoiMapper.convertHabilitationToHabilitations(habilitations);
    try {
      Diff myDiff =
          DiffBuilder.compare(expectedXml)
              .checkForSimilar()
              .withTest(
                  CustomObjectMapper.XMLObjectMapper().writeValueAsString(habilitationsOuganext))
              .build();
      assertFalse(myDiff.hasDifferences());
    } catch (JsonProcessingException e) {
      fail(e);
    }
  }
}
