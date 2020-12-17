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
package fr.insee.sugoi.services.services;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

  @Value("${fr.insee.sugoi.api.regexp.role.reader:}")
  private List<String> regexpReaderList;

  @Value("${fr.insee.sugoi.api.regexp.role.writer:}")
  private List<String> regexpWriterList;

  @Value("${fr.insee.sugoi.api.regexp.role.admin:}")
  private List<String> adminRoleList;

  private static final Logger logger = LogManager.getLogger(PermissionService.class);

  public boolean isAtLeastReader(String realm, String userStorage) {
    Map<String, String> valueMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    valueMap.put("realm", realm.toUpperCase());
    valueMap.put("userStorage", userStorage.toUpperCase());
    List<String> searchRoleList =
        regexpReaderList.stream()
            .map(regexpReader -> StrSubstitutor.replace(regexpReader, valueMap, "$(", ")"))
            .collect(Collectors.toList());
    return checkIfUserGetRoles(searchRoleList) || isAtLeastWriter(realm, userStorage);
  }

  public boolean isAtLeastWriter(String realm, String userStorage) {
    Map<String, String> valueMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    valueMap.put("realm", realm.toUpperCase());
    valueMap.put("userStorage", userStorage.toUpperCase());
    List<String> searchRoleList =
        regexpWriterList.stream()
            .map(regexpReader -> StrSubstitutor.replace(regexpReader, valueMap, "$(", ")"))
            .collect(Collectors.toList());
    return checkIfUserGetRoles(searchRoleList) || isAdmin();
  }

  public boolean isAdmin() {
    return checkIfUserGetRoles(adminRoleList);
  }

  private boolean checkIfUserGetRoles(List<String> rolesSearch) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    List<String> roles =
        authentication.getAuthorities().stream()
            .map(authority -> authority.getAuthority())
            .collect(Collectors.toList());
    for (String roleSearch : rolesSearch) {
      if (roles.contains(roleSearch)) {
        return true;
      }
    }
    return false;
  }
}
