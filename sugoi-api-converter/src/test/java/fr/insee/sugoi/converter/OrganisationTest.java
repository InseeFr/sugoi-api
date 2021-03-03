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
import fr.insee.sugoi.converter.ouganext.Adresse;
import fr.insee.sugoi.converter.ouganext.Organisation;
import fr.insee.sugoi.converter.utils.CustomObjectMapper;
import fr.insee.sugoi.model.Organization;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

public class OrganisationTest {

  private static Organisation generateOrganisation() {
    Organisation organisation = new Organisation();
    organisation.setAdresseMessagerie("accueil@domain.tld");
    organisation.getPropriete().add("Test1");
    organisation.getPropriete().add("Test2");
    Adresse adresse = new Adresse();
    adresse.setLigneUne("17 bd adolphe pinard");
    adresse.setLigneDeux("");
    adresse.setLigneTrois("");
    adresse.setLigneQuatre("");
    adresse.setLigneCinq("");
    adresse.setLigneSix("");
    adresse.setLigneSept("92240 Malakoff");
    organisation.setAdresse(adresse);
    organisation.setDescription("INSEE");
    organisation.setDomaineDeGestion("TEST");
    organisation.setFacSimile("0123456789");
    organisation.setIdentifiant("XJHFLG4");
    organisation.setNomCommun("INSEE-DG");
    Organisation organisationRattachee = new Organisation();
    organisationRattachee.setIdentifiant("133546546");
    organisationRattachee.setNomCommun("Test Organisation");
    Organisation o2 = new Organisation();
    o2.setIdentifiant("133546547");
    o2.setNomCommun("Test Organisation");
    organisation.setOrganisationDeRattachement(organisationRattachee);
    return organisation;
  }

  public static Organization generateOrganization(boolean withSubOrganization) {
    Organization organization = new Organization();
    organization.setIdentifiant("Toto");
    Map<String, String> address = new HashMap<>();
    address.put("ligneDeux", "Lentreprise");
    address.put("ligneCinq", "CEDEX");
    organization.setAddress(address);
    organization.addAttributes("toto", "tata");
    organization.addAttributes("nomCommun", "Commun");
    organization.addAttributes("domaineDeGestion", "tutu");
    List<String> proprietes = new ArrayList<String>();
    proprietes.add("prop");
    proprietes.add("prop2");
    organization.addAttributes("proprietes", proprietes);
    if (withSubOrganization) {
      organization.setOrganization(generateOrganization(false));
    }
    return organization;
  }

  @Test
  public void testOrganisationJson() throws JsonProcessingException {
    Organisation organisation = generateOrganisation();
    String expectedJson =
        "{\"identifiant\":\"XJHFLG4\",\"nomCommun\":\"INSEE-DG\",\"domaineDeGestion\":\"TEST\",\"description\":\"INSEE\",\"adresseMessagerie\":\"accueil@domain.tld\",\"facSimile\":\"0123456789\",\"adresse\":{\"ligneUne\":\"17 bd adolphe pinard\",\"ligneDeux\":\"\",\"ligneTrois\":\"\",\"ligneQuatre\":\"\",\"ligneCinq\":\"\",\"ligneSix\":\"\",\"ligneSept\":\"92240 Malakoff\"},\"organisationDeRattachement\":\"133546546\",\"propriete\":[\"Test1\",\"Test2\"]}";
    assertEquals(
        expectedJson, CustomObjectMapper.JsonObjectMapper().writeValueAsString(organisation));
  }

