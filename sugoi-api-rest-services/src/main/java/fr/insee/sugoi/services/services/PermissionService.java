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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

  @Value("${fr.insee.sugoi.api.regexp.role.password.manager:}")
  private List<String> passwordManagerRoleList;

  @Value("${fr.insee.sugoi.api.regexp.role.application.manager:}")
  private List<String> applicationManagerRoleList;

  public static final Logger logger = LoggerFactory.getLogger(PermissionService.class);

  public boolean isReader(String realm, String userStorage) {
    List<String> searchRoleList = getSearchRoleList(realm, userStorage, null, regexpReaderList);
    return checkIfUserGetRoles(searchRoleList)
        || isWriter(realm, userStorage)
        || isApplicationManager(realm);
  }

  public boolean isPasswordManager(String realm, String userStorage) {
    List<String> searchRoleList =
        getSearchRoleList(realm, userStorage, null, passwordManagerRoleList);
    return checkIfUserGetRoles(searchRoleList);
  }

  public boolean isApplicationManager(String realm, String userStorage, String application) {
    List<String> searchRoleList =
        getSearchRoleList(realm, userStorage, application, applicationManagerRoleList);
    return checkIfUserGetRoles(searchRoleList);
  }

  public boolean isApplicationManager(String realm) {
    List<String> searchRoleList = getSearchRoleList(realm, "*", "*", applicationManagerRoleList);
    return checkIfUserGetRoles(searchRoleList);
  }

  public boolean isWriter(String realm, String userStorage) {
    List<String> searchRoleList = getSearchRoleList(realm, userStorage, null, regexpWriterList);
    return checkIfUserGetRoles(searchRoleList) || isAdmin();
  }

  public boolean isAdmin() {
    return checkIfUserGetRoles(adminRoleList);
  }

  private boolean checkIfUserGetRoles(List<String> rolesSearch) {
    logger.debug("Checking if user is in : {}", rolesSearch);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    List<String> roles =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(String::toUpperCase)
            .collect(Collectors.toList());
    logger.debug("User roles: {}", roles);
    for (String roleSearch : rolesSearch) {
      logger.trace(roleSearch);
      if (roles.contains(roleSearch.toUpperCase())) {
        return true;
      }
      for (String role : roles) {
        if (role.toUpperCase().matches(roleSearch.replaceAll("\\*", ".*").toUpperCase())) {
          return true;
        }
      }
    }
    return false;
  }

  private List<String> getSearchRoleList(
      String realm, String userStorage, String application, List<String> regexpList) {
    Map<String, String> valueMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    valueMap.put("realm", realm.toUpperCase());
    if (userStorage != null) {
      valueMap.put("userStorage", userStorage.toUpperCase());
    }
    if (application != null) {
      valueMap.put("application", application);
    }
    return regexpList.stream()
        .map(regexp -> StrSubstitutor.replace(regexp, valueMap, "$(", ")"))
        .collect(Collectors.toList());
  }
}
