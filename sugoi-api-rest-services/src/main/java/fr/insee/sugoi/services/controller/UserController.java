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

import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.model.Habilitation;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@Tag(name = "Manage users")
@RequestMapping(value = {"/v2", "/"})
@SecurityRequirement(name = "oAuth")
public class UserController {

  @Autowired private UserService userService;

  @GetMapping(
      path = {"/{realm}/users", "/{realm}/{storage}/users"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Search users")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastReader(#realm,#storage)")
  public ResponseEntity<?> getUsers(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @RequestParam(name = "identifiant", required = false) String identifiant,
      @RequestParam(name = "nomCommun", required = false) String nomCommun,
      @RequestParam(name = "description", required = false) String description,
      @RequestParam(name = "organisationId", required = false) String organisationId,
      @RequestParam(name = "size", defaultValue = "20") int size,
      @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "searchCookie", required = false) String searchCookie,
      @RequestParam(name = "typeRecherche", defaultValue = "et", required = true)
          String typeRecherche,
      @RequestParam(name = "habilitation", required = false) List<String> habilitations,
      @RequestParam(name = "application", required = false) String application) {

    // set the user which will serve as a model to retrieve the matching users
    User searchUser = new User();
    searchUser.setUsername(identifiant);
    searchUser.setLastName(nomCommun);
    if (organisationId != null) {
      Organization organizationSearch = new Organization();
      organizationSearch.setIdentifiant(organisationId);
      searchUser.setOrganization(organizationSearch);
    }
    if (habilitations != null) {
      habilitations.forEach(
          habilitationName -> searchUser.addHabilitation(new Habilitation(habilitationName)));
    }

    // set the page to maintain the search request pagination
    PageableResult pageable = new PageableResult();
    if (searchCookie != null) {
      pageable.setCookie(searchCookie.getBytes());
    }
    pageable.setOffset(offset);
    pageable.setSize(size);

    PageResult<User> foundUsers =
        userService.findByProperties(realm, searchUser, pageable, storage);
    if (foundUsers.isHasMoreResult()) {
      URI location =
          ServletUriComponentsBuilder.fromCurrentRequest()
              .replaceQueryParam("offset", offset + size)
              .build()
              .toUri();
      return ResponseEntity.status(HttpStatus.OK)
          .header(HttpHeaders.LOCATION, location.toString())
          .body(foundUsers);
    } else {
      return ResponseEntity.status(HttpStatus.OK).body(foundUsers);
    }
  }

  @PostMapping(
      value = {"/{realm}/users", "/{realm}/{storage}/users"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Create user")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastWriter(#realm,#storage)")
  public ResponseEntity<?> createUsers(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @RequestBody User user) {
    if (userService.findById(realm, user.getUsername(), storage) == null) {
      userService.create(realm, user, storage);
      URI location =
          ServletUriComponentsBuilder.fromCurrentRequest()
              .path("/" + user.getUsername())
              .build()
              .toUri();
      return ResponseEntity.created(location)
          .body(userService.findById(realm, user.getUsername(), storage));
    } else {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
  }

  @PutMapping(
      value = {"/{realm}/users/{id}", "/{realm}/{storage}/users/{id}"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Update user")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastWriter(#realm,#storage)")
  public ResponseEntity<?> updateUsers(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @PathVariable("id") String id,
      @RequestBody User user) {

    if (!user.getUsername().equals(id)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    if (userService.findById(realm, id, storage) != null) {
      userService.update(realm, user, storage);
      URI location =
          ServletUriComponentsBuilder.fromCurrentRequest()
              .path("/" + user.getUsername())
              .build()
              .toUri();
      return ResponseEntity.status(HttpStatus.OK)
          .header(HttpHeaders.LOCATION, location.toString())
          .body(userService.findById(realm, id, storage));
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @DeleteMapping(
      value = {"/{realm}/users/{id}", "/{realm}/{storage}/users/{id}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Delete user")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastWriter(#realm,#storage)")
  public ResponseEntity<String> deleteUsers(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @PathVariable("id") String id) {

    if (userService.findById(realm, id, storage) != null) {
      userService.delete(realm, id, storage);
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @GetMapping(
      path = {"/{realm}/users/{name}", "/{realm}/{storage}/users/{name}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Get user by username")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastReader(#realm,#storage)")
  public ResponseEntity<User> getUserByUsername(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @PathVariable("username") String id) {
    User user = userService.findById(realm, id, storage);
    if (user != null) {
      return ResponseEntity.status(HttpStatus.OK).body(user);
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}
