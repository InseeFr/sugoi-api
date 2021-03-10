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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(
    name = "[Deprecated] - Gestion des inseeRoleApplicatif",
    description = "Old Enpoints to manage inseeRoleApplicatif of contacts")
@SecurityRequirement(name = "basic")
public class ContactInseeRoleApplicatifDomaineController {

  @Autowired private UserService userService;

  /**
   * Get inseeroleapplicatif for a contact
   *
   * @param identifiant
   * @param domaine
   * @return Ok with the list of insee_roles_applicatifs
   */
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastConsultant(#domaine)")
  @GetMapping(
      value = "/{domaine}/contact/{id}/inseeroles",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Get inseeroleapplicatif for a contact", deprecated = true)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Contact successfully updated or created",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = String[].class)),
              @Content(
                  mediaType = "application/xml",
                  schema = @Schema(implementation = String[].class))
            })
      })
  public ResponseEntity<Object> getInseeRoles(
      @Parameter(description = "Contact to look for inseeRoleApplicatifs", required = true)
          @PathVariable(name = "id", required = true)
          String identifiant,
      @Parameter(
              description = "Name of the domaine where the operation will be made",
              required = true)
          @PathVariable(name = "domaine", required = true)
          String domaine) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            userService
                .findById(domaine, null, identifiant)
                .getAttributes()
                .get("insee_roles_applicatifs"));
  }

  /**
   * Add inseeroleapplicatif to a contact
   *
   * @param identifiant
   * @param domaine
   * @param inseeRole
   * @return NO_CONTENT
   */
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  @PutMapping(
      value = "/{domaine}/contact/{id}/inseeroles/{inseerole}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Add inseeroleapplicatif to a contact", deprecated = true)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "inseeroleapplicatif successfully added to contact",
            content = {
              @Content(mediaType = "application/json"),
              @Content(mediaType = "application/xml")
            })
      })
  public ResponseEntity<Object> addInseeRoles(
      @Parameter(description = "Contact's id to update", required = true)
          @PathVariable(name = "id", required = true)
          String identifiant,
      @Parameter(
              description = "Name of the domaine where the operation will be made",
              required = true)
          @PathVariable(name = "domaine", required = true)
          String domaine,
      @Parameter(description = "Name of the inseeRoleApplicatif to add", required = true)
          @PathVariable(name = "inseerole", required = true)
          String inseeRole) {
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

  /**
   * Delete inseeRoleApplicatif from a contact
   *
   * @param identifiant
   * @param domaine
   * @param inseeRole
   * @return NO_CONTENT
   */
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  @DeleteMapping(
      value = "/{domaine}/contact/{id}/inseeroles/{inseerole}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Delete inseeroleapplicatif from a contact", deprecated = true)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "inseeroleapplicatif successfully deleted from contact",
            content = {
              @Content(mediaType = "application/json"),
              @Content(mediaType = "application/xml")
            })
      })
  @SuppressWarnings("unchecked")
  public ResponseEntity<?> removeInseeRole(
      @Parameter(description = "Contact's id to update", required = true)
          @PathVariable(name = "id", required = true)
          String identifiant,
      @Parameter(
              description = "Name of the domaine where the operation will be made",
              required = true)
          @PathVariable(name = "domaine", required = true)
          String domaine,
      @Parameter(description = "Name of the inseeRoleApplicatif to delete", required = true)
          @PathVariable(name = "inseerole", required = true)
          String inseeRole) {
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
