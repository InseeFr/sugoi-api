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
import fr.insee.sugoi.converter.ouganext.Organisation;
import java.io.IOException;

public class OrganisationDeserializer extends StdDeserializer<Organisation> {

  /** */
  private static final long serialVersionUID = 1L;

  public OrganisationDeserializer() {
    this(null);
  }

  public OrganisationDeserializer(Class<Organisation> t) {
    super(t);
  }

  @Override
  public Organisation deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    Organisation organisation = new Organisation();
    organisation.setIdentifiant(p.getText());
    return organisation;
  }
}
