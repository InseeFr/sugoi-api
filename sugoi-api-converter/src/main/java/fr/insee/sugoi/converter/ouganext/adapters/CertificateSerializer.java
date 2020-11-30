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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import javax.xml.bind.DatatypeConverter;

public class CertificateSerializer extends StdSerializer<byte[]> {

  /** */
  private static final long serialVersionUID = 1L;

  public CertificateSerializer() {
    this(null);
  }

  public CertificateSerializer(Class<byte[]> t) {
    super(t);
  }

  @Override
  public void serialize(byte[] value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    gen.writeString(DatatypeConverter.printBase64Binary(value));
  }
}
