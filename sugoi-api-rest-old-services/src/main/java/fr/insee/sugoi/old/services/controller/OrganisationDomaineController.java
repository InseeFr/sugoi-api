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
import fr.insee.sugoi.converter.ouganext.Organisation;
import fr.insee.sugoi.converter.ouganext.Organisations;
import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.model.SearchType;
import fr.insee.sugoi.core.service.OrganizationService;
import fr.insee.sugoi.model.Organization;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/v1")
@SecurityRequirement(name = "basic")
@Tag(name = "V1- Gestion des organisations")
public class OrganisationDomaineController {

  private OuganextSugoiMapper ouganextSugoiMapper = new OuganextSugoiMapper();

  @Autowired private OrganizationService organizationService;

  /**
   * Update or create an organisation.
   *
   * @param domaine realm of the request
   * @param id id of the organisation to update, will replace the id of the organization body
   *     parameter
   * @param creation if true a non existent organisation to update is created
   * @param organisation
   * @return OK with the updated or created organisation
   */
  @PutMapping(
      value = "/{domaine}/organisation/{id}",
      consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<Organisation> createOrModifyOrganisation(
      @PathVariable("domaine") String domaine,
      @PathVariable("id") String id,
      @RequestParam("creation") boolean creation,
      @RequestBody Organisation organisation) {
    organisation.setIdentifiant(id);
    Organization sugoiOrganization =
        ouganextSugoiMapper.serializeToSugoi(organisation, Organization.class);

    if (organizationService.findById(domaine, null, id) != null) {
      organizationService.update(domaine, null, sugoiOrganization);
    } else if (creation) {
      organizationService.create(domaine, null, sugoiOrganization);
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ouganextSugoiMapper.serializeToOuganext(
                organizationService.findById(domaine, null, id), Organisation.class));
  }

  @GetMapping(
      value = "/{domaine}/organisation/{id}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastConsultant(#domaine)")
  public ResponseEntity<Organisation> getOrganisationDomaine(
      @PathVariable("domaine") String domaine, @PathVariable("id") String id) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ouganextSugoiMapper.serializeToOuganext(
                organizationService.findById(domaine, null, id), Organisation.class));
  }

  @DeleteMapping(
      value = "/{domaine}/organisation/{id}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<?> deleteOrganisation(
      @PathVariable("domaine") String domaine, @PathVariable("id") String id) {

    if (organizationService.findById(domaine, null, id) != null) {
      organizationService.delete(domaine, null, id);
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * Create an organisation. The organisation id is slug if filled and not already used. Otherwise
   * the id is generated.
   *
   * @param domaine
   * @param organisation
   * @param slug filled in header Slug, the id of the organisation if not already used
   * @return OK with the created organisation
   */
  @PostMapping(
      value = "{domaine}/organisations",
      consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastGestionnaire(#domaine)")
  public ResponseEntity<?> createOrganisation(
      @PathVariable("domaine") String domaine,
      @RequestBody Organisation organisation,
      @RequestHeader(value = "Slug", required = false) String slug) {
    Organization sugoiOrganization =
        ouganextSugoiMapper.serializeToSugoi(organisation, Organization.class);
    if (organizationService.findById(domaine, null, slug) == null) {
      sugoiOrganization.setIdentifiant(slug);
    } else {
      sugoiOrganization.setIdentifiant(UUID.randomUUID().toString());
    }
    organizationService.create(domaine, null, sugoiOrganization);
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ouganextSugoiMapper.serializeToOuganext(
                organizationService.findById(domaine, null, sugoiOrganization.getIdentifiant()),
                Organisation.class));
  }

  /**
   * Search organizations matching criteria.
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
   * @param resultatsDansBody if false, response is 204 and Http Response contains link headers to
   *     retrieve the resources
   * @param identifiantsSeuls if true, the contacts found will only have id value
   * @param certificat
   * @return OK and all the Organisation in the body if resultatsDansBody or NO_CONTENT and the Link
   *     to the results as a header
   */
  @GetMapping(
      value = "/{domaine}/organisations",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAtLeastConsultant(#domaine)")
  public ResponseEntity<?> getOrganisations(
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
      @RequestParam(name = "body", defaultValue = "false") boolean resultatsDansBody,
      @RequestParam(name = "idOnly", defaultValue = "false") boolean identifiantsSeuls,
      @RequestParam(name = "certificat", required = false) String certificat) {
    Organisation searchOrganisation = new Organisation();
    searchOrganisation.setIdentifiant(identifiant);
    searchOrganisation.setNomCommun(nomCommun);
    searchOrganisation.setDescription(description);
    if (organisationId != null) {
      Organisation subOrganisation = new Organisation();
      subOrganisation.setIdentifiant(organisationId);
      searchOrganisation.setOrganisationDeRattachement(subOrganisation);
    }
    searchOrganisation.setAdresseMessagerie(mail);
    if (certificat != null) {
      searchOrganisation.setCleDeChiffrement(certificat.getBytes());
    }
    Organization searchSugoiOrganization =
        ouganextSugoiMapper.serializeToSugoi(searchOrganisation, Organization.class);

    PageableResult pageable = new PageableResult();
    if (searchCookie != null) {
      pageable.setCookie(searchCookie.getBytes());
    }
    pageable.setOffset(offset);
    pageable.setSize(size);

    PageResult<Organization> foundOrganizations =
        organizationService.findByProperties(
            domaine, null, searchSugoiOrganization, pageable, SearchType.AND);

    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Size", String.valueOf(foundOrganizations.getTotalElements()));
    headers.add(
        "nextLocation",
        ServletUriComponentsBuilder.fromCurrentRequest()
            .replaceQueryParam("offset", foundOrganizations.getNextStart())
            .build()
            .toString());
    if (!resultatsDansBody) {
      headers.put(
          "Link",
          foundOrganizations.getResults().stream()
              .map(
                  organization -> {
                    String request =
                        ServletUriComponentsBuilder.fromCurrentRequestUri().build().toString();
                    return Link.fromUri(
                            request.substring(0, request.lastIndexOf("/"))
                                + "/organisation/"
                                + organization.getIdentifiant())
                        .rel("http://xml/insee.fr/schema/annuaire/Contact")
                        .build()
                        .toString();
                  })
              .collect(Collectors.toList()));
      return ResponseEntity.status(HttpStatus.NO_CONTENT).headers(headers).build();
    } else {
      Organisations organisations = new Organisations();
      if (identifiantsSeuls) {
        organisations
            .getListe()
            .addAll(
                foundOrganizations.getResults().stream()
                    .map(
                        organization -> {
                          Organisation organisation = new Organisation();
                          organisation.setIdentifiant(organization.getIdentifiant());
                          return organisation;
                        })
                    .collect(Collectors.toList()));
      } else {
        organisations
            .getListe()
            .addAll(
                foundOrganizations.getResults().stream()
                    .map(
                        organization ->
                            ouganextSugoiMapper.serializeToOuganext(
                                organization, Organisation.class))
                    .collect(Collectors.toList()));
      }
      return ResponseEntity.status(HttpStatus.OK).headers(headers).body(organisations);
    }
  }
}
