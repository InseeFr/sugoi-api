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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.sugoi.core.service.PasswordService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/v1")
@SecurityRequirement(name = "basic")
public class PasswordController {

  @Autowired
  PasswordService passwordService;

  private static final int TAILLE_MINIMALE_PASSWORD = 8;

  @GetMapping(value = "/password", produces = { MediaType.TEXT_PLAIN_VALUE })
  public ResponseEntity<?> generatePasswords(@RequestParam("nb") int nb, @RequestParam("length") int length) {
    if (length < TAILLE_MINIMALE_PASSWORD) {
      return new ResponseEntity<>(
          "Le paramètre length est obligatoire et doit être supérieur ou égal à " + TAILLE_MINIMALE_PASSWORD + ".",
          HttpStatus.BAD_REQUEST);
    }
    if (nb < 1) {
      return new ResponseEntity<>("Le paramètre nb est obligatoire et doit être positif.", HttpStatus.BAD_REQUEST);
    }
    StringBuilder listeMdp = new StringBuilder();
    for (int i = 0; i < nb - 1; i++) {
      listeMdp.append(passwordService.generatePassword(length));
      listeMdp.append(System.getProperty("line.separator"));
    }
    listeMdp.append(passwordService.generatePassword(length));
    return new ResponseEntity<>(listeMdp, HttpStatus.OK);
  }
}
