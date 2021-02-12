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
package fr.insee.sugoi.old.services.controller;

import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.model.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@Tag(name = "V1 - Gestion des inseeRoleApplicatif")
@SecurityRequirement(name = "basic")
public class ContactInseeRoleApplicatifDomaineController {

  @Autowired private UserService userService;

  @GetMapping(
      value = "/{domaine}/contact/{id}/inseeroles",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastConsultant(#domaine)")
  public ResponseEntity<?> getInseeRoles(
      @PathVariable("id") String identifiant, @PathVariable("domaine") String domaine) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            userService
                .findById(domaine, null, identifiant)
                .getAttributes()
                .get("insee_roles_applicatifs"));
  }

  @PutMapping(
      value = "/{domaine}/contact/{id}/inseeroles/{inseerole}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<?> addInseeRoles(
      @PathVariable("id") String identifiant,
      @PathVariable("domaine") String domaine,
      @PathVariable("inseerole") String inseeRole) {
    User user = userService.findById(domaine, null, identifiant);
    if (user.getAttributes().containsKey("insee_roles_applicatifs")) {
      @SuppressWarnings("unchecked")
      List<String> userRoles = (List<String>) user.getAttributes().get("insee_roles_applicatifs");
      userRoles.add(inseeRole);
    } else {
      List<String> userRoles = new ArrayList<String>();
      userRoles.add(inseeRole);
      user.getAttributes().put("insee_roles_applicatifs", userRoles);
    }
    userService.update(domaine, null, user);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @SuppressWarnings("unchecked")
  @DeleteMapping(
      value = "/{domaine}/contact/{id}/inseeroles/{inseerole}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<?> removeInseeRole(
      @PathVariable("id") String identifiant,
      @PathVariable("domaine") String domaine,
      @PathVariable("inseerole") String inseeRole) {
    User user = userService.findById(domaine, null, identifiant);
    if (user.getAttributes().containsKey("insee_roles_applicatifs")) {
      user.getAttributes()
          .put(
              "insee_roles_applicatifs",
              ((List<String>) user.getAttributes().get("insee_roles_applicatifs"))
                  .stream()
                      .filter(role -> !role.equalsIgnoreCase(inseeRole))
                      .collect(Collectors.toList()));
    }
    userService.update(domaine, null, user);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
