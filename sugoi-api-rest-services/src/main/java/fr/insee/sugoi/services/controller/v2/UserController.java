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
package fr.insee.sugoi.services.controller.v2;

import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Users")
@RequestMapping(value = {"/v2", "/"})
public class UserController {

  @Autowired private UserService userService;

  @Operation(
      description = "Chercher un utilisateur par idep",
      operationId = "userSearch",
      summary = "Chercher un utilisateur par idep")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "search results matching criteria"),
        @ApiResponse(responseCode = "404", description = "entity not found")
      })
  @GetMapping(
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
      value = "/{realm}/users/{username}")
  public User getUserByUsernameAndDomaine(
      @PathVariable("username") String id, @PathVariable("realm") String domaine) throws Exception {
    return userService.searchUser(domaine, id);
  }

  @GetMapping(
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
      value = "/{domaine}/users")
  public PageResult<User> getUsersByDomaine(
      @RequestParam(name = "identifiant", required = false) String identifiant,
      @RequestParam(name = "nomCommun", required = false) String nomCommun,
      @RequestParam(name = "description", required = false) String description,
      @RequestParam(name = "organisationId", required = false) String organisationId,
      @PathVariable(name = "domaine") String domaineGestion,
      @RequestParam(name = "mail", required = false) String mail,
      @RequestParam(name = "size", defaultValue = "20") int size,
      @RequestParam(name = "start", required = false, defaultValue = "0") int offset,
      @RequestParam(name = "searchCookie", required = false) String searchCookie,
      @RequestParam(name = "typeRecherche", defaultValue = "et", required = true)
          String typeRecherche,
      @RequestParam(name = "habilitation", required = false) List<String> habilitations,
      @RequestParam(name = "application", required = false) String application,
      @RequestParam(name = "role", required = false) String role,
      @RequestParam(name = "rolePropriete", required = false) String rolePropriete,
      @RequestParam(name = "body", defaultValue = "false", required = false)
          boolean resultatsDansBody,
      @RequestParam(name = "idOnly", defaultValue = "false", required = false)
          boolean identifiantsSeuls,
      @RequestParam(name = "certificat", required = false) String certificat) {

    PageResult<User> users =
        userService.searchUsers(
            identifiant,
            nomCommun,
            description,
            organisationId,
            domaineGestion,
            mail,
            searchCookie,
            size,
            offset,
            typeRecherche,
            habilitations,
            application,
            role,
            rolePropriete,
            certificat);
    return users;
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

  @GetMapping(
      path = {"/delete/{realm}/user/{id}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public void deleteUser(
      @PathVariable(name = "realm") String realm, @PathVariable(name = "id") String id) {
    userService.delete(realm, id);
  }
}
