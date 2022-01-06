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
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.model.Habilitation;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import fr.insee.sugoi.model.paging.SearchType;
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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
  public void getAllUsers(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(
              description = "Name of the userStorage where the operation will be made",
              required = false)
          @PathVariable(name = "storage", required = false)
          String storage,
      @Parameter(description = "User's identifiant of user to search ", required = false)
          @RequestParam(name = "identifiant", required = false)
          String identifiant,
      @Parameter(description = "User's mail of user to search ", required = false)
          @RequestParam(name = "mail", required = false)
          String mail,
      @Parameter(description = "User's commun name of user to search ", required = false)
          @RequestParam(name = "nomCommun", required = false)
          String nomCommun,
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
      @Parameter(description = "User's application of user to search ", required = false)
          @RequestParam(name = "application", required = false)
          String application,
      HttpServletResponse response) {

    // set the user which will serve as a model to retrieve the matching users
    User searchUser = new User();
    searchUser.setUsername(identifiant);
    searchUser.setLastName(nomCommun);
    searchUser.setMail(mail);
    if (organisationId != null) {
      Organization organizationSearch = new Organization();
      organizationSearch.setIdentifiant(organisationId);
      searchUser.setOrganization(organizationSearch);
    }
    if (habilitations != null) {
      habilitations.forEach(
          habilitationName -> searchUser.addHabilitation(new Habilitation(habilitationName)));
    }

    List<String> storageToFind = new ArrayList<>();

    if (storage != null && !storage.isEmpty()) {
      storageToFind.add(storage);
    } else {
      storageToFind =
          configService.getRealm(realm).getUserStorages().stream()
              .map(UserStorage::getName)
              .collect(Collectors.toList());
    }

    int allResultsSize = 0;
    try {
      response.setCharacterEncoding("UTF-8");
      response.setContentType("text/csv");
      CSVPrinter csvPrinter = new CSVPrinter(response.getWriter(), CSVFormat.DEFAULT);
      // print headers on the first line
      csvPrinter.printRecord(getFieldsAsStringList(new User()));

      for (String storageName : storageToFind) {

        // set the page to maintain the search request pagination
        PageableResult pageable = new PageableResult(pageSize, 0, null);

        while (true) {
          PageResult<User> foundUsers =
              userService.findByProperties(realm, storageName, searchUser, pageable, typeRecherche);
          csvPrinter.printRecords(
              foundUsers.getResults().stream()
                  .map(this::toStringList)
                  .collect(Collectors.toList()));
          csvPrinter.flush();

          if (foundUsers.isHasMoreResult()) {
            if (allResultsSize > maxSizeOutput) {
              csvPrinter.close();
              throw new RuntimeException("too much result");
            }
            pageable = new PageableResult(pageSize, 0, foundUsers.getSearchToken());
          } else {
            break;
          }
        }
        csvPrinter.flush();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    // too late, data already send
    // response.setStatus(HttpStatus.OK.value());
    // response.setHeader("X-TotalSize", String.valueOf(allResultsSize));

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
  public void getAllUsers(
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
      @Parameter(description = "User's commun name of user to search ", required = false)
          @RequestParam(name = "nomCommun", required = false)
          String nomCommun,
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
      @Parameter(description = "User's application of user to search ", required = false)
          @RequestParam(name = "application", required = false)
          String application,
      HttpServletResponse response) {
    getAllUsers(
        realm,
        null,
        identifiant,
        mail,
        nomCommun,
        description,
        organisationId,
        typeRecherche,
        habilitations,
        application,
        response);
  }

  private <T> List<String> getFieldsAsStringList(T t) {
    return Arrays.asList(t.getClass().getDeclaredFields()).stream()
        .map(f -> f.getName())
        .collect(Collectors.toList());
  }

  private <T> List<String> toStringList(T t) {
    List<String> list = new ArrayList<>();
    for (Field f : t.getClass().getDeclaredFields()) {
      f.setAccessible(true);
      try {
        Object value = f.get(t);
        if (value == null) {
          list.add("");
        } else {
          list.add(value.toString());
        }
      } catch (IllegalArgumentException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
    return list;
  }
}
