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
package fr.insee.sugoi.services.controller;

import fr.insee.sugoi.core.exceptions.RealmNotFoundException;
import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.services.decider.AuthorizeMethodDecider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(value = {"/v2", "/"})
@Tag(
    name = "Manage Realms and storages",
    description = "New Endpoints to create, update, delete and find realms")
@SecurityRequirements(
    value = {@SecurityRequirement(name = "oAuth"), @SecurityRequirement(name = "basic")})
public class RealmController {

  @Autowired ConfigService configService;

  @Autowired
  @Qualifier("NewAuthorizeMethodDecider")
  AuthorizeMethodDecider authorizeService;

  @GetMapping(value = "/realms")
  @Operation(summary = "Get realms where you have rights")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "realm found",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Realm[].class))
            })
      })
  public ResponseEntity<List<Realm>> getRealms(
      @Parameter(description = "Id of the realm to search", required = false)
          @RequestParam(name = "id", required = false)
          String id,
      Authentication authentication) {
    List<Realm> realms = new ArrayList<>();
    List<Realm> realmsFiltered = new ArrayList<>();

    if (id != null) {
      Realm retrievedRealm =
          configService
              .getRealm(id)
              .orElseThrow(() -> new RealmNotFoundException("Realm " + id + " not found"));
      if (retrievedRealm != null) {
        realms.add(retrievedRealm);
      }
    } else {
      realms.addAll(configService.getRealms());
    }

    // Filter realm before sending if user not admin
    for (Realm realm : realms) {
      if (realm.getUserStorages().stream()
              .filter(us -> authorizeService.isReader(realm.getName(), us.getName()))
              .count()
          > 0) realmsFiltered.add(realm);
    }
    return new ResponseEntity<List<Realm>>(realmsFiltered, HttpStatus.OK);
  }

  @PostMapping(
      value = "/realms",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Create realm")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAdmin()")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "409",
            description = "Realm already exist",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(
            responseCode = "201",
            description = "Realm created",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Realm.class))
            })
      })
  public ResponseEntity<Realm> createRealm(@RequestBody Realm realm) {
    if (configService.getRealm(realm.getName()) != null) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
    configService.createRealm(realm);
    return ResponseEntity.created(createRealmURI(realm.getName()))
        .body(
            configService
                .getRealm(realm.getName())
                .orElseThrow(() -> new RealmNotFoundException("Cannot found realm " + realm)));
  }

  @PutMapping(
      value = "/realms/{id}",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@NewAuthorizeMethodDecider.isAdmin()")
  @Operation(summary = "Update realm")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(
            responseCode = "404",
            description = "Realm not found",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(
            responseCode = "200",
            description = "Realm updated",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Realm.class))
            })
      })
  public ResponseEntity<Realm> updateRealm(
      @RequestBody Realm realm, @PathVariable("id") String id) {
    if (!realm.getName().equalsIgnoreCase(id)) {
      return ResponseEntity.badRequest().build();
    }
    configService.updateRealm(realm);
    return ResponseEntity.ok()
        .location(createRealmURI(realm.getName()))
        .body(configService.getRealm(realm.getName()).get());
  }

  @DeleteMapping(
      value = "/realms/{id}",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Delete Realm")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAdmin()")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "404",
            description = "Realm not found",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(
            responseCode = "204",
            description = "Realm deleted",
            content = {@Content(mediaType = "application/json")})
      })
  public ResponseEntity<String> deleteRealm(@PathVariable("id") String id) {
    if (configService.getRealm(id) == null) {
      return ResponseEntity.notFound().build();
    }
    configService.deleteRealm(id);
    return ResponseEntity.noContent().build();
  }

  private URI createRealmURI(String realmName) {
    return ServletUriComponentsBuilder.fromCurrentRequest().path("/" + realmName).build().toUri();
  }
}
