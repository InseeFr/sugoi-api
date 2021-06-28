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
import com.unboundid.ldap.sdk.SearchResultEntry;
import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.ldap.utils.LdapUtils;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.model.Realm;
import java.util.ArrayList;
import java.util.List;

/** ProfilContextMapper */
public class RealmLdapMapper {

  public static Realm mapFromSearchEntry(SearchResultEntry searchResultEntry) {
    Realm realm = new Realm();
    // this means we can't have _ in a realm name !
    realm.setName(searchResultEntry.getAttributeValue("cn").split("_")[1]);
    String[] inseeProperties = searchResultEntry.getAttributeValues("inseepropriete");
    for (String inseeProperty : inseeProperties) {
      String[] property = inseeProperty.split("\\$", 2);
      // Test if property is valid
      if (property.length == 2) {
        if (property[0].equalsIgnoreCase("ldapUrl")) {
          realm.setUrl(property[1]);
        } else if (property[0].equalsIgnoreCase("branchesApplicativesPossibles")) {
          realm.setAppSource(property[1]);
        } else if (property[0].equalsIgnoreCase("seealso_attributes")) {
          realm.addProperty(GlobalKeysConfig.SEEALSO_ATTRIBUTES, property[1]);
        } else if (property[0].equalsIgnoreCase("vlv_enabled")) {
          realm.addProperty(LdapConfigKeys.VLV_ENABLED, property[1]);
        } else if (property[0].equalsIgnoreCase("default_writer_type")) {
          realm.setWriterType(property[1]);
        } else if (property[0].equalsIgnoreCase("default_reader_type")) {
          realm.setReaderType(property[1]);
        } else if (property[0].equalsIgnoreCase("app_managed_attribute_key")) {
          realm.addProperty(GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_KEYS_LIST, property[1]);
        } else if (property[0].equalsIgnoreCase("app_managed_attribute_pattern")) {
          realm.addProperty(GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_PATTERNS_LIST, property[1]);
        } else if (property[0].equalsIgnoreCase("sort_key")) {
          realm.addProperty(LdapConfigKeys.SORT_KEY, property[1]);
        }
      }
    }
    return realm;
  }

  public static List<Attribute> mapToAttributes(
      Realm realm, String realmEntryPattern, String baseDn) {
    List<Attribute> attributes = new ArrayList<>();
    attributes.add(new Attribute("objectClass", "inseeOrganizationalRole"));
    if (realm.getUrl() != null) {
      attributes.add(new Attribute("inseepropriete", String.format("ldapUrl$%s", realm.getUrl())));
    }
    if (realm.getAppSource() != null) {
      attributes.add(
          new Attribute(
              "inseepropriete",
              String.format("branchesApplicativesPossibles$%s", realm.getAppSource())));
    }
    realm
        .getProperties()
        .forEach(
            (propertyName, propertyValue) ->
                attributes.add(
                    new Attribute(
                        "inseepropriete", String.format("%s$%s", propertyName, propertyValue))));
    return attributes;
  }

  public static List<Modification> createMods(
      Realm realm, String realmEntryPattern, String baseDn) {
    return LdapUtils.convertAttributesToModifications(
        mapToAttributes(realm, realmEntryPattern, baseDn));
  }
}
