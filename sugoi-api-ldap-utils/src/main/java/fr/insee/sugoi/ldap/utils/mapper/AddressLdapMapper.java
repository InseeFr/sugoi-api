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

import com.unboundid.ldap.sdk.SearchResultEntry;
import java.util.HashMap;
import java.util.Map;

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
}
