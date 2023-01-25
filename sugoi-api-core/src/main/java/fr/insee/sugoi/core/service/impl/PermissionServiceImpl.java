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
package fr.insee.sugoi.core.service.impl;

import fr.insee.sugoi.core.model.Right;
import fr.insee.sugoi.core.service.PermissionService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl implements PermissionService {

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

  public static final Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);

  public boolean isReader(List<String> roles, String realmName, String usName) {
    return areUserRolesInValidRoles(regexpReaderList, roles, realmName, usName, null)
        || isPasswordManager(roles, realmName, usName)
        || isWriter(roles, realmName, usName);
  }

  public boolean isPasswordManager(List<String> roles, String realmName, String usName) {
    return areUserRolesInValidRoles(passwordManagerRoleList, roles, realmName, usName, null)
        || isWriter(roles, realmName, usName);
  }

  public boolean isApplicationManager(List<String> roles, String realmName, String appName) {
    return (areUserRolesInValidRoles(applicationManagerRoleList, roles, realmName, null, appName)
            && isReader(roles, realmName, null))
        || isWriter(roles, realmName, null);
  }

  public boolean isWriter(List<String> roles, String realmName, String usName) {
    return areUserRolesInValidRoles(regexpWriterList, roles, realmName, usName, null)
        || isAdmin(roles);
  }

  public boolean isAdmin(List<String> roles) {
    return areUserRolesInValidRoles(adminRoleList, roles, null, null, null);
  }

  public boolean isValidAttributeAccordingAttributePattern(
      List<String> roles, String realm, String storage, String pattern, String attribute) {
    return getAllowedAttributePattern(roles, realm, storage, pattern).stream()
        .anyMatch(allowedPattern -> attribute.toUpperCase().matches(allowedPattern));
  }

  public List<String> getReaderRoles(List<String> roles) {
    return getStringRightsFromRegexList(roles, regexpReaderList);
  }

  public List<String> getWriterRoles(List<String> roles) {
    return getStringRightsFromRegexList(roles, regexpWriterList);
  }

  public List<String> getPasswordManagerRoles(List<String> roles) {
    return getStringRightsFromRegexList(roles, passwordManagerRoleList);
  }

  public List<String> getAppManagerRoles(List<String> roles) {
    return getStringRightsFromRegexList(roles, applicationManagerRoleList);
  }

  private List<String> getAllowedAttributePattern(
      List<String> roles, String realm, String storage, String pattern) {
    return getRightsFromRegexList(roles, applicationManagerRoleList).stream()
        .map(appRight -> getPatternFromRegex(pattern, realm, storage, appRight.getApplication()))
        .map(Pattern::pattern)
        .collect(Collectors.toList());
  }

  private static List<String> getStringRightsFromRegexList(
      List<String> roles, List<String> applicationManagerRoleList) {
    return getRightsFromRegexList(roles, applicationManagerRoleList).stream()
        .map(Right::toString)
        .collect(Collectors.toList());
  }

  private static List<Right> getRightsFromRegexList(List<String> roles, List<String> regexpList) {
    List<Pattern> validPatterns =
        regexpList.stream()
            .map(PermissionServiceImpl::createCatchingPattern)
            .collect(Collectors.toList());
    return roles.stream()
        .map(role -> transformRoleToRight(role, validPatterns))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  /**
   * Create a pattern from the regexp used to retrieve realm, userstorage and application from any
   * roles matching it via transformRoleToRight.
   *
   * @param regexp a String defining the form of a valid role (ex: READER_$(realm)_$(userstorage))
   * @return the catching pattern form this regexp (ex : READER_(?<realm>.*_(?<userStorage>.*)))
   */
  private static Pattern createCatchingPattern(String regexp) {
    return getPatternFromRegex(regexp, "(?<realm>.*)", "(?<userStorage>.*)", "(?<application>.*)");
  }

  private static boolean areUserRolesInValidRoles(
      List<String> regexpList,
      List<String> userRoles,
      String realmName,
      String usName,
      String appName) {
    logger.debug("Checking if user is in : {}", regexpList);
    return regexpList.stream()
        .map(regexp -> getPatternFromRegex(regexp, realmName, usName, appName))
        .anyMatch(
            validPattern ->
                userRoles.stream()
                    .anyMatch(userRole -> validPattern.matcher(userRole.toUpperCase()).matches()));
  }

  private static Pattern getPatternFromRegex(
      String regexp, String realmName, String usName, String appName) {
    Map<String, String> valueMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    if (realmName != null) {
      valueMap.put("realm", realmName.toUpperCase());
    }
    if (usName != null) {
      valueMap.put("userStorage", usName.toUpperCase());
    }
    if (appName != null) {
      valueMap.put("application", appName.toUpperCase());
    }
    return Pattern.compile(
        StrSubstitutor.replace(regexp, valueMap, "$(", ")").replace("*", ".*"),
        Pattern.CASE_INSENSITIVE);
  }

  private static Optional<Right> transformRoleToRight(String role, List<Pattern> patterns) {
    for (Pattern p : patterns) {
      Matcher m = p.matcher(role);
      if (m.matches()) {
        Right right = new Right();
        if (p.pattern().contains("?<REALM>")) {
          right.setRealm(m.group("REALM"));
        }
        if (p.pattern().contains("?<USERSTORAGE>")) {
          right.setUserStorage(m.group("USERSTORAGE"));
        }
        if (p.pattern().contains("?<APPLICATION>")) {
          right.setApplication(m.group("APPLICATION"));
        }
        return Optional.of(right);
      }
    }
    return Optional.empty();
  }
}
