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

import fr.insee.sugoi.converter.ouganext.Profils;
import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.old.services.utils.ResponseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/profils")
@Tag(name = "[Deprecated] - Manage profiles", description = "Old Enpoints to manage profiles")
@SecurityRequirement(name = "basic")
public class ProfilsController {

  @Autowired
  private ConfigService configService;

  /**
   * Get all profiles
   *
   * @return Ok with a list of all available profiles
   */
  @PreAuthorize("@OldAuthorizeMethodDecider.isAdmin()")
  @GetMapping(value = "/", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
  @Operation(summary = "Get all profiles", deprecated = true)
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Profile found", content = {
      @Content(mediaType = "application/json", schema = @Schema(implementation = Profils.class)),
      @Content(mediaType = "application/xml", schema = @Schema(implementation = Profils.class)) }), })
  public ResponseEntity<?> getProfils() {
    List<Realm> realms = configService.getRealms();
    Profils profils = new Profils();
    profils.getListe().addAll(realms.stream().map(realm -> ResponseUtils.convertRealmToProfils(realm))
        .flatMap(List::stream).collect(Collectors.toList()));
    return new ResponseEntity<>(profils, HttpStatus.OK);
  }
}
