package fr.insee.sugoi.converter;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import fr.insee.sugoi.converter.ouganext.InfoFormattage;
import fr.insee.sugoi.converter.ouganext.PasswordChangeRequest;
import fr.insee.sugoi.converter.utils.CustomObjectMapper;

public class PasswordChangeRequestTest {

    private static PasswordChangeRequest pcr;

    @BeforeAll
    private static void initialize() {
        pcr = new PasswordChangeRequest();
        pcr.setAncienMotDePasse("rrrèé~~~kfl)%");
        pcr.setNouveauMotDePasse("ffgjktiuyed_<gh>");
        pcr.setAdresseMessagerie("test@insee.fr");
        InfoFormattage info = new InfoFormattage();
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
