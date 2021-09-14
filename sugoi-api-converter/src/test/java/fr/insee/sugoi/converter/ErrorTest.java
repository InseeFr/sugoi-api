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

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.sugoi.converter.ouganext.ErrorResult;
import fr.insee.sugoi.converter.utils.CustomObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

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
    String expectedErrorJson =
        "{\"exception\":\"java.io.IOException\",\"message\":\"Ioexception Test\"}";
    assertEquals(
        expectedErrorJson, CustomObjectMapper.JsonObjectMapper().writeValueAsString(errorResult));
  }

  @Test
  public void testXMLJacksonError() throws JsonProcessingException {
    ErrorResult errorResult = createErrorResult();
    String expectedErrorXml =
        "<?xml version='1.0' encoding='UTF-8'?>\r\n"
            + "<ns1:ErrorResult xmlns:ns1=\"http://xml.insee.fr/schema/annuaire\">\r\n"
            + "  <Exception>java.io.IOException</Exception>\r\n"
            + "  <Message>Ioexception Test</Message>\r\n"
            + "</ns1:ErrorResult>\r\n";
    Diff myDiff =
        DiffBuilder.compare(expectedErrorXml)
            .checkForSimilar()
            .withTest(CustomObjectMapper.XMLObjectMapper().writeValueAsString(errorResult))
            .build();
    assertFalse(myDiff.hasDifferences());
  }
}
