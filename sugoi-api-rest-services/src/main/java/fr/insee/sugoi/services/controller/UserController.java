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

import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.model.User;
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
@Tag(name = "Manage users")
@RequestMapping(value = {"/v2", "/"})
public class UserController {

  @Autowired private UserService userService;

  @GetMapping(
      path = {"/{realm}/Users", "/{realm}/{storage}/Users"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastReader(#realm,#userStorage)")
  public ResponseEntity<?> getUsers(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String userStorage,
      @RequestParam(name = "identifiant", required = false) String identifiant,
      @RequestParam(name = "nomCommun", required = false) String nomCommun,
      @RequestParam(name = "description", required = false) String description,
      @RequestParam(name = "organisationId", required = false) String organisationId,
      @RequestParam(name = "size", defaultValue = "20") int size,
      @RequestParam(name = "start", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "searchCookie", required = false) String searchCookie,
      @RequestParam(name = "typeRecherche", defaultValue = "et", required = true)
          String typeRecherche,
      @RequestParam(name = "habilitation", required = false) List<String> habilitations,
      @RequestParam(name = "application", required = false) String application) {
    // TODO: process GET request

    return null;
  }

  @PostMapping(
      value = {"/{realm}/Users", "/{realm}/{storage}/Users"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastWriter(#realm,#userStorage)")
  public ResponseEntity<?> createUsers(
      @PathVariable("realm") String realm,
      @PathVariable("storage") String UserStorage,
      @RequestBody User user) {
    // TODO: process POST request

    return new ResponseEntity<>(user, HttpStatus.CREATED);
  }

  @PutMapping(
      value = {"/{realm}/Users/{id}", "/{realm}/{storage}/Users/{id}"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastWriter(#realm,#userStorage)")
  public ResponseEntity<?> updateUsers(
      @PathVariable("realm") String realm,
      @PathVariable("storage") String UserStorage,
      @PathVariable("id") String id,
      @RequestBody User user) {
    // TODO: process PUT request

    return new ResponseEntity<>(user, HttpStatus.OK);
  }

  @DeleteMapping(
      value = {"/{realm}/Users/{id}", "/{realm}/{storage}/Users/{id}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastWriter(#realm,#userStorage)")
  public ResponseEntity<String> deleteUsers(
      @PathVariable("realm") String realm,
      @PathVariable("storage") String UserStorage,
      @PathVariable("id") String id) {
    // TODO: process DELETE request

    return new ResponseEntity<String>(id, HttpStatus.OK);
  }

  @PostMapping(
      path = {"/{realm}/users", "/{realm}/{storage}/users"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<User> createUser(
      @PathVariable(name = "realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      User user) {
    User createdUser;
    try {
      createdUser = userService.create(realm, storage, user);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    } catch (Exception e) {
      return ResponseEntity.status(500).build();
    }
  }

  @DeleteMapping(
      path = {"/delete/{realm}/user/{id}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public void deleteUser(
      @PathVariable(name = "realm") String realm, @PathVariable(name = "id") String id) {
    userService.delete(realm, id);
  }
}
