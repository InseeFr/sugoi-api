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
import fr.insee.sugoi.converter.ouganext.Contact;
import fr.insee.sugoi.converter.ouganext.InfoFormattage;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@Tag(name = "V1 - Gestion des contacts")
@SecurityRequirement(name = "basic")
public class ContactDomaineController {

  @Autowired private UserService userService;

  @Autowired private OuganextSugoiMapper ouganextSugoiMapper;

  /**
   * Update or create a contact.
   *
   * @param domaine realm of the request
   * @param id id of the contact to update, will replace the id of the contact body parameter
   * @param creation if true a non existent contact to update is created
   * @param contact
   * @return OK with the updated or created contact
   */
  @PutMapping(
      value = "/{domaine}/contact/{id}",
      consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<?> createOrModifyContact(
      @RequestBody Contact contact,
      @PathVariable("id") String identifiant,
      @PathVariable("domaine") String domaine,
      @RequestParam("creation") boolean creation) {
    contact.setIdentifiant(identifiant);
    User sugoiUser = ouganextSugoiMapper.serializeToSugoi(contact, User.class);
    if (userService.findById(domaine, null, identifiant) != null) {
      userService.update(domaine, null, sugoiUser);
    } else if (creation) {
      userService.create(domaine, null, sugoiUser);
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ouganextSugoiMapper.serializeToOuganext(
                userService.findById(domaine, null, identifiant), Contact.class));
  }

  @GetMapping(
      value = "/{domaine}/contact/{id}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastConsultant(#domaine)")
  public ResponseEntity<?> getContactDomaine(
      @PathVariable("id") String identifiant, @PathVariable("domaine") String domaine) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ouganextSugoiMapper.serializeToOuganext(
                userService.findById(domaine, null, identifiant), Contact.class));
  }

  @DeleteMapping(
      value = "/{domaine}/contact/{id}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<?> deleteContact(
      @PathVariable("id") String identifiant, @PathVariable("domaine") String domaine) {
    if (userService.findById(domaine, null, identifiant) != null) {
      userService.delete(domaine, null, identifiant);
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * Send the login of a contact using its mail address
   *
   * @param identifiant id of the contact
   * @param domaine
   * @param modeEnvoiStrings can only be "mail"
   * @param infoEnvoi mail formating information
   * @return NOT_IMPLEMENTED
   */
  @PostMapping(
      value = "/{domaine}/contact/{id}/login",
      consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<?> envoiLogin(
      @PathVariable("id") String identifiant,
      @PathVariable("domaine") String domaine,
      @RequestParam("modeEnvoi") List<String> modeEnvoiStrings,
      @RequestBody InfoFormattage infoEnvoi) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }
}
