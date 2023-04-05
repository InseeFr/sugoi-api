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
package fr.insee.sugoi.core.configuration;

import fr.insee.sugoi.model.RealmConfigKeys;
import java.util.Arrays;
import java.util.Optional;

public enum GlobalKeysConfig implements RealmConfigKeys {
  REALM("realm"),
  REALM_DESCRIPTION("description"),
  USERSTORAGE("userStorage"),
  USER_SOURCE("user_source"),
  APP_SOURCE("branchesApplicativesPossibles"),
  ORGANIZATION_SOURCE("organization_source"),
  ADDRESS_SOURCE("address_source"),
  SEEALSO_ATTRIBUTES("seealso_attributes"),
  APP_MANAGED_ATTRIBUTE_KEYS_LIST("app_managed_attribute_key"),
  APP_MANAGED_ATTRIBUTE_PATTERNS_LIST("app_managed_attribute_pattern"),
  VERIFY_MAIL_UNICITY("enableMailUnicity"),
  USERS_MAX_OUTPUT_SIZE("usersMaxOutputSize"),
  APPLICATIONS_MAX_OUTPUT_SIZE("applicationsMaxOutputSize"),
  GROUPS_MAX_OUTPUT_SIZE("groupsMaxOutputSize"),
  ORGANIZATIONS_MAX_OUTPUT_SIZE("organizationsMaxOutputSize"),
  USER_USERSTORAGE_DEFINED_ATTRIBUTES("user_us_defined_attributes");

  private String name;

  GlobalKeysConfig(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  public static Optional<GlobalKeysConfig> getGlobalKeyConfig(String name) {
    return Arrays.stream(GlobalKeysConfig.values())
        .filter(gkc -> gkc.getName().equalsIgnoreCase(name))
        .findFirst();
  }

  public static RealmConfigKeys getRealmConfigKey(String key) {
    return getGlobalKeyConfig(key).orElse(null);
  }
}
