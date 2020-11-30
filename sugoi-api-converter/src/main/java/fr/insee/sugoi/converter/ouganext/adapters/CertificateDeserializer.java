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
package fr.insee.sugoi.converter.ouganext.adapters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public class CertificateDeserializer extends StdDeserializer<byte[]> {

  public static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n";
  public static final String END_CERTIFICATE = "\n-----END CERTIFICATE-----\n";
  /** */
  private static final long serialVersionUID = 1L;

  public CertificateDeserializer() {
    this(null);
  }

  public CertificateDeserializer(Class<byte[]> t) {
    super(t);
  }

  @Override
  public byte[] deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {

    String certstring =
        BEGIN_CERTIFICATE
            + p.getText().replaceAll("\n", "").replaceAll(" ", "").replaceAll("\t", "")
            + END_CERTIFICATE;

    return certstring.getBytes();
  }
}
