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
package fr.insee.sugoi.old.services.decider;

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
import org.springframework.stereotype.Component;

@Component("OldAuthorizeMethodDecider")
public class AuthorizeMethodDecider {

  @Value("${fr.insee.sugoi.api.old.regexp.role.consultant:}")
  private String regexpConsult;

  @Value("${fr.insee.sugoi.api.old.regexp.role.gestionnaire:}")
  private String regexpGest;

  @Value("${fr.insee.sugoi.api.old.regexp.role.admin:}")
  private String regexpAdmin;

  @Value("${fr.insee.sugoi.api.old.enable.preauthorize:false}")
  private boolean enable;

  private static final Logger logger = LogManager.getLogger(AuthorizeMethodDecider.class);

  public boolean isAtLeastConsultant(String domaine) {
    if (enable) {
      logger.info("Check if user is at least consultant on domaine {}", domaine);
      Map<String, String> valueMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      valueMap.put("domaine", domaine.toUpperCase());
      String searchRole = StrSubstitutor.replace(regexpConsult, valueMap);
      return checkIfUserGetRoles(searchRole) || isAtLeastGestionnaire(domaine);
    }
    logger.warn("PreAuthorize on request is disabled, can cause security problem");
    return true;
  }

  public boolean isAtLeastGestionnaire(String domaine) {
    if (enable) {
      logger.info("Check if user is at least gestionnaire on domaine {}", domaine);
      Map<String, String> valueMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      valueMap.put("domaine", domaine.toUpperCase());
      String searchRole = StrSubstitutor.replace(regexpGest, valueMap);
      return checkIfUserGetRoles(searchRole) || isAdmin();
    }
    logger.warn("PreAuthorize on request is disabled, can cause security problem");
    return true;
  }

  public boolean isAdmin() {
    if (enable) {
      logger.info("Check if user is at least admin");
      return checkIfUserGetRoles(regexpAdmin);
    }
    logger.warn("PreAuthorize on request is disabled, can cause security problem");
    return true;
  }

  private boolean checkIfUserGetRoles(String roleSearch) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    List<String> roles =
        authentication.getAuthorities().stream()
            .map(authority -> authority.getAuthority())
            .collect(Collectors.toList());
    if (roles.contains(roleSearch)) {
      return true;
    }
    return false;
  }
}
