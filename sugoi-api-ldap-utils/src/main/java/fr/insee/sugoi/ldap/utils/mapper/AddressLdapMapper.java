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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AddressLdapMapper {

  public Map<String, String> config;
  public String[] objectClasses = {"InseeAdressePostale", "locality", "top"};

  public AddressLdapMapper(Map<String, String> config) {
    // TODO Only if this mapper become more generic
    // if (config.get(LdapConfigKeys.ADDRESS_OBJECT_CLASSES) != null) {
    // objectClasses = config.get(LdapConfigKeys.ADDRESS_OBJECT_CLASSES).split(",");
    // }
  }

  public Map<String, String> mapFromSearchEntry(SearchResultEntry searchResultEntry) {
    Map<String, String> address = new HashMap<>();
    for (int i = 1; i < 8; i++) {
      address.put(
          "line" + String.valueOf(i),
          searchResultEntry.getAttributeValue(
              "inseeAdressePostaleCorrespondantLigne" + String.valueOf(i)));
    }
    return address;
  }

  public List<Attribute> mapToAttributes(Map<String, String> address) {
    List<Attribute> attributes = new ArrayList<>();
    for (int i = 1; i < 8; i++) {
      if (address.containsKey("line" + String.valueOf(i))
          && address.get("line" + String.valueOf(i)) != null) {
        attributes.add(
            new Attribute(
                "inseeAdressePostaleCorrespondantLigne" + String.valueOf(i),
                address.get("line" + String.valueOf(i))));
      }
    }
    attributes.add(new Attribute("objectClass", objectClasses));
    return attributes;
  }

  public List<Modification> createMods(Map<String, String> address) {
    return mapToAttributes(address).stream()
        .map(
            attribute ->
                new Modification(
                    ModificationType.REPLACE, attribute.getName(), attribute.getValues()))
        .collect(Collectors.toList());
  }
}
