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
import fr.insee.sugoi.converter.ouganext.Contact;
import fr.insee.sugoi.converter.ouganext.Organisation;
import fr.insee.sugoi.converter.utils.CustomObjectMapper;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Habilitation;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import java.security.cert.CertificateException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

public class ContactsTest {

  private static Contact generateContact() throws CertificateException, JAXBException {
    Contact contact = new Contact();
    contact.setIdentifiant("test");
    contact.setNomCommun("Test");
    contact.setAdresseMessagerie("tes.tkmgfdl@jhk.gmail");
    contact.setDomaineDeGestion("testDG");
    contact.setNumeroTelephone("055654587");
    contact.setDescription("description");
    contact.setCivilite("Camarade");
    contact.setFacSimile("0123456789");
    contact.setIdentifiantMetier("123456789");
    contact.setNom("test");
    contact.setNumeroTelephone("0123456789");
    contact.setPrenom("test");
    contact.setTelephonePortable("061245789636");
    contact.setHasPassword(false);
    // CertificateFactory cf = CertificateFactory.getInstance("X509");
    // X509Certificate cert = (X509Certificate) cf
    // .generateCertificate(ContactsTest.class.getResourceAsStream("/cert.crt"));
    // contact.setCertificate(cert);
    Adresse adresse = new Adresse();
    adresse.setLigneUne("15 rue Gabriel Peri");
    adresse.setLigneDeux("");
    adresse.setLigneTrois("");
    adresse.setLigneQuatre("");
    adresse.setLigneCinq("");
    adresse.setLigneSix("");
    adresse.setLigneSept("92240 Malakoff");
    contact.setAdresse(adresse);
    contact.setCodePin(new byte[2]);
    Organisation organisation = new Organisation();
    organisation.setIdentifiant("lorganisation");
    contact.setOrganisationDeRattachement(organisation);
    return contact;
  }

  private static User generateUser() {
    Habilitation habilitation = new Habilitation("superappli", "mega chef", "property");
    User user = new User();
    user.addHabilitation(habilitation);
    user.setLastName("Le Test");
    user.setFirstName("Toto");
    user.setMail("toto@test.fr");
    user.setUsername("toto");
    Organization organization = new Organization();
    Map<String, String> address = new HashMap<>();
    address.put("ligneDeux", "Lentreprise");
    address.put("ligneCinq", "CEDEX");
    organization.setIdentifiant("Lorganisation");
    organization.setAddress(address);
    user.addAttributes("description", "ce user est trop cool");
    user.addAttributes("inseeTimbre", "Z678");
    user.addAttributes("nvAttribute", "toto");
    user.addAttributes("attributeComplexe", new String[] {"coucou", "les", "amis"});
    user.addMetadatas("dateCreation", new Date(1603294311926L));
    user.setOrganization(organization);
    Group group1 = new Group();
    group1.setName("groupe tres important");
    group1.setDescription("ce groupe est tres important");
    Group group2 = new Group();
    group2.setName("groupe moins important");
    group2.setDescription("cest pas tres important");
    List<Group> groups = new ArrayList<Group>();
    groups.add(group1);
    groups.add(group2);
    user.setGroups(groups);
    user.setAddress(address);
    return user;
  }

