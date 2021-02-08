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
import fr.insee.sugoi.converter.ouganext.Contacts;
import fr.insee.sugoi.converter.ouganext.Organisation;
import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.model.SearchType;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.model.Habilitation;
import fr.insee.sugoi.model.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/v1")
@Tag(name = "V1- Gestion des contacts")
@SecurityRequirement(name = "basic")
public class ContactsDomaineController {

  @Autowired private UserService userService;

  @Autowired private OuganextSugoiMapper ouganextSugoiMapper;

  /**
   * Create a contact. The contact id is slug if filled and not already used. Otherwise the id is
   * generated.
   *
   * @param domaine
   * @param contact
   * @param slug filled in header Slug, the id of the organisation if not already used
   * @return OK with the created organisation
   */
  @PostMapping(
      value = "/{domaine}/contacts",
      consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<Contact> createContactIdentifiant(
      @PathVariable("domaine") String domaine,
      @RequestHeader(value = "Slug", required = false) String slug,
      @RequestBody Contact contact) {
    User sugoiUser = ouganextSugoiMapper.serializeToSugoi(contact, User.class);
    if (userService.findById(domaine, null, slug) == null) {
      sugoiUser.setUsername(slug);
    } else {
      sugoiUser.setUsername(UUID.randomUUID().toString());
    }
    userService.create(domaine, null, sugoiUser);
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ouganextSugoiMapper.serializeToOuganext(
                userService.findById(domaine, null, sugoiUser.getUsername()), Contact.class));
  }

