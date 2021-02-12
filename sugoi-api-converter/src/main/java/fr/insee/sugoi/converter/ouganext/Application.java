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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import fr.insee.sugoi.converter.utils.MapFromAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAttribute;

@JacksonXmlRootElement(localName = "application", namespace = Namespace.ANNUAIRE)
@JsonPropertyOrder({"name", "role"})
public class Application {

  @JacksonXmlProperty(namespace = Namespace.ANNUAIRE)
  @MapFromAttribute(attributeName = "name")
  private String name;

  @JacksonXmlProperty(namespace = Namespace.ANNUAIRE)
  private Collection<Role> role = new ArrayList<>();

  public Application(String appName) {
    this.name = appName;
  }

  public Application() {}

  @XmlAttribute(name = "name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /** @return la liste des roles ou une liste vide. */
  public Collection<Role> getRole() {
    if (role == null) {
      role = new ArrayList<Role>();
    }
    return role;
  }

  public void addRole(Role role) {
    this.role.add(role);
  }

  public void removeRole(String roleName) {
    this.role =
        this.role.stream()
            .filter(r -> !r.getName().equalsIgnoreCase(roleName))
            .collect(Collectors.toList());
  }
}
