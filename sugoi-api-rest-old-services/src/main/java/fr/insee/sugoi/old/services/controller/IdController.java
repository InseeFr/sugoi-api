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
import fr.insee.sugoi.converter.ouganext.ContactOuganext;
import fr.insee.sugoi.converter.ouganext.OrganisationOuganext;
import fr.insee.sugoi.core.exceptions.UserNotFoundException;
import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.core.service.OrganizationService;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.old.services.decider.AuthorizeMethodDecider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@Tag(name = "[Deprecated]", description = "Old Enpoints")
@SecurityRequirement(name = "basic")
public class IdController {

  @Autowired private UserService userService;
  @Autowired private OrganizationService organizationService;
  @Autowired private ConfigService configService;

  @Autowired private OuganextSugoiMapper ouganextSugoiMapper;

  @Autowired AuthorizeMethodDecider authorizeDecider;

  /**
   * Search an entity (contact or organisation) by its id on all domains. First contact found is
   * returned and if no contact is found the first organisation found.
   *
   * @param id Username of a contact or id of an organisation
   * @return a contact or an organisation
   */
  @GetMapping(
      value = "/{id}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Search an entity (contact or organisation) by its id", deprecated = true)
  public ResponseEntity<?> getById(
      @Parameter(description = "Username of a contact or id of an organisation", required = true)
          @PathVariable(name = "id", required = true)
          String id) {

    List<Realm> realms =
        configService.getRealms().stream()
            .filter(realm -> authorizeDecider.isAtLeastConsultant(realm.getName()))
            .collect(Collectors.toList());

    Optional<ContactOuganext> contact =
        realms.stream().map(realm -> findContact(realm, id)).filter(c -> c != null).findFirst();
    if (contact.isPresent()) {
      return ResponseEntity.ok().body(contact.get());
    } else {
      Optional<OrganisationOuganext> organisation =
          realms.stream()
              .map(realm -> findOrganisation(realm, id))
              .filter(o -> o != null)
              .findFirst();
      if (organisation.isPresent()) {
        return ResponseEntity.ok().body(organisation.get());
      } else {
        return ResponseEntity.notFound().build();
      }
    }
  }

  private ContactOuganext findContact(Realm realm, String id) {
    return ouganextSugoiMapper.serializeToOuganext(
        userService
            .findById(realm.getName(), null, id)
            .orElseThrow(() -> new UserNotFoundException("User" + id + " not found in " + realm)),
        ContactOuganext.class);
  }

  private OrganisationOuganext findOrganisation(Realm realm, String id) {
    return ouganextSugoiMapper.serializeToOuganext(
        organizationService.findById(realm.getName(), null, id), OrganisationOuganext.class);
  }
}
