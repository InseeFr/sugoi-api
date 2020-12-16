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

import fr.insee.sugoi.model.Application;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@Tag(name = "Manage applications")
@RequestMapping(value = { "/v2", "/" })
@SecurityRequirement(name = "oAuth")
public class ApplicationController {

  @GetMapping(path = { "/{realm}/applications", "/{realm}/{storage}/applications" }, produces = {
      MediaType.APPLICATION_JSON_VALUE })
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastReader(#realm,#storage)")
  public ResponseEntity<?> getApplications(@PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @RequestParam(value = "name", required = false) String name,
      @RequestParam(value = "owner", required = false) String owner) {
    // TODO: process GET request

    return null;
  }

  @PostMapping(value = { "/{realm}/applications", "/{realm}/{storage}/applications" }, consumes = {
      MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastWriter(#realm,#storage)")
  public ResponseEntity<?> createApplication(@PathVariable("realm") String realm,
      @PathVariable("storage") String storage, @RequestBody Application application) {
    // TODO: process POST request

    return new ResponseEntity<Application>(application, HttpStatus.CREATED);
  }

  @PutMapping(value = { "/{realm}/applications/{id}", "/{realm}/{storage}/applications/{id}" }, consumes = {
      MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastWriter(#realm,#storage)")
  public ResponseEntity<?> updateApplication(@PathVariable("realm") String realm,
      @PathVariable("storage") String storage, @PathVariable("id") String id, @RequestBody Application application) {
    // TODO: process PUT request

    return new ResponseEntity<>(application, HttpStatus.OK);
  }

  @DeleteMapping(value = { "/{realm}/applications/{id}", "/{realm}/{storage}/applications/{id}" }, produces = {
      MediaType.APPLICATION_JSON_VALUE })
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastWriter(#realm,#storage)")
  public ResponseEntity<String> deleteApplication(@PathVariable("realm") String realm,
      @PathVariable("storage") String storage, @PathVariable("id") String id) {
    // TODO: process DELETE request

    return new ResponseEntity<String>(id, HttpStatus.OK);
  }
}
