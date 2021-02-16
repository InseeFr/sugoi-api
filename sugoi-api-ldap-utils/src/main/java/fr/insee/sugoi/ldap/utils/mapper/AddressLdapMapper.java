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
    for (int i = 1; i < 8; i++) {
      address.put(
          "Ligne" + String.valueOf(i),
          searchResultEntry.getAttributeValue(
              "inseeAdressePostaleCorrespondantLigne" + String.valueOf(i)));
    }
    return address;
  }

  public static List<Attribute> mapToAttributes(Map<String, String> address) {
    List<Attribute> attributes = new ArrayList<>();
    for (int i = 1; i < 8; i++) {
      if (address.containsKey("Ligne" + String.valueOf(i))
          && address.get("Ligne" + String.valueOf(i)) != null) {
        attributes.add(
            new Attribute(
                "inseeAdressePostaleCorrespondantLigne" + String.valueOf(i),
                address.get("Ligne" + String.valueOf(i))));
      }
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
