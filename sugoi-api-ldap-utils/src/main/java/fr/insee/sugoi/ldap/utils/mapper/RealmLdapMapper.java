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
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import java.util.List;
import org.springframework.stereotype.Component;

/** ProfilContextMapper */
@Component
public class RealmLdapMapper {

  public Realm mapFromSearchEntry(SearchResultEntry searchResultEntry) {
    Realm realm = new Realm();
    UserStorage userStorage = new UserStorage();
    realm.setName(searchResultEntry.getAttributeValue("cn").split("_")[1]);
    String[] inseeProperties = searchResultEntry.getAttributeValues("inseepropriete");
    for (String inseeProperty : inseeProperties) {
      String[] property = inseeProperty.split("\\$");
      // Test if property is valid
      if (property.length == 2) {
        if (property[0].equals("ldapUrl")) {
          realm.setUrl(property[1]);
        }
        if (property[0].contains("branchesApplicativesPossibles")) {
          realm.setAppSource(property[1]);
        }

        if (property[0].contains("brancheContact")) {
          userStorage.setUserSource(property[1]);
        }
        if (property[0].equals("brancheOrganisation")) {
          userStorage.setOrganizationSource(property[1]);
        }
        if (property[0].equals("brancheAdresse")) {
          userStorage.setAddressSource(property[1]);
        }
        if (property[0].equals("groupSourcePattern")) {
          userStorage.addProperty("group_source_pattern", property[1]);
        }
        if (property[0].equals("groupFilterPattern")) {
          userStorage.addProperty("group_filter_pattern", property[1]);
        }
      }
    }
    userStorage.setName("default");
    realm.setUserStorages(List.of(userStorage));
    return realm;
  }
}
