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
import com.fasterxml.jackson.annotation.JsonRootName;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonRootName(value = "role", namespace = Namespace.ANNUAIRE)
@JsonPropertyOrder({"name", "propriete"})
public class RoleOuganext {

  public RoleOuganext() {
    super();
  }

  public RoleOuganext(String name) {
    super();
    this.name = name;
  }

  @JacksonXmlProperty(isAttribute = true)
  private String name;

  @JacksonXmlElementWrapper(useWrapping = false)
  private List<String> propriete = new ArrayList<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return la liste des propriétés ou une liste vide.
   */
  public List<String> getPropriete() {
    if (propriete == null) {
      propriete = new ArrayList<String>();
    }
    return propriete;
  }

  public void addPropriete(String prop) {
    this.propriete.add(prop);
  }

  public void removePropriete(String propName) {
    this.propriete =
        this.propriete.stream()
            .filter(prop -> !prop.equalsIgnoreCase(propName))
            .collect(Collectors.toList());
  }
}
