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
package fr.insee.sugoi.ldap.utils.config;

import fr.insee.sugoi.model.RealmConfigKeys;
import java.util.Arrays;
import java.util.Optional;

public enum LdapConfigKeys implements RealmConfigKeys {
  URL("ldapUrl"),
  PORT("ldapPort"),
  USERNAME("username"),
  PASSWORD("password"),
  POOL_SIZE("pool_size"),
  GROUP_SOURCE_PATTERN("groupSourcePattern"),
  GROUP_MANAGER_SOURCE_PATTERN("group_manager_source_pattern"),
  GROUP_FILTER_PATTERN("groupFilterPattern"),
  REALM_NAME("realm_name"),
  VLV_ENABLED("vlvEnabled"),
  SORT_KEY("sortKey"),
  USER_OBJECT_CLASSES("user_object_classes"),
  ORGANIZATION_OBJECT_CLASSES("organization_object_classes"),
  GROUP_OBJECT_CLASSES("group_object_classes"),
  APPLICATION_OBJECT_CLASSES("application_object_classes"),
  ADDRESS_OBJECT_CLASSES("address_object_classes"),
  USERSTORAGE_NAME("userstorage_name"),
  READ_CONNECTION_AUTHENTICATED("read_connection_authenticated");

  private String name;

  LdapConfigKeys(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  public static Optional<LdapConfigKeys> getLdapConfigKeys(String name) {
    return Arrays.stream(LdapConfigKeys.values())
        .filter(gkc -> gkc.getName().equalsIgnoreCase(name))
        .findFirst();
  }

  public static RealmConfigKeys getRealmConfigKey(String key) {
    return getLdapConfigKeys(key).orElse(null);
  }
}
