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
package fr.insee.sugoi.core.service;

import fr.insee.sugoi.core.model.SugoiUser;
import java.util.List;

public interface PermissionService {

  public boolean isReader(SugoiUser sugoiUser, String realm, String userStorage);

  public boolean isPasswordManager(SugoiUser sugoiUser, String realm, String userStorage);

  public boolean isApplicationManager(
      SugoiUser sugoiUser, String realm, String userStorage, String application);

  public boolean isApplicationManager(SugoiUser sugoiUser, String realm);

  public boolean isWriter(SugoiUser sugoiUser, String realm, String userStorage);

  public boolean isAdmin(SugoiUser sugoiUser);

  public boolean isValidAttributeAccordingAttributePattern(
      SugoiUser sugoiUser, String realm, String storage, String pattern, String attribute);

  public List<String> getUserRealmReader(SugoiUser sugoiUser);

  public List<String> getUserRealmWriter(SugoiUser sugoiUser);

  public List<String> getUserRealmPasswordManager(SugoiUser sugoiUser);

  public List<String> getUserRealmAppManager(SugoiUser sugoiUser);

  public List<String> getAllowedAttributePattern(
      SugoiUser sugoiUser, String realm, String storage, String pattern);
}
