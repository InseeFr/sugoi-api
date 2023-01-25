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
package fr.insee.sugoi.services.decider;

import fr.insee.sugoi.core.model.SugoiUser;
import fr.insee.sugoi.core.service.PermissionService;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("NewAuthorizeMethodDecider")
public class AuthorizeMethodDecider {

  private static final Logger logger = LoggerFactory.getLogger(AuthorizeMethodDecider.class);

  @Value("${fr.insee.sugoi.api.enable.preauthorize}")
  private boolean enable;

  @Autowired PermissionService permissionService;

  public boolean isReader(String realm, String userStorage) {
    if (enable) {
      SugoiUser user = getCurrentSugoiUser();
      logger.info(
          "Check if user {} is reader on realm {} and userStorage {}",
          user.getName(),
          realm,
          userStorage);
      return permissionService.isReader(user.getRoles(), realm, userStorage);
    }
    logger.warn("PreAuthorize on request is disabled, can cause security problem");
    return true;
  }

  public boolean isAppManager(String realm, String application) {
    if (enable) {
      SugoiUser user = getCurrentSugoiUser();
      logger.info("Check if {} user is at least reader on realm {}", user.getName(), realm);
      return permissionService.isApplicationManager(user.getRoles(), realm, application);
    }
    logger.warn("PreAuthorize on request is disabled, can cause security problem");
    return true;
  }

  public boolean isPasswordManager(String realm, String userStorage) {
    if (enable) {
      SugoiUser user = getCurrentSugoiUser();
      logger.info(
          "Check if user {} is at least reader on realm {} and userStorage {}",
          user.getName(),
          realm,
          userStorage);
      return permissionService.isPasswordManager(user.getRoles(), realm, userStorage);
    }
    logger.warn("PreAuthorize on request is disabled, can cause security problem");
    return true;
  }

  public boolean isWriter(String realm, String userStorage) {
    if (enable) {
      SugoiUser user = getCurrentSugoiUser();
      logger.info(
          "Check if user {} is at least writer on realm {} and userStorage {}",
          user.getName(),
          realm,
          userStorage);
      return permissionService.isWriter(user.getRoles(), realm, userStorage);
    }
    logger.warn("PreAuthorize on request is disabled, can cause security problem");
    return true;
  }

  public boolean isAdmin() {
    if (enable) {
      SugoiUser user = getCurrentSugoiUser();
      logger.info("Check if user {} is admin", user.getName());
      return permissionService.isAdmin(user.getRoles());
    }
    logger.warn("PreAuthorize on request is disabled, can cause security problem");
    return true;
  }

  private SugoiUser getCurrentSugoiUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return new SugoiUser(
        authentication.getName(),
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(String::toUpperCase)
            .collect(Collectors.toList()));
  }
}
