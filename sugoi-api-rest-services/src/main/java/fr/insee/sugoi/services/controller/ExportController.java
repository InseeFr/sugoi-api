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
package fr.insee.sugoi.services.controller;

import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.core.service.GroupService;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Habilitation;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.exceptions.StoreException;
import fr.insee.sugoi.model.exceptions.UserNotFoundException;
import fr.insee.sugoi.model.exceptions.UserStorageNotFoundException;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import fr.insee.sugoi.model.paging.SearchType;
import fr.insee.sugoi.model.technics.StoreMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Export objects", description = "Endpoints to export all objects in some formats")
@RequestMapping(value = {"/v2", "/"})
@SecurityRequirements(
    value = {@SecurityRequirement(name = "oAuth"), @SecurityRequirement(name = "basic")})
public class ExportController {

  @Value("${fr.insee.sugoi.export.maxSizeOutput:10000}")
  private int maxSizeOutput;

  @Value("${fr.insee.sugoi.export.pagesize:2000}")
  private int pageSize;

  @Autowired private UserService userService;
  @Autowired private GroupService groupService;

  @Autowired private ConfigService configService;

  @GetMapping(path = {"/realms/{realm}/storages/{storage}/export/users/export.csv"})
  @Operation(summary = "Search users according to parameters")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Users found according to parameter",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PageResult.class))
            })
      })
  @PreAuthorize("@NewAuthorizeMethodDecider.isReader(#realm,#storage)")
  public ResponseEntity<Void> getAllUsers(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(
              description = "Name of the userStorage where the operation will be made",
              required = true)
          @PathVariable(name = "storage", required = false)
          String storage,
      @Parameter(description = "User's identifiant of user to search ")
          @RequestParam(name = "identifiant", required = false)
          String identifiant,
      @Parameter(description = "User's mail of user to search ")
          @RequestParam(name = "mail", required = false)
          String mail,
      @Parameter(description = "User's common name of user to search ", required = false)
          @RequestParam(name = "commonName", required = false)
          String commonName,
      @Parameter(description = "User's firstname of user to search ", required = false)
          @RequestParam(name = "firstName", required = false)
          String firstName,
      @Parameter(description = "User's lastname of user to search ", required = false)
          @RequestParam(name = "lastName", required = false)
          String lastName,
      @Parameter(description = "User's description", required = false)
          @RequestParam(name = "description", required = false)
          String description,
      @Parameter(description = "User rattached organization")
          @RequestParam(name = "organisationId", required = false)
          String organisationId,
      @Parameter(description = "Search type can be OR or AND ")
          @RequestParam(name = "typeRecherche", defaultValue = "AND", required = true)
          SearchType typeRecherche,
      @Parameter(description = "User's habilitations of user to search ")
          @RequestParam(name = "habilitation", required = false)
          List<String> habilitations,
      @Parameter(description = "Filter on group. applicationFilter parameter is required")
          @RequestParam(name = "groupFilter", required = false)
          String groupFilter,
      @Parameter(description = "Filter on application. groupFilter parameter is required")
          @RequestParam(name = "applicationFilter", required = false)
          String applicationFilter,
      HttpServletResponse response) {

    // set the user which will serve as a model to retrieve the matching users
    User searchUser = new User();
    searchUser.setUsername(identifiant);
    searchUser.setFirstName(firstName);
    searchUser.setLastName(lastName);
    searchUser.setMail(mail);
    if (groupFilter != null && applicationFilter != null) {
      searchUser.setGroups(List.of(new Group(groupFilter, applicationFilter)));
    }
    if (commonName != null) {
      searchUser.getAttributes().put("common_name", commonName);
    }
    if (organisationId != null) {
      Organization organizationSearch = new Organization();
      organizationSearch.setIdentifiant(organisationId);
      searchUser.setOrganization(organizationSearch);
    }
    if (habilitations != null) {
      habilitations.forEach(
          habilitationName -> searchUser.addHabilitation(new Habilitation(habilitationName)));
    }
    CSVPrinter csvPrinter;
    try {
      response.setCharacterEncoding("UTF-8");
      response.setContentType("text/csv");
      csvPrinter = new CSVPrinter(response.getWriter(), CSVFormat.DEFAULT);
      // print headers on the first line

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    List<StoreMapping> storeMappings =
        storage != null && !storage.isEmpty()
            ? configService
                .getRealm(realm)
                .getUserStorageByName(storage)
                .orElseThrow(() -> new UserStorageNotFoundException(realm, storage))
                .getUserMappings()
            : configService.getRealm(realm).getUserStorages().stream()
                .flatMap(storage1 -> storage1.getUserMappings().stream())
                .distinct()
                .collect(Collectors.toList());
    printHeader(csvPrinter, storeMappings);
    if (storage != null && !storage.isEmpty()) {
      getExportUsersWithStorages(
          searchUser, storage, storeMappings, realm, typeRecherche, csvPrinter);
    } else {
      configService
          .getRealm(realm)
          .getUserStorages()
          .forEach(
              storage1 ->
                  getExportUsersWithStorages(
                      searchUser,
                      storage1.getName(),
                      storeMappings,
                      realm,
                      typeRecherche,
                      csvPrinter));
    }
    return ResponseEntity.ok().build();
  }

  @GetMapping(
      path = {"/realms/{realm}/applications/{application}/groups/{group}/export/export.csv"})
  @Operation(summary = "Export all users in a group")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Members of group found",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PageResult.class))
            })
      })
  @PreAuthorize("@NewAuthorizeMethodDecider.isAppManager(#realm,#application)")
  public ResponseEntity<Void> getAllMembersInGroup(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(description = "The application containing the group", required = true)
          @PathVariable("application")
          String application,
      @Parameter(description = "Group from which members are to be retrieved", required = true)
          @PathVariable("group")
          String group,
      HttpServletResponse response)
      throws IOException {

    response.setCharacterEncoding("UTF-8");
    response.setContentType("text/csv");

    List<StoreMapping> headerMappings =
        configService.getRealm(realm).getUserStorages().stream()
            .flatMap(storage1 -> storage1.getUserMappings().stream())
            .distinct()
            .collect(Collectors.toList());

    CSVPrinter csvPrinter = new CSVPrinter(response.getWriter(), CSVFormat.DEFAULT);
    printHeader(csvPrinter, headerMappings);
    groupService
        .findById(realm, application, group)
        .getUsers()
        .forEach(
            u -> {
              try {
                csvPrinter.printRecord(
                    getCsvLineFromUser(
                        headerMappings, userService.findById(realm, null, u.getUsername(), false)));
              } catch (IOException e) {
                throw new RuntimeException(e);
              } catch (UserNotFoundException e) {
                // not a sugoi user
              }
            });
    csvPrinter.flush();
    return ResponseEntity.ok().build();
  }

  private void getExportUsersWithStorages(
      User searchUser,
      String storageName,
      List<StoreMapping> headerMappings,
      String realm,
      SearchType typeRecherche,
      CSVPrinter csvPrinter) {
    try {

      // set the page to maintain the search request pagination
      PageableResult pageable = new PageableResult(pageSize, 0, null);

      while (true) {
        PageResult<User> foundUsers =
            userService.findByProperties(
                realm, storageName, searchUser, pageable, typeRecherche, false);
        for (User foundUser : foundUsers.getResults()) {
          List<String> attributesToPrint = getCsvLineFromUser(headerMappings, foundUser);
          csvPrinter.printRecord(attributesToPrint);
          csvPrinter.flush();
        }

        if (foundUsers.isHasMoreResult()) {
          pageable = new PageableResult(pageSize, 0, foundUsers.getSearchToken());
        } else {
          break;
        }
      }
      csvPrinter.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @GetMapping(path = {"/realms/{realm}/export/users/export.csv"})
  @Operation(summary = "Search users according to parameters")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Users found according to parameters",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PageResult.class))
            })
      })
  @PreAuthorize("@NewAuthorizeMethodDecider.isReader(#realm,#storage)")
  public ResponseEntity<Void> getAllUsers(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(description = "User's identifiant of user to search ", required = false)
          @RequestParam(name = "identifiant", required = false)
          String identifiant,
      @Parameter(description = "User's mail of user to search ", required = false)
          @RequestParam(name = "mail", required = false)
          String mail,
      @Parameter(description = "User's common name of user to search ", required = false)
          @RequestParam(name = "commonName", required = false)
          String commonName,
      @Parameter(description = "User's firstname of user to search ", required = false)
          @RequestParam(name = "firstName", required = false)
          String firstName,
      @Parameter(description = "User's lastname of user to search ", required = false)
          @RequestParam(name = "lastName", required = false)
          String lastName,
      @Parameter(description = "User's description", required = false)
          @RequestParam(name = "description", required = false)
          String description,
      @Parameter(description = "User rattached organization", required = false)
          @RequestParam(name = "organisationId", required = false)
          String organisationId,
      @Parameter(description = "Search type can be OR or AND ", required = false)
          @RequestParam(name = "typeRecherche", defaultValue = "AND", required = true)
          SearchType typeRecherche,
      @Parameter(description = "User's habilitations of user to search ", required = false)
          @RequestParam(name = "habilitation", required = false)
          List<String> habilitations,
      @Parameter(description = "Filter on group. applicationFilter parameter is required")
          @RequestParam(name = "groupFilter", required = false)
          String groupFilter,
      @Parameter(description = "Filter on application. groupFilter parameter is required")
          @RequestParam(name = "applicationFilter", required = false)
          String applicationFilter,
      HttpServletResponse response) {
    return getAllUsers(
        realm,
        null,
        identifiant,
        mail,
        commonName,
        firstName,
        lastName,
        description,
        organisationId,
        typeRecherche,
        habilitations,
        groupFilter,
        applicationFilter,
        response);
  }

  private List<String> getCsvLineFromUser(List<StoreMapping> headerMappings, User user) {
    try {
      List<String> attributesToPrint = new ArrayList<>();
      for (StoreMapping headerMapping : headerMappings) {
        String newAttribute;
        switch (headerMapping.getModelType()) {
          case LIST_HABILITATION:
          case LIST_GROUP:
            newAttribute =
                ((List<?>) user.get(headerMapping.getSugoiName()).orElse(new ArrayList<>()))
                    .stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .collect(Collectors.joining(",", "", ""));
            break;
          case ORGANIZATION:
            newAttribute =
                ((Organization) user.get(headerMapping.getSugoiName()).orElse(new Organization()))
                    .getIdentifiant();
            break;
          default:
            newAttribute = user.get(headerMapping.getSugoiName()).orElse("").toString();
        }

        attributesToPrint.add(newAttribute);
      }
      return attributesToPrint;
    } catch (NoSuchFieldException e) {
      throw new StoreException(
          "A configured field has not been found during export. Check store configuration.", e);
    }
  }

  private void printHeader(CSVPrinter csvPrinter, List<StoreMapping> storeMappings) {
    try {
      csvPrinter.printRecord(
          storeMappings.stream().map(StoreMapping::getSugoiName).collect(Collectors.toList()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