  @Test
  public void testXMLJackson() throws JsonProcessingException {
    Organisation organisation = generateOrganisation();
    String expectedXML =
        "<?xml version='1.0' encoding='UTF-8'?>\r\n"
            + "<Organisation xmlns=\"http://xml.insee.fr/schema/annuaire\">\r\n"
            + "  <Identifiant>XJHFLG4</Identifiant>\r\n"
            + "  <NomCommun>INSEE-DG</NomCommun>\r\n"
            + "  <DomaineDeGestion>TEST</DomaineDeGestion>\r\n"
            + "  <Description>INSEE</Description>\r\n"
            + "  <AdresseMessagerie>accueil@domain.tld</AdresseMessagerie>\r\n"
            + "  <FacSimile>0123456789</FacSimile>\r\n"
            + "  <wstxns1:adressePostale xmlns:wstxns1=\"http://xml.insee.fr/schema\">\r\n"
            + "    <wstxns1:LigneUne>17 bd adolphe pinard</wstxns1:LigneUne>\r\n"
            + "    <wstxns1:LigneDeux></wstxns1:LigneDeux>\r\n"
            + "    <wstxns1:LigneTrois></wstxns1:LigneTrois>\r\n"
            + "    <wstxns1:LigneQuatre></wstxns1:LigneQuatre>\r\n"
            + "    <wstxns1:LigneCinq></wstxns1:LigneCinq>\r\n"
            + "    <wstxns1:LigneSix></wstxns1:LigneSix>\r\n"
            + "    <wstxns1:LigneSept>92240 Malakoff</wstxns1:LigneSept>\r\n"
            + "  </wstxns1:adressePostale>\r\n"
            + "  <OrganisationDeRattachementUri>133546546</OrganisationDeRattachementUri>\r\n"
            + "  <Propriete>Test1</Propriete>\r\n"
            + "  <Propriete>Test2</Propriete>\r\n"
            + "</Organisation>\r\n";
    Diff myDiff =
        DiffBuilder.compare(expectedXML)
            .withTest(CustomObjectMapper.XMLObjectMapper().writeValueAsString(organisation))
            .build();
    assertFalse(myDiff.hasDifferences());
  }

  @Test
  public void testConvertOrganisationToOrganizationJson() throws JsonProcessingException {
    try {
      Organisation organisation = generateOrganisation();
      OuganextSugoiMapper osm = new OuganextSugoiMapper();
      Organization organization = osm.serializeToSugoi(organisation, Organization.class);
      String expectedJson =
          "{\"identifiant\":\"XJHFLG4\",\"gpgkey\":null,\"organization\":{\"identifiant\":\"133546546\",\"gpgkey\":null,\"organization\":null,\"address\":{},\"metadatas\":{},\"attributes\":{\"proprietes\":null,\"nomCommun\":\"Test Organisation\",\"mail\":null,\"domaineDeGestion\":null,\"repertoireDeDistribution\":null,\"description\":null,\"numeroTelephone\":null,\"facSimile\":null}},\"address\":{\"LigneQuatre\":\"\",\"LigneUne\":\"17 bd adolphe pinard\",\"LigneDeux\":\"\",\"LigneCinq\":\"\",\"LigneSix\":\"\",\"LigneTrois\":\"\",\"LigneSept\":\"92240 Malakoff\"},\"metadatas\":{},\"attributes\":{\"proprietes\":[\"Test1\",\"Test2\"],\"nomCommun\":\"INSEE-DG\",\"mail\":\"accueil@domain.tld\",\"domaineDeGestion\":\"TEST\",\"repertoireDeDistribution\":null,\"description\":\"INSEE\",\"numeroTelephone\":null,\"facSimile\":\"0123456789\"}}";
      assertEquals(
          expectedJson, CustomObjectMapper.JsonObjectMapper().writeValueAsString(organization));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testConvertOrganizationToOrganisationXml() throws JsonProcessingException {
    try {
      Organization organization = generateOrganization(true);
      OuganextSugoiMapper osm = new OuganextSugoiMapper();
      Organisation organisation = osm.serializeToOuganext(organization, Organisation.class);
      String expectedXml =
          "<?xml version='1.0' encoding='UTF-8'?>\r\n"
              + "<Organisation xmlns=\"http://xml.insee.fr/schema/annuaire\">\r\n"
              + "  <Identifiant>Toto</Identifiant>\r\n"
              + "  <NomCommun>Commun</NomCommun>\r\n"
              + "  <DomaineDeGestion>tutu</DomaineDeGestion>\r\n"
              + "  <wstxns1:adressePostale xmlns:wstxns1=\"http://xml.insee.fr/schema\">\r\n"
              + "    <wstxns1:LigneDeux>Lentreprise</wstxns1:LigneDeux>\r\n"
              + "    <wstxns1:LigneCinq>CEDEX</wstxns1:LigneCinq>\r\n"
              + "  </wstxns1:adressePostale>\r\n"
              + "  <OrganisationDeRattachementUri>Toto</OrganisationDeRattachementUri>\r\n"
              + "  <Propriete>prop</Propriete>\r\n"
              + "  <Propriete>prop2</Propriete>\r\n"
              + "</Organisation>\r\n";

      Diff myDiff =
          DiffBuilder.compare(expectedXml)
              .withTest(CustomObjectMapper.XMLObjectMapper().writeValueAsString(organisation))
              .build();
      assertFalse(myDiff.hasDifferences());
    } catch (Exception e) {
      fail(e);
    }
  }
}
