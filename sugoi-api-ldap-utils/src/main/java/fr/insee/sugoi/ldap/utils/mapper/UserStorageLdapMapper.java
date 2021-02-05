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
import fr.insee.sugoi.model.UserStorage;
import java.util.Collection;
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
                  if (property[0].equalsIgnoreCase("groupFilterPattern")) {
                    userStorage.addProperty("group_filter_pattern", property[1]);
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
}
