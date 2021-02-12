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

import fr.insee.sugoi.converter.ouganext.Habilitations;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.model.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@Tag(name = "V1 - Gestion des habilitations")
@SecurityRequirement(name = "basic")
public class ContactHabilitationDomaineController {

  @Autowired private UserService userService;

  /**
   * Retrieve all the habilitations of a contact
   *
   * @param identifiant id of the contact
   * @param domaine
   * @return OK with the list of habilitations
   */
  @GetMapping(
      value = "/{domaine}/contact/{id}/habilitations",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastConsultant(#domaine)")
  public ResponseEntity<?> getHabilitation(
      @PathVariable("id") String identifiant, @PathVariable("domaine") String domaine) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            new Habilitations(userService.findById(domaine, null, identifiant).getHabilitations()));
  }

  /**
   * Add habiltiations without properties on an application to a contact. Create the habilitation
   * application if it does not already exist.
   *
   * @param identifiant id of the contact
   * @param domaine
   * @param appName name of the app on which the habilitation applies
   * @param nomRoles roles to add on the app to the user
   * @return NO_CONTENT if creating went well
   */
  @PutMapping(
      value = "/{domaine}/contact/{id}/habilitations/{application}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<?> addHabilitations(
      @PathVariable("id") String identifiant,
      @PathVariable("domaine") String domaine,
      @PathVariable("application") String appName,
      @RequestParam("role") List<String> nomRoles) {
    User user = userService.findById(domaine, null, identifiant);
    Habilitations habilitations = new Habilitations(user.getHabilitations());
    habilitations.addHabilitation(appName, nomRoles);
    user.setHabilitations(habilitations.convertSugoiHabilitation());
    userService.update(domaine, null, user);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  /**
   * Add habilitations of a specified role and application with one or more properties to a contact.
   * Role and application habilitation are created if they do not already exist.
   *
   * @param identifiant id of the contact
   * @param domaine
   * @param appName name of the application on which the habilitation apply
   * @param role role on which the habilitation apply
   * @param proprietes properties to add to the role of the application
   * @return NO_CONTENT if creating went well
   */
  @PutMapping(
      value = "/{domaine}/contact/{id}/habilitations/{application}/{role}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<?> addHabilitationsWithProperty(
      @PathVariable("id") String identifiant,
      @PathVariable("domaine") String domaine,
      @PathVariable("application") String appName,
      @PathVariable("role") String role,
      @RequestParam("propriete") List<String> proprietes) {
    User user = userService.findById(domaine, null, identifiant);
    Habilitations habilitations = new Habilitations(user.getHabilitations());
    habilitations.addHabilitations(appName, role, proprietes);
    user.setHabilitations(habilitations.convertSugoiHabilitation());
    userService.update(domaine, null, user);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  /**
   * Delete habilitations of a contact on an application.
   *
   * @param identifiant id of the contact
   * @param domaine
   * @param appName name of the app on which the habilitation applies
   * @param nomRoles roles to delete on the app
   * @return NO_CONTENT if deleting went well
   */
  @DeleteMapping(
      value = "/{domaine}/contact/{id}/habilitations/{application}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<?> deleteHabilitations(
      @PathVariable("id") String identifiant,
      @PathVariable("domaine") String domaine,
      @PathVariable("application") String appName,
      @RequestParam("role") List<String> nomRoles) {
    User user = userService.findById(domaine, null, identifiant);
    Habilitations habilitations = new Habilitations(user.getHabilitations());
    habilitations.removeHabilitation(appName, nomRoles);
    user.setHabilitations(habilitations.convertSugoiHabilitation());
    userService.update(domaine, null, user);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  /**
   * Delete habilitation properties of a specified role and application.
   *
   * @param identifiant id of the contact
   * @param domaine
   * @param appName name of the application on which the habilitation apply
   * @param role role on which the habilitation apply
   * @param proprietes properties to delete from the role of the application
   * @return NO_CONTENT if deleting went well
   */
  @DeleteMapping(
      value = "/{domaine}/contact/{id}/habilitations/{application}/{role}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<?> deleteHabilitationsWithProperty(
      @PathVariable("id") String identifiant,
      @PathVariable("domaine") String domaine,
      @PathVariable("application") String appName,
      @PathVariable("role") String nomRole,
      @RequestParam("propriete") List<String> proprietes) {
    User user = userService.findById(domaine, null, identifiant);
    Habilitations habilitations = new Habilitations(user.getHabilitations());
    habilitations.removeHabilitation(appName, nomRole, proprietes);
    user.setHabilitations(habilitations.convertSugoiHabilitation());
    userService.update(domaine, null, user);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
