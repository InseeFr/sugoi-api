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

import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.service.OrganizationService;
import fr.insee.sugoi.model.Organization;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "Manage Organization")
@SecurityRequirement(name = "oAuth")
public class OrganizationController {

  @Autowired private OrganizationService organizationService;

  @GetMapping(
      path = {"/{realm}/organizations", "/{realm}/{storage}/organizations"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastReader(#realm,#storage)")
  @Operation(summary = "Search organizations")
  public ResponseEntity<?> getOrganizations(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @RequestParam(value = "identifiant", required = false) String identifiant,
      @RequestParam(value = "size", defaultValue = "20") int size,
      @RequestParam(value = "offset", defaultValue = "0") int offset) {
    Organization filterOrganization = new Organization();
    filterOrganization.setIdentifiant(identifiant);
    PageableResult pageableResult = new PageableResult(size, offset);

    PageResult<Organization> foundOrganizations =
        organizationService.findByProperties(realm, filterOrganization, pageableResult, storage);

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

  @PostMapping(
      value = {"/{realm}/organizations", "/{realm}/{storage}/organizations"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Create a new organization")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastWriter(#realm,#storage)")
  public ResponseEntity<?> createOrganizations(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @RequestBody Organization organization) {

    if (organizationService.findById(realm, organization.getIdentifiant(), storage) == null) {
      organizationService.create(realm, organization, storage);

      URI location =
          ServletUriComponentsBuilder.fromCurrentRequest()
              .path("/" + organization.getIdentifiant())
              .build()
              .toUri();

      return ResponseEntity.created(location)
          .body(organizationService.findById(realm, organization.getIdentifiant(), storage));
    } else {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
  }

  @PutMapping(
      value = {"/{realm}/organizations/{id}", "/{realm}/{storage}/organizations/{id}"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastWriter(#realm,#storage)")
  @Operation(summary = "Update organization")
  public ResponseEntity<?> updateOrganizations(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @PathVariable("id") String id,
      @RequestBody Organization organization) {
    if (organization.getIdentifiant().equals(id)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    if (organizationService.findById(realm, id, storage) != null) {
      organizationService.update(realm, organization, storage);

      URI location =
          ServletUriComponentsBuilder.fromCurrentRequest()
              .path("/" + organization.getIdentifiant())
              .build()
              .toUri();

      return ResponseEntity.status(HttpStatus.OK)
          .header(HttpHeaders.LOCATION, location.toString())
          .body(organizationService.findById(realm, id, storage));
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @DeleteMapping(
      value = {"/{realm}/organizations/{id}", "/{realm}/{storage}/organizations/{id}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastWriter(#realm,#storage)")
  @Operation(summary = "Delete organization")
  public ResponseEntity<String> deleteOrganizations(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @PathVariable("id") String id) {

    if (organizationService.findById(realm, id, storage) != null) {
      organizationService.delete(realm, id, storage);
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @GetMapping(
      path = {"/{realm}/organizations/{name}", "/{realm}/{storage}/organizations/{name}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Get organization by identifiant")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastReader(#realm,#storage)")
  public ResponseEntity<Organization> getUserByUsername(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @PathVariable("username") String id) {
    Organization organization = organizationService.findById(realm, id, storage);
    if (organization != null) {
      return ResponseEntity.status(HttpStatus.OK).body(organization);
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}