  @Test
  public void testJson() throws JsonProcessingException {
    try {
      Contact object = generateContact();
      String expectedJson =
          "{\"Identifiant\":\"test\",\"NomCommun\":\"Test\",\"Nom\":\"test\",\"Prenom\":\"test\",\"DomaineDeGestion\":\"testDG\",\"Description\":\"description\",\"Civilite\":\"Camarade\",\"IdentifiantMetier\":\"123456789\",\"AdresseMessagerie\":\"tes.tkmgfdl@jhk.gmail\",\"NumeroTelephone\":\"0123456789\",\"TelephonePortable\":\"061245789636\",\"FacSimile\":\"0123456789\",\"MotDePasseExiste\":false,\"AdressePostale\":{\"ligneUne\":\"15 rue Gabriel Peri\",\"ligneDeux\":\"\",\"ligneTrois\":\"\",\"ligneQuatre\":\"\",\"ligneCinq\":\"\",\"ligneSix\":\"\",\"ligneSept\":\"92240 Malakoff\"},\"Propriete\":[],\"InseeRoleApplicatif\":[],\"CodePin\":\"AAA=\",\"OrganisationDeRattachementUri\":\"lorganisation\"}";
      assertEquals(expectedJson, CustomObjectMapper.JsonObjectMapper().writeValueAsString(object));
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testXMLJackson() throws JsonProcessingException {
    Contact contact;
    try {
      contact = generateContact();
      String expectedXml =
          "<?xml version='1.0' encoding='UTF-8'?>\r\n"
              + "<Contact xmlns=\"http://xml.insee.fr/schema/annuaire\">\r\n"
              + "  <Identifiant>test</Identifiant>\r\n"
              + "  <NomCommun>Test</NomCommun>\r\n"
              + "  <Nom>test</Nom>\r\n"
              + "  <Prenom>test</Prenom>\r\n"
              + "  <DomaineDeGestion>testDG</DomaineDeGestion>\r\n"
              + "  <Description>description</Description>\r\n"
              + "  <Civilite>Camarade</Civilite>\r\n"
              + "  <IdentifiantMetier>123456789</IdentifiantMetier>\r\n"
              + "  <AdresseMessagerie>tes.tkmgfdl@jhk.gmail</AdresseMessagerie>\r\n"
              + "  <NumeroTelephone>0123456789</NumeroTelephone>\r\n"
              + "  <TelephonePortable>061245789636</TelephonePortable>\r\n"
              + "  <FacSimile>0123456789</FacSimile>\r\n"
              + "  <MotDePasseExiste>false</MotDePasseExiste>\r\n"
              + "  <wstxns1:AdressePostale xmlns:wstxns1=\"http://xml.insee.fr/schema\">\r\n"
              + "    <wstxns1:LigneUne>15 rue Gabriel Peri</wstxns1:LigneUne>\r\n"
              + "    <wstxns1:LigneDeux></wstxns1:LigneDeux>\r\n"
              + "    <wstxns1:LigneTrois></wstxns1:LigneTrois>\r\n"
              + "    <wstxns1:LigneQuatre></wstxns1:LigneQuatre>\r\n"
              + "    <wstxns1:LigneCinq></wstxns1:LigneCinq>\r\n"
              + "    <wstxns1:LigneSix></wstxns1:LigneSix>\r\n"
              + "    <wstxns1:LigneSept>92240 Malakoff</wstxns1:LigneSept>\r\n"
              + "  </wstxns1:AdressePostale>\r\n"
              + "  <InseeRoleApplicatif/>\r\n"
              + "  <CodePin>AAA=</CodePin>\r\n"
              + "  <OrganisationDeRattachementUri>lorganisation</OrganisationDeRattachementUri>\r\n"
              + "</Contact>\r\n";
      Diff myDiff =
          DiffBuilder.compare(expectedXml)
              .withTest(CustomObjectMapper.XMLObjectMapper().writeValueAsString(contact))
              .build();
      assertFalse(myDiff.hasDifferences());
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testConvertUserToContactXML() throws JsonProcessingException {
    User user = generateUser();
    OuganextSugoiMapper osm = new OuganextSugoiMapper();
    Contact contact = osm.serializeToOuganext(user, Contact.class);
    String expectedXml =
        "<?xml version='1.0' encoding='UTF-8'?>\r\n"
            + "<Contact xmlns=\"http://xml.insee.fr/schema/annuaire\">\r\n"
            + "  <Identifiant>toto</Identifiant>\r\n"
            + "  <Nom>Le Test</Nom>\r\n"
            + "  <Prenom>Toto</Prenom>\r\n"
            + "  <Description>ce user est trop cool</Description>\r\n"
            + "  <AdresseMessagerie>toto@test.fr</AdresseMessagerie>\r\n"
            + "  <MotDePasseExiste>false</MotDePasseExiste>\r\n"
            + "  <wstxns1:AdressePostale xmlns:wstxns1=\"http://xml.insee.fr/schema\">\r\n"
            + "    <wstxns1:LigneDeux>Lentreprise</wstxns1:LigneDeux>\r\n"
            + "    <wstxns1:LigneCinq>CEDEX</wstxns1:LigneCinq>\r\n"
            + "  </wstxns1:AdressePostale>\r\n"
            + "  <InseeRoleApplicatif/>\r\n"
            + "  <DateCreation>1603294311926</DateCreation>\r\n"
            + "  <OrganisationDeRattachementUri>Lorganisation</OrganisationDeRattachementUri>\r\n"
            + "</Contact>\r\n";
    Diff myDiff =
        DiffBuilder.compare(expectedXml)
            .withTest(CustomObjectMapper.XMLObjectMapper().writeValueAsString(contact))
            .build();
    assertFalse(myDiff.hasDifferences());
  }

  @Test
  public void testConvertContactToUserJson() throws JsonProcessingException {
    try {
      Contact contact = generateContact();
      OuganextSugoiMapper osm = new OuganextSugoiMapper();
      User user = osm.serializeToSugoi(contact, User.class);
      String expectedJson =
          "{\"lastName\":\"test\",\"firstName\":\"test\",\"mail\":\"tes.tkmgfdl@jhk.gmail\",\"username\":\"test\",\"organization\":{\"identifiant\":\"lorganisation\",\"gpgkey\":null,\"organization\":null,\"address\":{},\"metadatas\":{},\"attributes\":{\"proprietes\":null,\"nomCommun\":null,\"mail\":null,\"domaineDeGestion\":null,\"repertoireDeDistribution\":null,\"description\":null,\"numeroTelephone\":null,\"facSimile\":null}},\"groups\":[],\"habilitations\":[],\"address\":{\"LigneQuatre\":\"\",\"LigneUne\":\"15 rue Gabriel Peri\",\"LigneDeux\":\"\",\"LigneCinq\":\"\",\"LigneSix\":\"\",\"LigneTrois\":\"\",\"LigneSept\":\"92240 Malakoff\"},\"metadatas\":{\"dateCreation\":null,\"hasPassword\":false},\"attributes\":{\"proprietes\":null,\"insee_roles_applicatifs\":null,\"nomCommun\":\"Test\",\"domaineDeGestion\":\"testDG\",\"repertoireDeDistribution\":null,\"description\":\"description\",\"telephonePortable\":\"061245789636\",\"codePin\":\"AAA=\",\"numeroTelephone\":\"0123456789\",\"identifiantMetier\":\"123456789\",\"civilite\":\"Camarade\",\"facSimile\":\"0123456789\"}}";
      assertEquals(expectedJson, CustomObjectMapper.JsonObjectMapper().writeValueAsString(user));
    } catch (Exception e) {
      fail(e);
    }
  }
}
