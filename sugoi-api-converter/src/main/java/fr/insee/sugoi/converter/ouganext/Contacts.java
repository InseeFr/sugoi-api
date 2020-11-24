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
package fr.insee.sugoi.converter.ouganext;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "ListeCompte", namespace = Namespace.ANNUAIRE)
@JsonPropertyOrder({"liste"})
public class Contacts {

  @JacksonXmlProperty(localName = "Contact", namespace = Namespace.ANNUAIRE)
  @JacksonXmlElementWrapper(useWrapping = false)
  protected List<Contact> liste = new ArrayList<Contact>();

  public List<Contact> getListe() {
    return this.liste;
  }
}
