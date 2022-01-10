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
import java.util.ArrayList;
import java.util.HashMap;
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
        } else if (property[0].equalsIgnoreCase("ldapPort")) {
          realm.setPort(property[1]);
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

        } else if (property[0].equalsIgnoreCase("groupMapping")
            || property[0].equalsIgnoreCase("applicationMapping")) {

          if (!realm.getMappings().containsKey(property[0])) {
            realm.getMappings().put(property[0], new HashMap<>());
          }
          String[] attributeSplits = property[1].split(":");
          String attributeName = attributeSplits[0];
          String attributeMappingValues = attributeSplits[1];
          realm.getMappings().get(property[0]).put(attributeName, attributeMappingValues);
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
        } else {
          realm.addProperty(property[0], property[1]);
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
    if (realm.getPort() != null) {
      attributes.add(
          new Attribute("inseepropriete", String.format("ldapPort$%s", realm.getPort())));
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
    if (realm.getUiMapping().containsKey(Realm.UIMappingType.UI_USER_MAPPING)
        && realm.getUiMapping().get(Realm.UIMappingType.UI_USER_MAPPING) != null) {
      realm
          .getUiMapping()
          .get(Realm.UIMappingType.UI_USER_MAPPING)
          .forEach(
              uf ->
                  attributes.add(
                      new Attribute(
                          "inseepropriete",
                          String.format(
                              "uiUserMapping$%s", uiMappingService.convertUIFieldToString(uf)))));
    }
    if (realm.getUiMapping().containsKey(Realm.UIMappingType.UI_ORGANIZATION_MAPPING)
        && realm.getUiMapping().get(Realm.UIMappingType.UI_ORGANIZATION_MAPPING) != null) {
      realm
          .getUiMapping()
          .get(Realm.UIMappingType.UI_ORGANIZATION_MAPPING)
          .forEach(
              uf ->
                  attributes.add(
                      new Attribute(
                          "inseepropriete", String.format("uiOrganizationMapping$%s", uf))));
    }
    realm
        .getProperties()
        .forEach(
            (propertyName, propertyValue) ->
                attributes.add(
                    new Attribute(
                        "inseepropriete", String.format("%s$%s", propertyName, propertyValue))));
    for (String entityKey : realm.getMappings().keySet()) {
      for (String attributeName : realm.getMappings().get(entityKey).keySet()) {
        attributes.add(
            new Attribute(
                "inseepropriete",
                String.format(
                    "%s$%s:%s",
                    entityKey,
                    attributeName,
                    realm.getMappings().get(entityKey).get(attributeName))));
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
