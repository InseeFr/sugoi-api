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
import fr.insee.sugoi.converter.ouganext.Application;
import fr.insee.sugoi.converter.ouganext.Habilitations;
import fr.insee.sugoi.converter.ouganext.Role;
import fr.insee.sugoi.converter.utils.CustomObjectMapper;
import fr.insee.sugoi.model.Habilitation;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

public class HabilitationsTest {

  private static Habilitations generateHabilitations() {
    Role roleUpload = new Role();
    roleUpload.setName("upload");
    Role roleDownload = new Role();
    roleDownload.setName("download");
    Role rolededfze = new Role();
    rolededfze.setName("dedfze");
    roleUpload.getPropriete().add("UP2012");
    Role roleDoublon = new Role();
    roleDoublon.setName("download");
    roleDoublon.getPropriete().add("DW2012");
    List<Role> roles = new ArrayList<Role>();
    roles.add(roleDoublon);
    roles.add(roleDownload);
    roles.add(roleUpload);
    roles.add(rolededfze);
    Application application1 = new Application();
    Application application2 = new Application();
    application1.setName("app1");
    application2.setName("app2");
    application1.addRole(roleDoublon);
    application2.addRole(roleDownload);
    application1.addRole(roleUpload);
    application2.addRole(rolededfze);
    Habilitations habs = new Habilitations();
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
    Habilitations habs = generateHabilitations();
    String jsonExpected =
        "{\"application\":[{\"name\":\"app1\",\"role\":[{\"name\":\"download\",\"propriete\":[\"DW2012\"]},{\"name\":\"upload\",\"propriete\":[\"UP2012\"]}]},{\"name\":\"app2\",\"role\":[{\"name\":\"download\",\"propriete\":[]},{\"name\":\"dedfze\",\"propriete\":[]}]}]}";
    System.out.println(CustomObjectMapper.JsonObjectMapper().writeValueAsString(habs));
    assertEquals(jsonExpected, CustomObjectMapper.JsonObjectMapper().writeValueAsString(habs));
  }

  @Test
  public void testXMLJackson() throws JsonProcessingException {
    Habilitations habs = generateHabilitations();
    String xmlExpected =
        "<?xml version='1.0' encoding='UTF-8'?>\r\n"
            + "<Habilitations xmlns=\"http://xml.insee.fr/schema/annuaire\">\r\n"
            + "  <application>\r\n"
            + "    <application xmlns:wstxns1=\"http://xml.insee.fr/schema/annuaire\" wstxns1:name=\"app1\">\r\n"
            + "      <role>\r\n"
            + "        <role name=\"download\">\r\n"
            + "          <propriete xmlns=\"\">\r\n"
            + "            <propriete>DW2012</propriete>\r\n"
            + "          </propriete>\r\n"
            + "        </role>\r\n"
            + "        <role name=\"upload\">\r\n"
            + "          <propriete xmlns=\"\">\r\n"
            + "            <propriete>UP2012</propriete>\r\n"
            + "          </propriete>\r\n"
            + "        </role>\r\n"
            + "      </role>\r\n"
            + "    </application>\r\n"
            + "    <application xmlns:wstxns2=\"http://xml.insee.fr/schema/annuaire\" wstxns2:name=\"app2\">\r\n"
            + "      <role>\r\n"
            + "        <role name=\"download\">\r\n"
            + "          <propriete xmlns=\"\"/>\r\n"
            + "        </role>\r\n"
            + "        <role name=\"dedfze\">\r\n"
            + "          <propriete xmlns=\"\"/>\r\n"
            + "        </role>\r\n"
            + "      </role>\r\n"
            + "    </application>\r\n"
            + "  </application>\r\n"
            + "</Habilitations>\r\n";
    Diff myDiff =
        DiffBuilder.compare(xmlExpected)
            .withTest(CustomObjectMapper.XMLObjectMapper().writeValueAsString(habs))
            .build();
    assertFalse(myDiff.hasDifferences());
  }

  @Test
  public void testConvertHabilitationToApplicationXml() {
    String expectedXml =
        "<?xml version='1.0' encoding='UTF-8'?>\r\n"
            + "<Habilitations xmlns=\"http://xml.insee.fr/schema/annuaire\">\r\n"
            + "  <application>\r\n"
            + "    <application xmlns:wstxns1=\"http://xml.insee.fr/schema/annuaire\" wstxns1:name=\"App2\">\r\n"
            + "      <role>\r\n"
            + "        <role name=\"consultant\">\r\n"
            + "          <propriete xmlns=\"\">\r\n"
            + "            <propriete>truc</propriete>\r\n"
            + "          </propriete>\r\n"
            + "        </role>\r\n"
            + "      </role>\r\n"
            + "    </application>\r\n"
            + "    <application xmlns:wstxns2=\"http://xml.insee.fr/schema/annuaire\" wstxns2:name=\"App1\">\r\n"
            + "      <role>\r\n"
            + "        <role name=\"admin\">\r\n"
            + "          <propriete xmlns=\"\">\r\n"
            + "            <propriete>important</propriete>\r\n"
            + "            <propriete>toto</propriete>\r\n"
            + "          </propriete>\r\n"
            + "        </role>\r\n"
            + "      </role>\r\n"
            + "    </application>\r\n"
            + "  </application>\r\n"
            + "</Habilitations>\r\n";
    List<Habilitation> habilitations = generateHabilitationsSugoi();
    Habilitations habilitationsOuganext =
        OuganextSugoiMapper.convertHabilitationToHabilitations(habilitations);
    try {
      Diff myDiff =
          DiffBuilder.compare(expectedXml)
              .withTest(
                  CustomObjectMapper.XMLObjectMapper().writeValueAsString(habilitationsOuganext))
              .build();
      assertFalse(myDiff.hasDifferences());
    } catch (JsonProcessingException e) {
      fail(e);
    }
  }
}
