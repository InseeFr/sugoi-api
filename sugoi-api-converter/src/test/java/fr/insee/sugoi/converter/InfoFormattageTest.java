package fr.insee.sugoi.converter;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import fr.insee.sugoi.converter.ouganext.InfoFormattage;
import fr.insee.sugoi.converter.utils.CustomObjectMapper;

public class InfoFormattageTest {

    private static InfoFormattage info;

    @BeforeAll
    private static void initialize() {
        info = new InfoFormattage();
        info.setChefSignataire("Moi");
        info.setUrlSite("https://entreprises.insee.fr");
        info.setNomApplicationLettre("ESA");
    }

    @Test
    public void TestJson() throws JsonProcessingException {
        System.out.println(CustomObjectMapper.JsonObjectMapper().writeValueAsString(info));
    }

    @Test
    public void testXMLJackson() throws JsonProcessingException {
        System.out.println(CustomObjectMapper.XMLObjectMapper().writeValueAsString(info));
    }

}
