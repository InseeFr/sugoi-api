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
package fr.insee.sugoi.model;

import java.util.List;

public class OrganizationMapping {
  private String address;

  private String identifiant;

  private String organization;

  private List<AttributesSugoi> attributes;

  @Override
  public String toString() {
    return "OrganizationMapping{"
        + "address='"
        + address
        + '\''
        + ", identifiant='"
        + identifiant
        + '\''
        + ", organization='"
        + organization
        + '\''
        + ", attributes="
        + attributes
        + '}';
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getIdentifiant() {
    return identifiant;
  }

  public void setIdentifiant(String identifiant) {
    this.identifiant = identifiant;
  }

  public String getOrganization() {
    return organization;
  }

  public void setOrganization(String organization) {
    this.organization = organization;
  }

  public List<AttributesSugoi> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<AttributesSugoi> attributes) {
    this.attributes = attributes;
  }
}
