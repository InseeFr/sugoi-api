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
import fr.insee.sugoi.converter.ouganext.OrganisationOuganext;
import java.io.IOException;

public class OrganisationSerializer extends StdSerializer<OrganisationOuganext> {

  public OrganisationSerializer() {
    this(null);
  }

  public OrganisationSerializer(Class<OrganisationOuganext> t) {
    super(t);
  }

  /** */
  private static final long serialVersionUID = 1L;

  @Override
  public void serialize(OrganisationOuganext value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    gen.writeString(value.getIdentifiant());
  }
}
