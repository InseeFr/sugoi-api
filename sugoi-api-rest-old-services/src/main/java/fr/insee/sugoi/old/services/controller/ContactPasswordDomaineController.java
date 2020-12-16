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

import fr.insee.sugoi.converter.ouganext.PasswordChangeRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@Tag(name = "V1 - Gestion du mot de passe")
@SecurityRequirement(name = "basic")
public class ContactPasswordDomaineController {

  @PostMapping(
      value = "/{domaine}/contact/{id}/password",
      consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<?> reinitPassword(
      @PathVariable("id") String identifiant,
      @PathVariable("domaine") String domaine,
      @RequestParam("modeEnvoi") List<String> modeEnvoisString,
      @RequestBody PasswordChangeRequest pcr) {
    return null;
  }

  @PostMapping(
      value = "/{domaine}/contact/{id}/password/first",
      consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<?> initPassword(
      @PathVariable("id") String identifiant,
      @PathVariable("domaine") String domaine,
      @RequestParam("modeEnvoi") List<String> modeEnvoisString,
      @RequestBody PasswordChangeRequest pcr) {
    return null;
  }

  @PutMapping(
      value = "/{domaine}/contact/{id}/password",
      consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<?> changePassword(
      @PathVariable("id") String identifiant,
      @PathVariable("domaine") String domaine,
      @RequestParam("modeEnvoi") List<String> modeEnvoisString,
      @RequestBody PasswordChangeRequest pcr) {
    return null;
  }
}
