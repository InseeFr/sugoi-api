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
import fr.insee.sugoi.core.configuration.UiMappingService;
import fr.insee.sugoi.ldap.utils.LdapUtils;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.Realm.UIMappingType;
import fr.insee.sugoi.model.technics.StoreMapping;

import java.util.ArrayList;
import java.util.List;

/** ProfilContextMapper */
public class RealmLdapMapper {

  static UiMappingService uiMappingService = new UiMappingService();

  public static Realm mapFromSearchEntry(SearchResultEntry searchResultEntry) {
    Realm realm = new Realm();
    // this means we can't have _ in a realm name !
    realm.setName(searchResultEntry.getAttributeValue("cn").split("_")[1]);
    realm.addProperty(
        GlobalKeysConfig.REALM_DESCRIPTION, searchResultEntry.getAttributeValue("description"));
    String[] inseeProperties = searchResultEntry.getAttributeValues("inseepropriete");
    for (String inseeProperty : inseeProperties) {
      String[] property = inseeProperty.split("\\$", 2);
      // Test if property is valid
      if (property.length == 2) {
        if (property[0].equalsIgnoreCase("ldapUrl")) {
          realm.setUrl(property[1]);
        } else if (property[0].equalsIgnoreCase("enableMailUnicity")) {
          realm.addProperty(GlobalKeysConfig.VERIFY_MAIL_UNICITY, property[1]);
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

        } else if (property[0].equalsIgnoreCase("applicationMapping")) {

          if (realm.getApplicationMappings() == null) {
            realm.setApplicationMappings(new ArrayList<>());
          }
          realm.getApplicationMappings().add(new StoreMapping(property[1]));

        } else if (property[0].equalsIgnoreCase("groupMapping")) {
          if (realm.getGroupMappings() == null) {
            realm.setGroupMappings(new ArrayList<>());
          }
          realm.getGroupMappings().add(new StoreMapping(property[1]));
        } else if (property[0].equalsIgnoreCase("uiOrganizationMapping")) {

          realm
              .getUiMapping()
              .computeIfAbsent(UIMappingType.UI_ORGANIZATION_MAPPING, l -> new ArrayList<>())
              .add(uiMappingService.convertStringToUIField(property[1]));
        } else if (property[0].equalsIgnoreCase("uiUserMapping")) {
          realm
              .getUiMapping()
              .computeIfAbsent(UIMappingType.UI_USER_MAPPING, l -> new ArrayList<>())
              .add(uiMappingService.convertStringToUIField(property[1]));
        }
      }
    }
    return realm;
  }

  // inseePropriété:
  // userUiMapping$name;helpTextTitle;helpText;path;type;modifiable;tag;options1:value,options2:value;

  public static List<Attribute> mapToAttributes(
      Realm realm, String realmEntryPattern, String baseDn) {
    List<Attribute> attributes = new ArrayList<>();
    attributes.add(new Attribute("objectClass", "inseeOrganizationalRole"));
    if (realm.getUrl() != null) {
      attributes.add(new Attribute("inseepropriete", String.format("ldapUrl$%s", realm.getUrl())));
    }
    if (realm.getProperties().containsKey(GlobalKeysConfig.REALM_DESCRIPTION)) {
      attributes.add(
          new Attribute(
              "description", realm.getProperties().get(GlobalKeysConfig.REALM_DESCRIPTION)));
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
    if (realm.getApplicationMappings() != null) {
      for (StoreMapping storeMapping : realm.getApplicationMappings()) {
        attributes.add(
            new Attribute(
                "inseepropriete",
                String.format("%s$%s", "applicationMapping", storeMapping.toStoreString())));
      }
    }
    if (realm.getGroupMappings() != null) {
      for (StoreMapping storeMapping : realm.getGroupMappings()) {
        attributes.add(
            new Attribute(
                "inseepropriete",
                String.format("%s$%s", "groupMapping", storeMapping.toStoreString())));
      }
    }

    return attributes;
  }

  public static List<Modification> createMods(
      Realm realm, String realmEntryPattern, String baseDn) {
    return LdapUtils.convertAttributesToModifications(
        mapToAttributes(realm, realmEntryPattern, baseDn));
  }
}
