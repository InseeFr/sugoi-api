package fr.insee.sugoi.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.jupiter.api.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import fr.insee.sugoi.converter.ouganext.ErrorResult;
import fr.insee.sugoi.converter.utils.CustomObjectMapper;

public class ErrorTest {

    private ErrorResult createErrorResult() {
        ErrorResult errorResult = new ErrorResult();
        try {
            throw new IOException("Ioexception Test");
        } catch (IOException e) {
            errorResult.setException(e.getClass().getCanonicalName());
            errorResult.setMessage(e.getMessage());
            return errorResult;
        }
    }

    @Test
    public void testJsonError() throws JsonProcessingException {
        ErrorResult errorResult = createErrorResult();
        String expectedErrorJson = "{\"exception\":\"java.io.IOException\",\"message\":\"Ioexception Test\"}";
        assertEquals(expectedErrorJson, CustomObjectMapper.JsonObjectMapper().writeValueAsString(errorResult));
    }

    @Test
    public void testXMLJacksonError() throws JsonProcessingException {
        ErrorResult errorResult = createErrorResult();
        String expectedErrorXml = "<?xml version='1.0' encoding='UTF-8'?>\r\n"
                + "<ErrorResult xmlns=\"http://xml.insee.fr/schema/annuaire\">\r\n"
                + "  <Exception>java.io.IOException</Exception>\r\n" + "  <Message>Ioexception Test</Message>\r\n"
                + "</ErrorResult>\r\n";
        Diff myDiff = DiffBuilder.compare(expectedErrorXml)
                .withTest(CustomObjectMapper.XMLObjectMapper().writeValueAsString(errorResult)).build();
        assertFalse(myDiff.hasDifferences());
    }
}
