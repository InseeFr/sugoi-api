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
import fr.insee.sugoi.core.service.ApplicationService;
import fr.insee.sugoi.model.Application;
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
@Tag(name = "Manage applications")
@RequestMapping(value = {"/v2", "/"})
@SecurityRequirement(name = "oAuth")
public class ApplicationController {

  @Autowired private ApplicationService applicationService;

  @GetMapping(
      path = {"/{realm}/applications", "/{realm}/{storage}/applications"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Search applications")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastReader(#realm,#storage)")
  public ResponseEntity<PageResult<Application>> getApplications(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @RequestParam(name = "size", defaultValue = "20") int size,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(value = "name", required = false) String name,
      @RequestParam(value = "owner", required = false) String owner) {
    Application applicationFilter = new Application();
    applicationFilter.setName(name);
    applicationFilter.setOwner(owner);

    PageableResult pageableResult = new PageableResult(size, offset);

    PageResult<Application> foundApplications =
        applicationService.findByProperties(realm, storage, applicationFilter, pageableResult);

    if (foundApplications.isHasMoreResult()) {
      URI location =
          ServletUriComponentsBuilder.fromCurrentRequest()
              .replaceQueryParam("offset", offset + size)
              .build()
              .toUri();
      return ResponseEntity.status(HttpStatus.OK)
          .header(HttpHeaders.LOCATION, location.toString())
          .body(foundApplications);
    } else {
      return ResponseEntity.status(HttpStatus.OK).body(foundApplications);
    }
  }

  @PostMapping(
      value = {"/{realm}/applications", "/{realm}/{storage}/applications"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Create application")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastWriter(#realm,#storage)")
  public ResponseEntity<Application> createApplication(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @RequestBody Application application) {

    if (applicationService.findById(realm, storage, application.getName()) == null) {
      applicationService.create(realm, storage, application);

      URI location =
          ServletUriComponentsBuilder.fromCurrentRequest()
              .path("/" + application.getName())
              .build()
              .toUri();

      return ResponseEntity.created(location)
          .body(applicationService.findById(realm, storage, application.getName()));
    } else {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
  }

  @PutMapping(
      value = {"/{realm}/applications/{id}", "/{realm}/{storage}/applications/{id}"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Update application")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastWriter(#realm,#storage)")
  public ResponseEntity<Application> updateApplication(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @PathVariable("id") String id,
      @RequestBody Application application) {

    if (!application.getName().equals(id)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    if (applicationService.findById(realm, storage, id) != null) {
      applicationService.update(realm, storage, application);

      URI location =
          ServletUriComponentsBuilder.fromCurrentRequest()
              .path("/" + application.getName())
              .build()
              .toUri();

      return ResponseEntity.status(HttpStatus.OK)
          .header(HttpHeaders.LOCATION, location.toString())
          .body(applicationService.findById(realm, storage, id));
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @DeleteMapping(
      value = {"/{realm}/applications/{id}", "/{realm}/{storage}/applications/{id}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Delete application")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastWriter(#realm,#storage)")
  public ResponseEntity<String> deleteApplication(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @PathVariable("id") String id) {

    if (applicationService.findById(realm, storage, id) != null) {
      applicationService.delete(realm, storage, id);
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @GetMapping(
      path = {"/{realm}/applications/{name}", "/{realm}/{storage}/applications/{name}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Get application by name")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastReader(#realm,#storage)")
  public ResponseEntity<Application> getApplicationByName(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @PathVariable("name") String name) {
    Application application = applicationService.findById(realm, storage, name);
    if (application != null) {
      return ResponseEntity.status(HttpStatus.OK).body(application);
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}
