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
import fr.insee.sugoi.core.exceptions.GroupNotFoundException;
import fr.insee.sugoi.core.service.GroupService;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.old.services.model.ConverterDomainRealm;
import fr.insee.sugoi.old.services.model.RealmStorage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[Deprecated] - Manage group", description = "Old Enpoints to manage group")
@RestController
@RequestMapping("/v1")
@SecurityRequirement(name = "basic")
public class GroupeController {

  private OuganextSugoiMapper ouganextSugoiMapper = new OuganextSugoiMapper();

  @Autowired private GroupService groupService;

  @Autowired private ConverterDomainRealm converterDomainRealm;

  /**
   * Get all contacts in a group
   *
   * @param domaine
   * @param application name of application of the group
   * @param groupe name of the group
   * @return OK with contact if at least one contact found, NOT_FOUND if no contact found
   */
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastConsultant(#domaine)")
  @GetMapping(
      value = "/{domaine}/contacts/groupe/{application}/{groupe}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Get all contacts in a group", deprecated = true)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Group and its contacts",
            content = {
              @Content(mediaType = "application/json"),
              @Content(mediaType = "application/xml")
            }),
        @ApiResponse(
            responseCode = "404",
            description = "Group not found",
            content = {
              @Content(mediaType = "application/json"),
              @Content(mediaType = "application/xml")
            })
      })
  public ResponseEntity<?> getContactByDomaineAndGroups(
      @Parameter(
              description = "Name of the domaine where the operation will be made",
              required = true)
          @PathVariable(name = "domaine", required = true)
          String domaine,
      @Parameter(description = "Application where to find groups", required = true)
          @PathVariable(name = "application", required = true)
          String application,
      @Parameter(description = "Group where collect contacts", required = true)
          @PathVariable(name = "groupe", required = true)
          String groupe) {
    RealmStorage realmUserStorage = converterDomainRealm.getRealmForDomain(domaine);

    Group group =
        groupService
            .findById(realmUserStorage.getRealm(), application, groupe)
            .orElseThrow(
                () ->
                    new GroupNotFoundException(
                        "Cannot find group " + groupe + " in domaine " + domaine));
    if (group.getUsers() != null) {
      if (group.getUsers().isEmpty()) {
        return new ResponseEntity<>("No users in group", HttpStatus.NOT_FOUND);
      } else {
        Contacts contacts = new Contacts();
        contacts
            .getListe()
            .addAll(
                group.getUsers().stream()
                    .map(user -> ouganextSugoiMapper.serializeToOuganext(user, Contact.class))
                    .collect(Collectors.toList()));
        return ResponseEntity.ok().body(contacts);
      }
    } else {
      return new ResponseEntity<>("Group not found", HttpStatus.NOT_FOUND);
    }
  }
}
