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

import fr.insee.sugoi.converter.ouganext.Profil;
import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.old.services.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/profil")
public class ProfilController {

  @Autowired private ConfigService configService;

  @GetMapping(
      value = "/{nom}",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAdmin()")
  public ResponseEntity<?> getProfil(@PathVariable("nom") String nom) {
    Realm realm = configService.getRealm(nom);
    Profil profil = ResponseUtils.convertRealmToProfils(realm).get(0);
    return new ResponseEntity<>(profil, HttpStatus.OK);
  }

  @PutMapping(
      value = "/",
      consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAdmin()")
  public ResponseEntity<?> createOrModifyProfil(@RequestBody Profil profil) {
    return null;
  }

  @DeleteMapping(
      value = "/nom",
      produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("@OldAuthorizeMethodDecider.isAdmin()")
  public ResponseEntity<?> deleteProfil(@PathVariable("nom") String nom) {
    return null;
  }
}
