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
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("NewAuthorizeMethodDecider")
public class AuthorizeMethodDecider {

  private static final Logger logger = LogManager.getLogger(AuthorizeMethodDecider.class);

  @Value("${fr.insee.sugoi.api.enable.preauthorize}")
  private boolean enable;

  @Autowired PermissionService permissionService;

  public boolean isReader(String realm, String userStorage) {
    if (enable) {
      logger.info("Check if user is reader on realm {} and userStorage {}", realm, userStorage);
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      List<String> roles =
          authentication.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .map(String::toUpperCase)
              .collect(Collectors.toList());
      SugoiUser sugoiUser = new SugoiUser(authentication.getName(), roles);
      return permissionService.isReader(sugoiUser, realm, userStorage)
          || permissionService.isWriter(sugoiUser, realm, userStorage)
          || permissionService.isApplicationManager(sugoiUser, realm)
          || permissionService.isPasswordManager(sugoiUser, realm, userStorage);
    }
    logger.warn("PreAuthorize on request is disabled, can cause security problem");
    return true;
  }

  public boolean isAppManager(String realm, String application) {
    if (enable) {
      logger.info("Check if user is at least reader on realm {}", realm);
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      List<String> roles =
          authentication.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .map(String::toUpperCase)
              .collect(Collectors.toList());
      SugoiUser sugoiUser = new SugoiUser(authentication.getName(), roles);
      return permissionService.isApplicationManager(sugoiUser, realm, null, application)
          || permissionService.isWriter(sugoiUser, realm, null);
    }
    logger.warn("PreAuthorize on request is disabled, can cause security problem");
    return true;
  }

  public boolean isPasswordManager(String realm, String userStorage) {
    if (enable) {
      logger.info(
          "Check if user is at least reader on realm {} and userStorage {}", realm, userStorage);
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      List<String> roles =
          authentication.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .map(String::toUpperCase)
              .collect(Collectors.toList());
      SugoiUser sugoiUser = new SugoiUser(authentication.getName(), roles);
      return permissionService.isPasswordManager(sugoiUser, realm, userStorage)
          || permissionService.isWriter(sugoiUser, realm, userStorage);
    }
    logger.warn("PreAuthorize on request is disabled, can cause security problem");
    return true;
  }

  public boolean isWriter(String realm, String userStorage) {
    if (enable) {
      logger.info(
          "Check if user is at least writer on realm {} and userStorage {}", realm, userStorage);
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      List<String> roles =
          authentication.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .map(String::toUpperCase)
              .collect(Collectors.toList());
      SugoiUser sugoiUser = new SugoiUser(authentication.getName(), roles);
      return permissionService.isWriter(sugoiUser, realm, userStorage);
    }
    logger.warn("PreAuthorize on request is disabled, can cause security problem");
    return true;
  }

  public boolean isAdmin() {
    if (enable) {
      logger.info("Check if user is admin");
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      List<String> roles =
          authentication.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .map(String::toUpperCase)
              .collect(Collectors.toList());
      SugoiUser sugoiUser = new SugoiUser(authentication.getName(), roles);
      return permissionService.isAdmin(sugoiUser);
    }
    logger.warn("PreAuthorize on request is disabled, can cause security problem");
    return true;
  }
}