  /**
   * Search contacts matching criteria.
   *
   * @param domaine realm of the request
   * @param identifiant
   * @param nomCommun
   * @param description
   * @param organisationId
   * @param mail
   * @param size number of results to return
   * @param offset
   * @param searchCookie a cookie to browse several pages
   * @param typeRecherche "ET or "OU" to decide if all criteria must be respected or only one
   * @param habilitations
   * @param application
   * @param role
   * @param rolePropriete
   * @param resultatsDansBody if false, response is 204 and Http Response contains link headers to
   *     retrieve the resources
   * @param identifiantsSeuls if true, the contacts found will only have id value
   * @param certificat
   * @return OK and all the contacts in the body if resultatsDansBody or NO_CONTENT and the Link to
   *     the results as a header
   */
  @GetMapping(
      value = "/{domaine}/contacts",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastConsultant(#domaine)")
  public ResponseEntity<?> getContacts(
      @PathVariable("domaine") String domaine,
      @RequestParam(name = "identifiant", required = false) String identifiant,
      @RequestParam(name = "nomCommun", required = false) String nomCommun,
      @RequestParam(name = "description", required = false) String description,
      @RequestParam(name = "organisationId", required = false) String organisationId,
      @RequestParam(name = "mail", required = false) String mail,
      @RequestParam(name = "size", defaultValue = "20") int size,
      @RequestParam(name = "start", defaultValue = "0") int offset,
      @RequestParam(name = "searchCookie", required = false) String searchCookie,
      @RequestParam(name = "typeRecherche", defaultValue = "et") String typeRecherche,
      @RequestParam(name = "habilitation", required = false) List<String> habilitations,
      @RequestParam(name = "application", required = false) String application,
      @RequestParam(name = "role", required = false) String role,
      @RequestParam(name = "rolePropriete", required = false) String rolePropriete,
      @RequestParam(name = "body", defaultValue = "false") boolean resultatsDansBody,
      @RequestParam(name = "idOnly", defaultValue = "false") boolean identifiantsSeuls,
      @RequestParam(name = "certificat", required = false) String certificat) {
    Contact searchContact = new Contact();
    searchContact.setIdentifiant(identifiant);
    searchContact.setNomCommun(nomCommun);
    searchContact.setDescription(description);
    if (organisationId != null) {
      Organisation searchOrganisation = new Organisation();
      searchOrganisation.setIdentifiant(organisationId);
      searchContact.setOrganisationDeRattachement(searchOrganisation);
    }
    searchContact.setAdresseMessagerie(mail);
    if (certificat != null) {
      searchContact.setCertificate(certificat.getBytes());
    }
    List<Habilitation> searchHabilitations = new ArrayList<>();
    if (habilitations != null) {
      searchHabilitations =
          habilitations.stream()
              .map(habilitation -> new Habilitation(habilitation))
              .collect(Collectors.toList());
    }
    if (application != null & role != null & rolePropriete != null) {
      Habilitation createdHabilitation = new Habilitation(application, role, rolePropriete);
      searchHabilitations.add(createdHabilitation);
    }
    User searchSugoiUser = ouganextSugoiMapper.serializeToSugoi(searchContact, User.class);
    searchSugoiUser.setHabilitations(searchHabilitations);

    PageableResult pageable = new PageableResult();
    if (searchCookie != null) {
      pageable.setCookie(searchCookie.getBytes());
    }
    pageable.setOffset(offset);
    pageable.setSize(size);

    PageResult<User> foundUsers =
        userService.findByProperties(domaine, null, searchSugoiUser, pageable, SearchType.AND);

    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Size", String.valueOf(foundUsers.getTotalElements()));
    headers.add(
        "nextLocation",
        ServletUriComponentsBuilder.fromCurrentRequest()
            .replaceQueryParam("offset", foundUsers.getNextStart())
            .build()
            .toString());

    if (!resultatsDansBody) {
      headers.put(
          "Link",
          foundUsers.getResults().stream()
              .map(
                  user -> {
                    String request =
                        ServletUriComponentsBuilder.fromCurrentRequestUri().build().toString();
                    return Link.fromUri(
                            request.substring(0, request.lastIndexOf("/"))
                                + "/contact/"
                                + user.getUsername())
                        .rel("http://xml/insee.fr/schema/annuaire/Contact")
                        .build()
                        .toString();
                  })
              .collect(Collectors.toList()));
      return ResponseEntity.status(HttpStatus.NO_CONTENT).headers(headers).build();
    } else {
      Contacts contacts = new Contacts();
      if (identifiantsSeuls) {
        contacts
            .getListe()
            .addAll(
                foundUsers.getResults().stream()
                    .map(
                        user -> {
                          Contact contact = new Contact();
                          contact.setIdentifiant(user.getUsername());
                          return contact;
                        })
                    .collect(Collectors.toList()));
      } else {
        contacts
            .getListe()
            .addAll(
                foundUsers.getResults().stream()
                    .map(user -> ouganextSugoiMapper.serializeToOuganext(user, Contact.class))
                    .collect(Collectors.toList()));
      }
      return ResponseEntity.status(HttpStatus.OK).headers(headers).body(contacts);
    }
  }

  /**
   * Find contacts by certificate
   *
   * @param domaine
   * @param id id of the certificate
   * @return NOT IMPLEMENTED
   */
  @GetMapping(
      value = "/{domaine}/contacts/certificat/{id}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastConsultant(#domaine)")
  public ResponseEntity<?> getAPartirDeIdCertificat(
      @PathVariable("domaine") String domaine, @PathVariable("id") String id) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /**
   * Get the number of contacts in domaine.
   *
   * @param domaine
   * @return number of contacts in domaine
   */
  @GetMapping(
      value = "/{domaine}/contacts/size",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastConsultant(#domaine)")
  public ResponseEntity<?> getContactsSize(@PathVariable("domaine") String domaine) {
    PageableResult pageable = new PageableResult();
    PageResult<User> foundUsers =
        userService.findByProperties(domaine, null, new User(), pageable, SearchType.AND);
    return ResponseEntity.status(HttpStatus.NO_CONTENT)
        .header("X-Total-Size", String.valueOf(foundUsers.getTotalElements()))
        .build();
  }
}
