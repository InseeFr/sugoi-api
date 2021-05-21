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

@LdapObjectClass(
    values = {
      "top",
      "inseeCompte",
      "inseeContact",
      "inseeAttributsAuthentification",
      "inseeAttributsHabilitation",
      "inseeAttributsCommunication"
    },
    rdnAttributeName = "uid")
public class UserLdap {

  @MapToAttribute("username")
  @AttributeLdapName("uid")
  public String identifiant;

  @AttributeLdapName("cn")
  @MapToMapElement(name = "attributes", key = "common_name")
  public String nomCommun;

  @AttributeLdapName("sn")
  @MapToAttribute("lastName")
  public String nom;

  @AttributeLdapName("givenName")
  @MapToAttribute("firstName")
  public String prenom;

  @AttributeLdapName("mail")
  @MapToAttribute("mail")
  public String adresseMessagerie;

  @AttributeLdapName("telephoneNumber")
  @MapToMapElement(name = "attributes", key = "phone_number")
  public String numeroTelephone;

  @AttributeLdapName("description")
  @MapToMapElement(name = "attributes", key = "description")
  public String description;

  @AttributeLdapName("inseeAdressePostaleDN")
  @MapToAttribute(value = "address", type = ModelType.ADDRESS)
  public String adresseDN;

  @AttributeLdapName("inseeOrganisationDN")
  @MapToAttribute(value = "organization", type = ModelType.ORGANIZATION)
  public String organisationDeRattachementDN;

  @AttributeLdapName("personalTitle")
  @MapToMapElement(name = "attributes", key = "personal_title")
  public String civilite;

  @AttributeLdapName("inseeIdentifiantMetier")
  @MapToMapElement(name = "attributes", key = "identifiant_metier")
  public String identifiantMetier;

  @AttributeLdapName("inseerepertoirededistribution")
  @MapToMapElement(name = "attributes", key = "repertoire_distribution")
  public String repertoireDeDistribution;

  @AttributeLdapName("inseenumerotelephoneportable")
  @MapToMapElement(name = "attributes", key = "telephone_portable")
  public String telephonePortable;

  @AttributeLdapName("facsimiletelephonenumber")
  public String facSimile;

  @AttributeLdapName("o")
  @MapToMapElement(name = "attributes", key = "organisation")
  private String organisation;

  @AttributeLdapName("inseeTimbre")
  @MapToMapElement(name = "attributes", key = "insee_timbre")
  private String inseeTimbre;

  @AttributeLdapName("inseeOrganisme")
  @MapToMapElement(name = "attributes", key = "insee_organisme")
  private String inseeOrganisme;

  @AttributeLdapName("inseeRoleApplicatif")
  @MapToMapElement(
      name = "attributes",
      key = "insee_roles_applicatifs",
      type = ModelType.LIST_STRING)
  private String[] inseeRolesApplicatifs;

  @AttributeLdapName("inseePropriete")
  @MapToMapElement(name = "attributes", key = "properties", type = ModelType.LIST_STRING)
  private String inseeProperties;

  @AttributeLdapName("inseeAdresseCorrespondantLigne1")
  private String inseeAdresseCorrespondantLigne1;

  @AttributeLdapName("inseeAdresseCorrespondantLigne2")
  private String inseeAdresseCorrespondantLigne2;

  @AttributeLdapName("inseeAdresseCorrespondantLigne3")
  private String inseeAdresseCorrespondantLigne3;

  @AttributeLdapName("postalCode")
  private String postalCode;

  @AttributeLdapName("inseenomcorrespondant")
  private String inseeNomCorrespondant;

  @AttributeLdapName("inseemailcorrespondant")
  private String inseeMailCorrespondant;

  @AttributeLdapName("inseetelephonenumbercorrespondant")
  private String inseeTelephoneNumberCorrespondant;

  @MapToMapElement(name = "metadatas", key = "modifyTimestamp", readonly = true)
  @AttributeLdapName("modifyTimestamp")
  private String dateModification;

  @AttributeLdapName("inseeGroupeDefaut")
  @MapToAttribute(value = "habilitations", type = ModelType.LIST_HABILITATION)
  private String habilitations;

  @AttributeLdapName("memberOf")
  @MapToAttribute(value = "groups", type = ModelType.LIST_GROUP, readonly = true)
  private String groups;

  @AttributeLdapName("inseeMailCorrespondant")
  @MapToMapElement(name = "attributes", key = "additionalMail")
  private String additionalMail;

  @AttributeLdapName("seeAlso")
  @MapToMapElement(name = "attributes", key = "seeAlsos", type = ModelType.LIST_STRING)
  private String seeAlsos;
}
