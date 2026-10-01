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

import java.io.Serial;
import java.io.Serializable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class CertificateDeserializer extends StdDeserializer<byte[]> implements Serializable {

  public static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n";
  public static final String END_CERTIFICATE = "\n-----END CERTIFICATE-----\n";
  /** */
  @Serial private static final long serialVersionUID = 1L;

  public CertificateDeserializer() {
    super(CertificateDeserializer.class);
  }

  @Override
  public byte[] deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {

    String certstring =
        BEGIN_CERTIFICATE
            + p.getString().replace("\n", "").replace(" ", "").replace("\t", "")
            + END_CERTIFICATE;

    return certstring.getBytes();
  }
}
