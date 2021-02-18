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

import fr.insee.sugoi.converter.mapper.OuganextSugoiMapper;
import fr.insee.sugoi.converter.ouganext.Groupe;
import fr.insee.sugoi.core.service.GroupService;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.stream.Collectors;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@Tag(name = "V1 - Gestion des groupes")
@SecurityRequirement(name = "basic")
public class ContactGroupeDomaineController {

  @Autowired private UserService userService;
  @Autowired private GroupService groupService;

  @Autowired private OuganextSugoiMapper ouganextSugoiMapper;

  @GetMapping(
      value = "/{domaine}/contact/{id}/groupes",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastConsultant(#domaine)")
  public ResponseEntity<?> getGroups(
      @PathVariable("id") String identifiant, @PathVariable("domaine") String domaine) {
    User user = userService.findById(domaine, null, identifiant);
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            user.getGroups().stream()
                .map(group -> ouganextSugoiMapper.serializeToOuganext(group, Groupe.class))
                .collect(Collectors.toList()));
  }

  /**
   * Add a contact in an application group
   *
   * @param identifiant id of the contact to add to the group
   * @param domaine
   * @param nomAppli name of the application of the group
   * @param nomGroupe group name
   * @return NO_CONTENT if contact was added to the group, CONFLICT if user already in group
   */
  @PutMapping(
      value = "/{domaine}/contact/{id}/groupes/{nomappli}/{nomgroupe}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<?> addToGroup(
      @PathVariable("id") String identifiant,
      @PathVariable("domaine") String domaine,
      @PathVariable("nomappli") String nomAppli,
      @PathVariable("nomgroupe") String nomGroupe) {
    Group group = groupService.findById(domaine, null, nomAppli, nomGroupe);
    if (group != null) {
      User user = userService.findById(domaine, null, identifiant);
      if (user != null) {
        if (user.getGroups() != null
            && user.getGroups().stream()
                .anyMatch(g -> g.getName().equals(nomGroupe) && g.getAppName().equals(nomAppli))) {
          return new ResponseEntity<>("Contact already in group", HttpStatus.CONFLICT);
        } else {
          userService.addUserToGroup(domaine, null, identifiant, nomAppli, nomGroupe);
          return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
      } else {
        return new ResponseEntity<>("Contact non trouvé", HttpStatus.NOT_FOUND);
      }
    } else {
      return new ResponseEntity<>("Groupe non trouvé", HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Delete a user from a group
   *
   * @param identifiant user to delete from the group
   * @param domaine
   * @param nomAppli name of the application of the group
   * @param nomGroupe name of the group in which the user will be added
   * @return NO_CONTENT if deletion occured, CONFLICT if user doesn't not belong to the group
   */
  @DeleteMapping(
      value = "{domaine}/contact/{id}/groupes/{nomappli}/{nomgroupe}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<?> removeFromGroups(
      @PathVariable("id") String identifiant,
      @PathVariable("domaine") String domaine,
      @PathVariable("nomappli") String nomAppli,
      @PathVariable("nomgroupe") String nomGroupe) {
    User user = userService.findById(domaine, null, identifiant);
    if (user != null) {
      if (user.getGroups() != null
          && user.getGroups().stream()
              .anyMatch(
                  group ->
                      group.getAppName().equals(nomAppli) && group.getName().equals(nomGroupe))) {
        userService.deleteUserFromGroup(domaine, null, identifiant, nomAppli, nomGroupe);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      } else {
        return new ResponseEntity<>("Contact doesn't belong to group", HttpStatus.CONFLICT);
      }
    } else {
      return new ResponseEntity<>("Contact not found", HttpStatus.NOT_FOUND);
    }
  }
}
