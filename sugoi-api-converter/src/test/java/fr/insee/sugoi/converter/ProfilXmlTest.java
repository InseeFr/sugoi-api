package fr.insee.sugoi.converter;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import fr.insee.sugoi.converter.ouganext.Profil;
import fr.insee.sugoi.converter.utils.CustomObjectMapper;

public class ProfilXmlTest {

  private static Profil profil;

  @BeforeAll
  public static void initialize() {
    profil = new Profil();
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
