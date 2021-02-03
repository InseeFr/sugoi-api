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
package fr.insee.sugoi.ldap.utils.mapper;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.SearchResultEntry;
import fr.insee.sugoi.ldap.utils.mapper.properties.AddressLdap;
import fr.insee.sugoi.ldap.utils.mapper.properties.LdapObjectClass;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AddressLdapMapper {

  public static Map<String, String> mapFromSearchEntry(SearchResultEntry searchResultEntry) {
    Map<String, String> address = new HashMap<>();
    address.put(
        "Ligne1", searchResultEntry.getAttributeValue("inseeAdressePostaleCorrespondantLigne1"));
    address.put(
        "Ligne2", searchResultEntry.getAttributeValue("inseeAdressePostaleCorrespondantLigne2"));
    address.put(
        "Ligne3", searchResultEntry.getAttributeValue("inseeAdressePostaleCorrespondantLigne3"));
    address.put(
        "Ligne4", searchResultEntry.getAttributeValue("inseeAdressePostaleCorrespondantLigne4"));
    address.put(
        "Ligne5", searchResultEntry.getAttributeValue("inseeAdressePostaleCorrespondantLigne5"));
    address.put(
        "Ligne6", searchResultEntry.getAttributeValue("inseeAdressePostaleCorrespondantLigne6"));
    address.put(
        "Ligne7", searchResultEntry.getAttributeValue("inseeAdressePostaleCorrespondantLigne7"));
    return address;
  }

  public static List<Attribute> mapToAttributes(Map<String, String> address) {
    List<Attribute> attributes = new ArrayList<>();
    if (address.containsKey("Ligne1")) {
      attributes.add(
          new Attribute("inseeAdressePostaleCorrespondantLigne1", address.get("Ligne1")));
    }
    if (address.containsKey("Ligne2")) {
      attributes.add(
          new Attribute("inseeAdressePostaleCorrespondantLigne2", address.get("Ligne2")));
    }
    if (address.containsKey("Ligne3")) {
      attributes.add(
          new Attribute("inseeAdressePostaleCorrespondantLigne3", address.get("Ligne3")));
    }
    if (address.containsKey("Ligne4")) {
      attributes.add(
          new Attribute("inseeAdressePostaleCorrespondantLigne4", address.get("Ligne4")));
    }
    if (address.containsKey("Ligne5")) {
      attributes.add(
          new Attribute("inseeAdressePostaleCorrespondantLigne5", address.get("Ligne5")));
    }
    if (address.containsKey("Ligne6")) {
      attributes.add(
          new Attribute("inseeAdressePostaleCorrespondantLigne6", address.get("Ligne6")));
    }
    if (address.containsKey("Ligne7")) {
      attributes.add(
          new Attribute("inseeAdressePostaleCorrespondantLigne7", address.get("Ligne7")));
    }
    attributes.add(
        new Attribute(
            "objectClass", AddressLdap.class.getAnnotation(LdapObjectClass.class).values()));
    return attributes;
  }

  public static List<Modification> createMods(Map<String, String> address) {
    return mapToAttributes(address).stream()
        .map(
            attribute ->
                new Modification(
                    ModificationType.REPLACE, attribute.getName(), attribute.getValues()))
        .collect(Collectors.toList());
  }
}
