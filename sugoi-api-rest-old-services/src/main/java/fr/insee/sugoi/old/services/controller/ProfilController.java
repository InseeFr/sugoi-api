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

import fr.insee.sugoi.converter.ouganext.Profil;
import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.old.services.utils.ResponseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/profil")
@Tag(name = "[Deprecated] - Manage profile")
@SecurityRequirement(name = "basic")
public class ProfilController {

  @Autowired private ConfigService configService;

  /**
   * Get a profile by its name
   *
   * @param nom
   * @return OK with the wanted profile
   */
  @PreAuthorize("@OldAuthorizeMethodDecider.isAdmin()")
  @GetMapping(
      value = "/{nom}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Get a profile by its name", deprecated = true)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Profile found",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Profil.class)),
              @Content(
                  mediaType = "application/xml",
                  schema = @Schema(implementation = Profil.class))
            }),
      })
  public ResponseEntity<?> getProfil(
      @Parameter(description = "Name of the profil to search", required = true)
          @PathVariable(name = "nom", required = true)
          String nom) {
    Realm realm = configService.getRealm(nom);
    Profil profil = ResponseUtils.convertRealmToProfils(realm).get(0);
    return new ResponseEntity<>(profil, HttpStatus.OK);
  }

  /**
   * Update a profile
   *
   * @param profil
   * @return null
   */
  @PreAuthorize("@OldAuthorizeMethodDecider.isAdmin()")
  @PutMapping(
      value = "/",
      consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Update a profile", deprecated = true)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Profile successfully created or updated",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Profil.class)),
              @Content(
                  mediaType = "application/xml",
                  schema = @Schema(implementation = Profil.class))
            })
      })
  public ResponseEntity<?> createOrModifyProfil(
      @Parameter(description = "The profil to update/create", required = true) @RequestBody
          Profil profil) {
    return null;
  }

  /**
   * Delete a profil by its name
   *
   * @param nom
   * @return null
   */
  @PreAuthorize("@OldAuthorizeMethodDecider.isAdmin()")
  @DeleteMapping(
      value = "/nom",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Delete a profile", deprecated = true)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Profile successfully deleted",
            content = {
              @Content(mediaType = "application/json"),
              @Content(mediaType = "application/xml")
            })
      })
  public ResponseEntity<?> deleteProfil(
      @Parameter(description = "Name of the profil to delete", required = true) @PathVariable("nom")
          String nom) {
    return null;
  }
}
