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
package fr.insee.sugoi.ldap.utils.mapper.properties;

import fr.insee.sugoi.ldap.utils.mapper.ModelType;
import fr.insee.sugoi.ldap.utils.mapper.properties.utils.AttributeLdapName;
import fr.insee.sugoi.ldap.utils.mapper.properties.utils.MapToAttribute;
import fr.insee.sugoi.ldap.utils.mapper.properties.utils.MapToMapElement;

public class OrganizationLdap {

  @AttributeLdapName("uid")
  @MapToAttribute("identifiant")
  public String uid;

  @AttributeLdapName("description")
  @MapToMapElement(name = "attributes", key = "description")
  public String description;

  @AttributeLdapName("inseeAdressePostaleDN")
  @MapToAttribute(value = "address", type = ModelType.ADDRESS)
  public String adresseDn;

  @AttributeLdapName("inseeClefChiffrement")
  @MapToAttribute("gpgkey")
  public byte[] clef;

  @AttributeLdapName("mail")
  @MapToMapElement(name = "attributes", key = "mail")
  public String mail;

  @AttributeLdapName("inseeOrganisationDN")
  @MapToAttribute(value = "organization", type = ModelType.ORGANIZATION)
  public String organisationDeRattachementDN;
}
