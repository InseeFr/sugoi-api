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
package fr.insee.sugoi.event.listener.webhook.service;

import fr.insee.sugoi.model.RealmConfigKeys;
import java.util.Arrays;
import java.util.Optional;

public enum TemplateConfigKeys implements RealmConfigKeys {
  LOGIN_TEMPLATE("send_login_template"),
  RESET_TEMPLATE("reset_template"),
  CHANGEPWD_TEMPLATE("changepwd_template");

  private String name;

  TemplateConfigKeys(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  private static Optional<TemplateConfigKeys> getTemplateConfigKeys(String name) {
    return Arrays.stream(TemplateConfigKeys.values())
        .filter(tkc -> tkc.getName().equalsIgnoreCase(name))
        .findFirst();
  }

  public static RealmConfigKeys getRealmConfigKey(String key) {
    return getTemplateConfigKeys(key).orElse(null);
  }
}
