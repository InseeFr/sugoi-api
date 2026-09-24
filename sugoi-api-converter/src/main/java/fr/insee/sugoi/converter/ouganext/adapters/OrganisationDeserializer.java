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

import fr.insee.sugoi.converter.ouganext.OrganisationOuganext;
import java.io.Serial;
import java.io.Serializable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class OrganisationDeserializer extends StdDeserializer<OrganisationOuganext>
    implements Serializable {

  /** */
  @Serial private static final long serialVersionUID = 1L;

  public OrganisationDeserializer() {
    super(OrganisationOuganext.class);
  }

  @Override
  public OrganisationOuganext deserialize(JsonParser p, DeserializationContext ctxt)
      throws JacksonException {
    OrganisationOuganext organisation = new OrganisationOuganext();
    organisation.setIdentifiant(p.getString());
    return organisation;
  }
}
