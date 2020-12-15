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

import fr.insee.sugoi.converter.ouganext.Contact;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@Tag(name = "V1- Gestion descontacts")
public class ContactsDomaineController {

  @PostMapping(
      value = "/{domaine}/contacts",
      consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<?> createContactIdsentifiant(
      @PathVariable("domaine") String domaine,
      @RequestHeader("Slug") String slug,
      @RequestBody Contact contact) {
    return null;
  }

  @GetMapping(
      value = "/{domaine}/contacts",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastConsultant(#domaine)")
  public ResponseEntity<?> getContacts(
      @PathVariable("domaine") String domaine,
      @RequestParam("identifiant") String identifiant,
      @RequestParam("nomCommun") String nomCommun,
      @RequestParam("description") String description,
      @RequestParam("organisationId") String organisationId,
      @RequestParam("mail") String mail,
      @RequestParam(name = "size", defaultValue = "20") int size,
      @RequestParam("start") int offset,
      @RequestParam("searchCookie") String searchCookie,
      @RequestParam(name = "typeRecherche", defaultValue = "et") String typeRecherche,
      @RequestParam("habilitation") List<String> habilitations,
      @RequestParam("application") String application,
      @RequestParam("role") String role,
      @RequestParam("rolePropriete") String rolePropriete,
      @RequestParam(name = "body", defaultValue = "false") boolean resultatsDansBody,
      @RequestParam(name = "idOnly", defaultValue = "false") boolean identifiantsSeuls,
      @RequestParam("certificat") String certificat) {
    return null;
  }

  @GetMapping(
      value = "/{domaine}/contacts/certificat/{id}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastConsultant(#domaine)")
  public ResponseEntity<?> getAPartirDeIdCertificat(
      @PathVariable("domaine") String domaine, @PathVariable("id") String id) {
    return null;
  }

  @GetMapping(
      value = "/{domaine}/contacts/size",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastConsultant(#domaine)")
  public ResponseEntity<?> getContactsSize(@PathVariable("domaine") String domaine) {
    return null;
  }
}
