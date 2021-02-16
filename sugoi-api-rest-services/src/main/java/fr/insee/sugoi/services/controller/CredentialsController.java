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

import fr.insee.sugoi.core.model.PasswordChangeRequest;
import fr.insee.sugoi.core.model.SendMode;
import fr.insee.sugoi.core.service.CredentialsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Manage credentials")
@RequestMapping(value = {"/v2", "/"})
@SecurityRequirement(name = "oAuth")
public class CredentialsController {

  @Autowired private CredentialsService credentialsService;

  @PostMapping(
      path = {"/{realm}/users/{id}/reinitPassword", "/{realm}/{storage}/users/{id}/reinitPassword"})
  @PreAuthorize("@NewAuthorizeMethodDecider.isPasswordManager(#realm,#storage)")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void reinitPassword(
      @RequestBody PasswordChangeRequest pcr,
      @PathVariable("realm") String realm,
      @PathVariable(value = "storage", required = false) String userStorage,
      @PathVariable("id") String id,
      @RequestParam(value = "sendModes", required = false) List<SendMode> sendMode) {
    credentialsService.reinitPassword(
        realm, userStorage, id, pcr, sendMode != null ? sendMode : new ArrayList<>());
  }

  @PostMapping(
      path = {"/{realm}/users/{id}/changePassword", "/{realm}/{storage}/users/{id}/changePassword"})
  @PreAuthorize("@NewAuthorizeMethodDecider.isPasswordManager(#realm,#storage)")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void changePassword(
      @RequestBody PasswordChangeRequest pcr,
      @PathVariable("realm") String realm,
      @PathVariable(value = "storage", required = false) String userStorage,
      @PathVariable("id") String id) {
    credentialsService.changePassword(realm, userStorage, id, pcr);
  }

  @PostMapping(
      path = {"/{realm}/users/{id}/initPassword", "/{realm}/{storage}/users/{id}/initPassword"})
  @PreAuthorize("@NewAuthorizeMethodDecider.isPasswordManager(#realm,#storage)")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void initPassword(
      @PathVariable("realm") String realm,
      @PathVariable(value = "storage", required = false) String userStorage,
      @PathVariable("id") String id,
      @RequestBody PasswordChangeRequest pcr,
      @RequestParam(value = "sendModes", required = false) List<SendMode> sendMode) {
    credentialsService.initPassword(
        realm, userStorage, id, pcr, sendMode != null ? sendMode : new ArrayList<>());
  }

  @PostMapping(
      path = {
        "/{realm}/users/{id}/validate-password",
        "/{realm}/{storage}/users/{id}/validate-password"
      },
      consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
  @PreAuthorize("@NewAuthorizeMethodDecider.isPasswordManager(#realm,#storage)")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<Void> validatePassword(
      @PathVariable("realm") String realm,
      @PathVariable(value = "storage", required = false) String userStorage,
      @PathVariable("id") String id,
      @RequestParam MultiValueMap<String, String> params) {
    if (params.containsKey("password")
        && credentialsService.validateCredential(
            realm, userStorage, id, params.getFirst("password"))) {
      return ResponseEntity.ok().build();

    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }
}
