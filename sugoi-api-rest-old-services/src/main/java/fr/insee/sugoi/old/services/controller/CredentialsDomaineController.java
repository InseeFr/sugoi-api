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

import fr.insee.sugoi.core.service.CredentialsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "V1 - Credentials")
@RestController
@RequestMapping("/v1")
@SecurityRequirement(name = "basic")
public class CredentialsDomaineController {

  @Autowired CredentialsService credentialsService;
  /**
   * Validate credentials of a contact
   *
   * @param domaine
   * @param id id of the contact
   * @param motDePasse credential to test
   * @return 200 if credential do match, 403 if not
   */
  @PostMapping(
      value = "/{domaine}/credentials/{id}",
      consumes = {MediaType.TEXT_PLAIN_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<?> authentifierContact(
      @PathVariable("domaine") String domaine,
      @PathVariable("id") String id,
      @RequestBody String motDePasse) {
    if (credentialsService.validateCredential(domaine, null, id, motDePasse)) {
      return ResponseEntity.ok().build();
    } else {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
  }
}
