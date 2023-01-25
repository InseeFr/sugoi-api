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

import java.util.List;

public interface PermissionService {

  public boolean isReader(List<String> roles, String realmName, String usName);

  public boolean isPasswordManager(List<String> roles, String realmName, String usName);

  public boolean isApplicationManager(List<String> roles, String realmName, String appName);

  public boolean isWriter(List<String> roles, String realmName, String usName);

  public boolean isAdmin(List<String> roles);

  public boolean isValidAttributeAccordingAttributePattern(
      List<String> roles, String realm, String storage, String pattern, String attribute);

  public List<String> getReaderRoles(List<String> roles);

  public List<String> getWriterRoles(List<String> roles);

  public List<String> getPasswordManagerRoles(List<String> roles);

  public List<String> getAppManagerRoles(List<String> roles);
}
