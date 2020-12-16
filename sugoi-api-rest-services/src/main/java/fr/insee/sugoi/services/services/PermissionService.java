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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

  @Value("${fr.insee.sugoi.api.regexp.role.reader:}")
  private String regexpReader;

  @Value("${fr.insee.sugoi.api.regexp.role.writer:}")
  private String regexpWriter;

  @Value("${fr.insee.sugoi.api.regexp.role.admin:}")
  private String regexpAdmin;

  private static final Logger logger = LogManager.getLogger(PermissionService.class);

  public boolean isAtLeastReader(String realm, String userStorage) {
    return checkIfUserInRole(regexpReader) || isAtLeastWriter(realm, userStorage);
  }

  public boolean isAtLeastWriter(String realm, String userStorage) {
    return checkIfUserInRole(regexpWriter) || isAdmin();
  }

  public boolean isAdmin() {
    return checkIfUserInRole(regexpAdmin);
  }

  public List<String> extractSugoiRole() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    List<String> rights = new ArrayList<>();
    if (isAdmin()) {
      rights.add(regexpAdmin);
    } else {
      List<String> readRights = authentication.getAuthorities().stream()
          .map(authority -> extractRole(authority.getAuthority(), regexpReader)).filter(authority -> authority != null)
          .collect(Collectors.toList());
      List<String> writeRights = authentication.getAuthorities().stream()
          .map(authority -> extractRole(authority.getAuthority(), regexpWriter)).filter(authority -> authority != null)
          .collect(Collectors.toList());
      rights.addAll(readRights);
      rights.addAll(writeRights);
    }
    logger.debug("Droit de l'utilisateur {} : {}", authentication.getPrincipal().toString(), rights);
    return rights;
  }

  public List<String> getRealmNameReader() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    List<String> rights = new ArrayList<>();
    List<String> readRights = authentication.getAuthorities().stream()
        .map(authority -> extractRealm(authority.getAuthority(), regexpReader)).filter(authority -> authority != null)
        .collect(Collectors.toList());
    rights.addAll(readRights);
    return rights;
  }

  public List<String> getRealmNameWriter() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    List<String> rights = new ArrayList<>();
    List<String> writeRights = authentication.getAuthorities().stream()
        .map(authority -> extractRealm(authority.getAuthority(), regexpWriter)).filter(authority -> authority != null)
        .collect(Collectors.toList());
    rights.addAll(writeRights);
    return rights;
  }

  public List<String> getMyRealm() {
    List<String> rights = getRealmNameReader();
    rights.addAll(getRealmNameWriter());
    return new ArrayList<>(new HashSet<>(rights));
  }

  private String extractRole(String authority, String regexp) {
    Pattern pattern = Pattern.compile(regexp);
    Matcher matcher = pattern.matcher(authority);
    if (matcher.matches()) {
      return authority;
    }
    return null;
  }

  private String extractRealm(String authority, String regexp) {
    Pattern pattern = Pattern.compile(regexp);
    Matcher matcher = pattern.matcher(authority);
    if (matcher.matches()) {
      return matcher.group(1);
    }
    return null;
  }

  private boolean checkIfUserInRole(String regexp) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication.getAuthorities().stream().map(authority -> extractRole(authority.getAuthority(), regexp))
        .filter(authority -> authority != null).collect(Collectors.toList()).size() > 0;
  }
}
