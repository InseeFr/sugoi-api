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

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.sugoi.converter.ouganext.Profils;
import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.old.services.utils.ResponseUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/v1/profils")
@SecurityRequirement(name = "basic")
public class ProfilsController {

  @Autowired
  private ConfigService configService;

  @GetMapping(value = "/", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
  @PreAuthorize("@OldAuthorizeMethodDecider.isAdmin()")
  public ResponseEntity<?> getProfils() {
    List<Realm> realms = configService.getRealms();
    Profils profils = new Profils();
    profils.getListe().addAll(realms.stream().map(realm -> ResponseUtils.convertRealmToProfils(realm))
        .flatMap(List::stream).collect(Collectors.toList()));
    return new ResponseEntity<>(profils, HttpStatus.OK);
  }
}
