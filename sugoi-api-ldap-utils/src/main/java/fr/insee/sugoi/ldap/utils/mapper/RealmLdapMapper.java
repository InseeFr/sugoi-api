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
import fr.insee.sugoi.model.RealmConfigKeys;
import fr.insee.sugoi.model.RealmConfigKeysFinder;
import fr.insee.sugoi.model.technics.StoreMapping;
import fr.insee.sugoi.model.technics.UiField;
import fr.insee.sugoi.model.technics.exceptions.EntryIsNotUIFieldException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** ProfilContextMapper */
public class RealmLdapMapper {

  private static final Logger logger = LoggerFactory.getLogger(RealmLdapMapper.class);
  static RealmConfigKeysFinder realmConfigKeysConfiguration = new RealmConfigKeysFinder();

  static UiMappingService uiMappingService = new UiMappingService();

  public static Realm mapFromSearchEntry(SearchResultEntry searchResultEntry) {
    Realm realm = new Realm();
    // this means we can't have _ in a realm name !
    realm.setName(searchResultEntry.getAttributeValue("cn").split("_")[1]);
    if (searchResultEntry.getAttributeValue("description") != null) {
      realm
          .getProperties()
          .put(
              GlobalKeysConfig.REALM_DESCRIPTION,
              List.of(searchResultEntry.getAttributeValue("description")));
    }
    String[] inseeProperties = searchResultEntry.getAttributeValues("inseepropriete");
    for (String inseeProperty : inseeProperties) {
      String[] property = inseeProperty.split("\\$", 2);
      // Test if property is valid
      if (property.length == 2) {
        RealmConfigKeys configKey = realmConfigKeysConfiguration.getRealmConfigKey(property[0]);
        if (configKey != null) {
          if (configKey.equals(LdapConfigKeys.URL)) {
            realm.setUrl(property[1]);
          } else if (configKey.equals(GlobalKeysConfig.APP_SOURCE)) {
            realm.setAppSource(property[1]);
          } else if (configKey.equals(LdapConfigKeys.PORT)) {
            realm.setPort(property[1]);
          } else {
            if (realm.getProperties().containsKey(configKey)
                || realm.getProperties().get(configKey) == null) {
              realm.getProperties().put(configKey, new ArrayList<>());
            }
            realm.getProperties().get(configKey).add(property[1]);
          }
        } else if (property[0].equalsIgnoreCase("default_writer_type")) {
          realm.setWriterType(property[1]);
        } else if (property[0].equalsIgnoreCase("default_reader_type")) {
          realm.setReaderType(property[1]);
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
          try {
            realm
                .getUiMapping()
                .computeIfAbsent(UIMappingType.UI_ORGANIZATION_MAPPING, l -> new ArrayList<>())
                .add(new UiField(property[1]));
          } catch (EntryIsNotUIFieldException e) {
            logger.debug(e.getLocalizedMessage());
          }
        } else if (property[0].equalsIgnoreCase("uiUserMapping")) {
          try {
            realm
                .getUiMapping()
                .computeIfAbsent(UIMappingType.UI_USER_MAPPING, l -> new ArrayList<>())
                .add(new UiField(property[1]));
          } catch (EntryIsNotUIFieldException e) {
            logger.debug(e.getLocalizedMessage());
          }
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
              GlobalKeysConfig.REALM_DESCRIPTION.getName(),
              realm.getProperties().get(GlobalKeysConfig.REALM_DESCRIPTION).get(0)));
    }
    if (realm.getAppSource() != null) {
      attributes.add(
          new Attribute(
              "inseepropriete",
              String.format("%s$%s", GlobalKeysConfig.APP_SOURCE.getName(), realm.getAppSource())));
    }
    if (realm.getUiMapping().containsKey(Realm.UIMappingType.UI_USER_MAPPING)
        && realm.getUiMapping().get(Realm.UIMappingType.UI_USER_MAPPING) != null) {
      realm
          .getUiMapping()
          .get(Realm.UIMappingType.UI_USER_MAPPING)
          .forEach(
              uf ->
                  attributes.add(
                      new Attribute("inseepropriete", String.format("uiUserMapping$%s", uf))));
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
            (configKey, propertyValue) ->
                propertyValue.stream()
                    .forEach(
                        v ->
                            attributes.add(
                                new Attribute(
                                    "inseepropriete",
                                    String.format("%s$%s", configKey.getName(), v)))));
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
