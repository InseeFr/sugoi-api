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

import fr.insee.sugoi.model.Habilitation;
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
@Tag(name = "Manage habilitations")
@RequestMapping(value = {"/v2", "/"})
@SecurityRequirement(name = "oAuth")
public class HabilitationController {

  @GetMapping(
      path = {"/realm/{realm}/habilitations", "/realm/{realm}/storage/{storage}/habilitations"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@NewAuthorizeMethodDecider.isReader(#realm,#storage)")
  public ResponseEntity<?> getHabilitations(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @RequestParam(value = "application", required = false) String application,
      @RequestParam(value = "role", required = false) String role,
      @RequestParam(value = "property", required = false) String property) {
    // TODO: process GET request

    return null;
  }

  @PostMapping(
      value = {"/realm/{realm}/habilitations", "/realm/{realm}/storage/{storage}/habilitations"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@NewAuthorizeMethodDecider.isWriter(#realm,#storage)")
  public ResponseEntity<?> createHabilitations(
      @PathVariable("realm") String realm,
      @PathVariable("storage") String storage,
      @RequestBody Habilitation habilitation) {
    // TODO: process POST request

    return new ResponseEntity<Habilitation>(habilitation, HttpStatus.CREATED);
  }

  @PutMapping(
      value = {
        "/realm/{realm}/habilitations/{id}",
        "/realm/{realm}/storage/{storage}/habilitations/{id}"
      },
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@NewAuthorizeMethodDecider.isWriter(#realm,#storage)")
  public ResponseEntity<?> updateHabilitations(
      @PathVariable("realm") String realm,
      @PathVariable("storage") String storage,
      @PathVariable("id") String id,
      @RequestBody Habilitation habilitation) {
    // TODO: process PUT request

    return new ResponseEntity<>(habilitation, HttpStatus.OK);
  }

  @DeleteMapping(
      value = {
        "/realm/{realm}/habilitations/{id}",
        "/realm/{realm}/storage/{storage}/habilitations/{id}"
      },
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@NewAuthorizeMethodDecider.isWriter(#realm,#storage)")
  public ResponseEntity<String> deleteHabilitations(
      @PathVariable("realm") String realm,
      @PathVariable("storage") String storage,
      @PathVariable("id") String id) {
    // TODO: process DELETE request

    return new ResponseEntity<String>(id, HttpStatus.OK);
  }
}
