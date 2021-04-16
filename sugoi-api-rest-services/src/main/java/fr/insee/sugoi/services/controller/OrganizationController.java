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

import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.core.exceptions.OrganizationNotFoundException;
import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.model.SearchType;
import fr.insee.sugoi.core.service.OrganizationService;
import fr.insee.sugoi.model.Organization;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    name = "Manage Organization",
    description = "New endpoints to create, update, delete and find organizations")
@SecurityRequirements(
    value = {@SecurityRequirement(name = "oAuth"), @SecurityRequirement(name = "basic")})
public class OrganizationController {

  @Autowired private OrganizationService organizationService;

  @GetMapping(
      path = {"/realms/{realm}/storages/{storage}/organizations"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@NewAuthorizeMethodDecider.isReader(#realm,#storage)")
  @Operation(summary = "Search organizations by parameters")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Organizations found according to parameter",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PageResult.class))
            })
      })
  public ResponseEntity<?> getOrganizations(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(
              description = "Name of the userStorage where the operation will be made",
              required = false)
          @PathVariable(name = "storage", required = false)
          String storage,
      @Parameter(
              description = "Organization's identifiant of the wanted organization",
              required = false)
          @RequestParam(value = "identifiant", required = false)
          String identifiant,
      @Parameter(description = "Expected size of result", required = false)
          @RequestParam(value = "size", defaultValue = "20")
          int size,
      @Parameter(description = "Offset to apply when searching", required = false)
          @RequestParam(value = "offset", defaultValue = "0")
          int offset,
      @Parameter(description = "Default search can be AND or OR", required = true)
          @RequestParam(name = "typeRecherche", defaultValue = "AND", required = true)
          SearchType typeRecherche) {
    Organization filterOrganization = new Organization();
    filterOrganization.setIdentifiant(identifiant);
    PageableResult pageableResult = new PageableResult(size, offset);

    PageResult<Organization> foundOrganizations =
        organizationService.findByProperties(
            realm, storage, filterOrganization, pageableResult, typeRecherche);

    if (foundOrganizations.isHasMoreResult()) {
      URI location =
          ServletUriComponentsBuilder.fromCurrentRequest()
              .replaceQueryParam("offset", offset + size)
              .build()
              .toUri();
      return ResponseEntity.status(HttpStatus.OK)
          .header(HttpHeaders.LOCATION, location.toString())
          .body(foundOrganizations);
    } else {
      return ResponseEntity.status(HttpStatus.OK).body(foundOrganizations);
    }
  }

  @GetMapping(
      path = {"/realms/{realm}/organizations"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@NewAuthorizeMethodDecider.isReader(#realm,#storage)")
  @Operation(summary = "Search organizations by parameters")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Organizations found according to parameter",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PageResult.class))
            })
      })
  public ResponseEntity<?> getOrganizations(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(
              description = "Organization's identifiant of the wanted organization",
              required = false)
          @RequestParam(value = "identifiant", required = false)
          String identifiant,
      @Parameter(description = "Expected size of result", required = false)
          @RequestParam(value = "size", defaultValue = "20")
          int size,
      @Parameter(description = "Offset to apply when searching", required = false)
          @RequestParam(value = "offset", defaultValue = "0")
          int offset,
      @Parameter(description = "Default search can be AND or OR", required = true)
          @RequestParam(name = "typeRecherche", defaultValue = "AND", required = true)
          SearchType typeRecherche) {
    return getOrganizations(realm, null, identifiant, size, offset, typeRecherche);
  }

  @PostMapping(
      value = {"/realms/{realm}/storages/{storage}/organizations"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Create a new organization")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Organization created",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Organization.class))
            }),
        @ApiResponse(
            responseCode = "409",
            description = "Organization already exist",
            content = {@Content(mediaType = "application/json")})
      })
  @PreAuthorize("@NewAuthorizeMethodDecider.isWriter(#realm,#storage)")
  public ResponseEntity<Organization> createOrganizations(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(
              description = "Name of the userStorage where the operation will be made",
              required = false)
          @PathVariable(name = "storage", required = false)
          String storage,
      @Parameter(description = "Organization to create", required = false) @RequestBody
          Organization organization) {

    Organization orgCreated = organizationService.create(realm, storage, organization);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/" + orgCreated.getIdentifiant())
            .build()
            .toUri();
    return ResponseEntity.created(location).body(orgCreated);
  }

  @PutMapping(
      value = {"/realms/{realm}/storages/{storage}/organizations/{id}"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@NewAuthorizeMethodDecider.isWriter(#realm,#storage)")
  @Operation(summary = "Update organization")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Organization updated",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Organization.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "Organization does'nt exist",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(
            responseCode = "400",
            description = "id in path and body are not equals",
            content = {@Content(mediaType = "application/json")})
      })
  public ResponseEntity<?> updateOrganizations(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(
              description = "Name of the userStorage where the operation will be made",
              required = false)
          @PathVariable(name = "storage", required = false)
          String storage,
      @Parameter(description = "Organization's id to update", required = false) @PathVariable("id")
          String id,
      @Parameter(description = "Organization to update", required = false) @RequestBody
          Organization organization) {
    if (!organization.getIdentifiant().equals(id)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    organizationService.update(realm, storage, organization);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
    return ResponseEntity.status(HttpStatus.OK)
        .header(HttpHeaders.LOCATION, location.toString())
        .body(organizationService.findById(realm, storage, id).get());
  }

  @PutMapping(
      value = {"/realms/{realm}/organizations/{id}"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@NewAuthorizeMethodDecider.isWriter(#realm,#storage)")
  @Operation(summary = "Update organization")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Organization updated",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Organization.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "Organization does'nt exist",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(
            responseCode = "400",
            description = "id in path and body are not equals",
            content = {@Content(mediaType = "application/json")})
      })
  public ResponseEntity<?> updateOrganizations(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(description = "Organization's id to update", required = false) @PathVariable("id")
          String id,
      @Parameter(description = "Organization to update", required = false) @RequestBody
          Organization organization) {
    if (!organization.getIdentifiant().equals(id)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    Organization org =
        organizationService
            .findById(realm, null, id)
            .orElseThrow(
                () ->
                    new OrganizationNotFoundException(
                        "Cannot find organization "
                            + organization.getIdentifiant()
                            + " in realm "
                            + realm));
    return updateOrganizations(
        realm, (String) org.getMetadatas().get(GlobalKeysConfig.USERSTORAGE), id, organization);
  }

  @DeleteMapping(
      value = {"/realms/{realm}/storages/{storage}/organizations/{id}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@NewAuthorizeMethodDecider.isWriter(#realm,#storage)")
  @Operation(summary = "Delete organization")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "Organization deleted",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(
            responseCode = "404",
            description = "Organization does'nt exist",
            content = {@Content(mediaType = "application/json")})
      })
  public ResponseEntity<String> deleteOrganizations(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(
              description = "Name of the userStorage where the operation will be made",
              required = false)
          @PathVariable(name = "storage", required = false)
          String storage,
      @Parameter(description = "Organization's id to delete", required = false) @PathVariable("id")
          String id) {
    organizationService.delete(realm, storage, id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @DeleteMapping(
      value = {"/realms/{realm}/organizations/{id}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@NewAuthorizeMethodDecider.isWriter(#realm,#storage)")
  @Operation(summary = "Delete organization")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "Organization deleted",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(
            responseCode = "404",
            description = "Organization does'nt exist",
            content = {@Content(mediaType = "application/json")})
      })
  public ResponseEntity<String> deleteOrganizations(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(description = "Organization's id to delete", required = false) @PathVariable("id")
          String id) {

    Organization org =
        organizationService
            .findById(realm, null, id)
            .orElseThrow(
                () ->
                    new OrganizationNotFoundException(
                        "Cannot find organization " + id + " in realm " + realm));
    return deleteOrganizations(
        realm, (String) org.getMetadatas().get(GlobalKeysConfig.USERSTORAGE), id);
  }

  @GetMapping(
      path = {"/realms/{realm}/storages/{storage}/organizations/{orgId}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Get organization by identifiant")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Organizations found according to params",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(
            responseCode = "404",
            description = "No organization match name",
            content = {@Content(mediaType = "application/json")})
      })
  @PreAuthorize("@NewAuthorizeMethodDecider.isReader(#realm,#storage)")
  public ResponseEntity<Organization> getOrganizationById(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(
              description = "Name of the userStorage where the operation will be made",
              required = false)
          @PathVariable(name = "storage", required = false)
          String storage,
      @Parameter(description = "Organization's id to search", required = false)
          @PathVariable("orgId")
          String id) {
    Organization organization =
        organizationService
            .findById(realm, storage, id)
            .orElseThrow(
                () ->
                    new OrganizationNotFoundException(
                        "Cannot find organization "
                            + id
                            + (storage != null ? " in userStorage " + storage : "")
                            + " in realm "
                            + realm));
    return ResponseEntity.status(HttpStatus.OK).body(organization);
  }

  @GetMapping(
      path = {"/realms/{realm}/organizations/{orgId}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Get organization by identifiant")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Organizations found according to params",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(
            responseCode = "404",
            description = "No organization match name",
            content = {@Content(mediaType = "application/json")})
      })
  @PreAuthorize("@NewAuthorizeMethodDecider.isReader(#realm,#storage)")
  public ResponseEntity<Organization> getOrganizationById(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(description = "Organization's id to search", required = false)
          @PathVariable("orgId")
          String id) {
    return getOrganizationById(realm, null, id);
  }
}
