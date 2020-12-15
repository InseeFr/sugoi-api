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

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Gestion des applications")
@RequestMapping(value = {"/v2", "/"})
public class ApplicationController {

  @GetMapping(
      path = {"/{realm}/applications", "/{realm}/{storage}/applications"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastConsultant(#realm,#userStorage)")
  public ResponseEntity<?> getApplications(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String userStorage) {
    return null;
  }
}
