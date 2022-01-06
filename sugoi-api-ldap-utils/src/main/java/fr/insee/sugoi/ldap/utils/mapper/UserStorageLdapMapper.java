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
import fr.insee.sugoi.ldap.utils.LdapUtils;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.model.technics.StoreMapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserStorageLdapMapper {

  public static UserStorage mapFromAttributes(Collection<Attribute> attributes) {
    UserStorage userStorage = new UserStorage();
    attributes.stream()
        .filter(attribute -> attribute.getName().equalsIgnoreCase("inseepropriete"))
        .forEach(
            attribute -> {
              for (String value : attribute.getValues()) {
                String[] property = value.split("\\$");
                if (property.length == 2) {
                  if (property[0].equalsIgnoreCase("brancheContact")) {
                    userStorage.setUserSource(property[1]);
                  }
                  if (property[0].equalsIgnoreCase("brancheOrganisation")) {
                    userStorage.setOrganizationSource(property[1]);
                  }
                  if (property[0].equalsIgnoreCase("brancheAdresse")) {
                    userStorage.setAddressSource(property[1]);
                  }
                  if (property[0].equalsIgnoreCase("groupSourcePattern")) {
                    userStorage.addProperty("group_source_pattern", property[1]);
                  }
                  if (property[0].equalsIgnoreCase("groupManagerSourcePattern")) {
                    userStorage.addProperty("group_manager_source_pattern", property[1]);
                  }
                  if (property[0].equalsIgnoreCase("groupFilterPattern")) {
                    userStorage.addProperty("group_filter_pattern", property[1]);
                  }
                  // ex with userMapping$username:uid,string,rw
                  else if (property[0].equalsIgnoreCase("userMapping")) {

                    if (userStorage.getUserMappings() == null) {
                      userStorage.setUserMappings(new ArrayList<>());
                    }
                    userStorage.getUserMappings().add(new StoreMapping(property[1]));

                  } else if (property[0].equalsIgnoreCase("organizationMapping")) {
                    if (userStorage.getOrganizationMappings() == null) {
                      userStorage.setOrganizationMappings(new ArrayList<>());
                    }
                    userStorage.getOrganizationMappings().add(new StoreMapping(property[1]));
                  }
                  if (property[0].equalsIgnoreCase(LdapConfigKeys.USER_OBJECT_CLASSES)) {
                    userStorage.addProperty(LdapConfigKeys.USER_OBJECT_CLASSES, property[1]);
                  }
                  if (property[0].equalsIgnoreCase(LdapConfigKeys.ORGANIZATION_OBJECT_CLASSES)) {
                    userStorage.addProperty(
                        LdapConfigKeys.ORGANIZATION_OBJECT_CLASSES, property[1]);
                  }
                  if (property[0].equalsIgnoreCase(LdapConfigKeys.GROUP_OBJECT_CLASSES)) {
                    userStorage.addProperty(LdapConfigKeys.GROUP_OBJECT_CLASSES, property[1]);
                  }
                  if (property[0].equalsIgnoreCase(LdapConfigKeys.APPLICATION_OBJECT_CLASSES)) {
                    userStorage.addProperty(LdapConfigKeys.APPLICATION_OBJECT_CLASSES, property[1]);
                  }
                  if (property[0].equalsIgnoreCase(LdapConfigKeys.ADDRESS_OBJECT_CLASSES)) {
                    userStorage.addProperty(LdapConfigKeys.ADDRESS_OBJECT_CLASSES, property[1]);
                  }
                }
              }
            });
    String name =
        attributes.stream()
            .filter(attribute -> attribute.getName().equalsIgnoreCase("cn"))
            .collect(Collectors.toList())
            .get(0)
            .getValue();
    userStorage.setName(name);
    return userStorage;
  }

  public static List<Attribute> mapToAttributes(UserStorage userStorage) {
    List<Attribute> attributes = new ArrayList<>();
    attributes.add(new Attribute("objectClass", "inseeOrganizationalRole"));
    if (userStorage.getUserSource() != null) {
      attributes.add(
          new Attribute(
              "inseepropriete", String.format("brancheContact$%s", userStorage.getUserSource())));
    }
    if (userStorage.getAddressSource() != null) {
      attributes.add(
          new Attribute(
              "inseepropriete",
              String.format("brancheAdresse$%s", userStorage.getAddressSource())));
    }
    if (userStorage.getOrganizationSource() != null) {
      attributes.add(
          new Attribute(
              "inseepropriete",
              String.format("brancheOrganisation$%s", userStorage.getOrganizationSource())));
    }
    if (userStorage.getProperties().containsKey("group_manager_source_pattern")) {
      userStorage.addProperty(
          "group_manager_source_pattern",
          userStorage.getProperties().get("group_manager_source_pattern"));
    }
    if (userStorage.getProperties().containsKey("group_source_pattern")) {
      attributes.add(
          new Attribute(
              "inseepropriete",
              String.format(
                  "groupSourcePattern$%s",
                  userStorage.getProperties().get("group_source_pattern"))));
    }
    if (userStorage.getProperties().containsKey("group_filter_pattern")) {
      attributes.add(
          new Attribute(
              "inseepropriete",
              String.format(
                  "groupFilterPattern$%s",
                  userStorage.getProperties().get("group_filter_pattern"))));
    }
    if (userStorage.getUserMappings() != null) {
      for (StoreMapping storeMapping : userStorage.getUserMappings()) {
        attributes.add(
            new Attribute(
                "inseepropriete",
                String.format("%s$%s", "userMapping", storeMapping.toStoreString())));
      }
    }
    if (userStorage.getOrganizationMappings() != null) {
      for (StoreMapping storeMapping : userStorage.getOrganizationMappings()) {
        attributes.add(
            new Attribute(
                "inseepropriete",
                String.format("%s$%s", "organizationMapping", storeMapping.toStoreString())));
      }
    }
    return attributes;
  }

  public static List<Modification> createMods(UserStorage userStorage) {
    return LdapUtils.convertAttributesToModifications(mapToAttributes(userStorage));
  }
}
