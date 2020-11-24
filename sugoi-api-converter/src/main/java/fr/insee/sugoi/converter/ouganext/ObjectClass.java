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

import java.util.Arrays;

public enum ObjectClass {
  TOP("top"),
  ORGANIZATIONAL_UNIT("organizationalUnit"),

  /* Schema standard des domaines de gestion */
  /* Contacts */
  INSEE_CONTACT("inseeContact"),
  INSEE_COMPTE("inseeCompte"),
  /* Organisations */
  INSEE_ORGANISATION("inseeOrganisation"),
  /* Adresses */
  INSEE_ADRESSE_POSTALE("inseeAdressePostale"),
  LOCALITY("locality"),

  /* Profils */
  ORGANIZATIONAL_ROLE("organizationalRole"),
  INSEE_ORGANIZATIONAL_ROLE("inseeOrganizationalRole"),

  /* Schemas auxiliaires */
  INSEE_ATTRIBUTS_COMMUNICATION("inseeAttributsCommunication"),
  INSEE_ATTRIBUTS_AUTHENTIFICATION("inseeAttributsAuthentification"),
  INSEE_ATTRIBUTS_HABILITATION("inseeAttributsHabilitation"),

  /* Schema annuaire infra */
  PERSON("person"),
  ORGANIZATIONAL_PERSON("organizationalPerson"),
  INSEE_PERSON("inseePerson"),
  INET_ORG_PERSON("inetOrgPerson"),

  /* Schema alternatif */
  INSEE_COMPTE_ENQUETE("inseeCompteEnquete"),
  INSEE_AUTHENTICATED_USER("inseeAuthenticatedUser"),

  /* Specifique mairies */
  INSEE_MAIRIE("inseeMairie");

  public static final String ATTRIBUT_OBJECT_CLASS = "objectClass";

  private String name;

  private ObjectClass(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static ObjectClass getFromName(String name) {
    return Arrays.asList(ObjectClass.values()).stream()
        .filter(o -> o.name.equalsIgnoreCase(name))
        .findFirst()
        .orElse(null);
  }

  @Override
  public String toString() {
    return name;
  }
}
