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
package fr.insee.sugoi.model;

import java.util.Arrays;
import java.util.Optional;

public enum PasswordPolicyConstants implements RealmConfigKeys {
  CREATE_PASSWORD_WITH_UPPERCASE("create_password_WITHUpperCase"),
  CREATE_PASSWORD_WITH_LOWERCASE("create_password_WITHLowerCase"),
  CREATE_PASSWORD_WITH_DIGITS("create_password_WITHDigit"),
  CREATE_PASSWORD_WITH_SPECIAL("create_password_WITHSpecial"),
  CREATE_PASSWORD_SIZE("create_password_size"),
  VALIDATE_PASSWORD_WITH_UPPERCASE("validate_password_WITHUpperCase"),
  VALIDATE_PASSWORD_WITH_LOWERCASE("validate_password_WITHLowerCase"),
  VALIDATE_PASSWORD_WITH_DIGITS("validate_password_WITHDigit"),
  VALIDATE_PASSWORD_WITH_SPECIAL("validate_password_WITHSpecial"),
  VALIDATE_PASSWORD_MIN_SIZE("validate_password_size");

  private String name;

  PasswordPolicyConstants(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  public static Optional<PasswordPolicyConstants> getPasswordPolicyConstant(String name) {
    return Arrays.stream(PasswordPolicyConstants.values())
        .filter(gkc -> gkc.getName().equalsIgnoreCase(name))
        .findFirst();
  }

  public static RealmConfigKeys getRealmConfigKey(String key) {
    return getPasswordPolicyConstant(key).orElse(null);
  }
}
