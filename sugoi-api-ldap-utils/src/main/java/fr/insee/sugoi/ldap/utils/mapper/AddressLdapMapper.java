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
import fr.insee.sugoi.model.PostalAddress;
import java.util.ArrayList;
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

  public PostalAddress getAddressFromSearchEntry(SearchResultEntry searchResultEntry) {
    PostalAddress address = new PostalAddress();
    String[] lines = new String[7];
    for (int i = 1; i <= lines.length; i++) {
      lines[i - 1] =
          searchResultEntry.getAttributeValue("inseeAdressePostaleCorrespondantLigne" + i);
    }
    address.setLines(lines);
    return address;
  }

  public List<Attribute> addressToAttributes(PostalAddress address) {
    List<Attribute> attributes = new ArrayList<>();
    for (int i = 1; i <= address.getLines().length; i++) {
      if (address.getLines()[i - 1] != null && !address.getLines()[i - 1].isBlank()) {
        attributes.add(
            new Attribute("inseeAdressePostaleCorrespondantLigne" + i, address.getLines()[i - 1]));
      }
    }
    attributes.add(new Attribute("objectClass", objectClasses));
    return attributes;
  }

  public List<Modification> createMods(PostalAddress address) {
    return addressToAttributes(address).stream()
        .map(
            attribute ->
                new Modification(
                    ModificationType.REPLACE, attribute.getName(), attribute.getValues()))
        .collect(Collectors.toList());
  }
}
