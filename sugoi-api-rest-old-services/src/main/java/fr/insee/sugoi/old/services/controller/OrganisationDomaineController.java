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

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.sugoi.converter.ouganext.Organisation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/v1")
@SecurityRequirement(name = "basic")
public class OrganisationDomaineController {

  @PutMapping(value = "/{domaine}/organisation/{id}", consumes = { MediaType.APPLICATION_XML_VALUE,
      MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_XML_VALUE,
          MediaType.APPLICATION_JSON_VALUE })
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<?> createOrModifyOrganisation(@PathVariable("domaine") String domaine,
      @PathVariable("id") String id, @RequestParam("creation") boolean creation,
      @RequestBody Organisation organisation) {
    // TODO: process PUT request

    return null;
  }

  @GetMapping(value = "/{domaine}/organisation/{id}", produces = { MediaType.APPLICATION_XML_VALUE,
      MediaType.APPLICATION_JSON_VALUE })
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastConsultant(#domaine)")
  public ResponseEntity<?> getOrganisationDomaine(@PathVariable("domaine") String domaine,
      @PathVariable("id") String id) {
    return null;
  }

  @DeleteMapping(value = "/{domaine}/organisation/{id}", produces = { MediaType.APPLICATION_XML_VALUE,
      MediaType.APPLICATION_JSON_VALUE })
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<?> deleteOrganisation(@PathVariable("domaine") String domaine, @PathVariable("id") String id) {
    return null;
  }
}
