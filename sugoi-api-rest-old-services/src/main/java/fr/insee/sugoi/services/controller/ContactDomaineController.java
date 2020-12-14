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

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.sugoi.converter.ouganext.Contact;
import fr.insee.sugoi.converter.ouganext.InfoFormattage;
import fr.insee.sugoi.converter.ouganext.PasswordChangeRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

@RestController
@RequestMapping("/v1")
@Tags(value = { @Tag(name = "V1"), @Tag(name = "contact") })
public class ContactDomaineController {

        // @Autowired
        // private UserService userService;

        // @Autowired
        // private OuganextSugoiMapper ouganextSugoiMapper;

        @RequestMapping(method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_XML_VALUE,
                        MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_XML_VALUE,
                                        MediaType.APPLICATION_JSON_VALUE }, path = "/{domaine}/contact/{id}")
        public ResponseEntity<?> createOrModifyContact(Contact contact, @PathVariable("id") String identifiant,
                        @PathVariable("domaine") String domaine, @RequestParam("creation") boolean creation) {
                return null;
        }

        @RequestMapping(value = "/{domaine}/contact/{id}", method = RequestMethod.GET, consumes = {
                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
        public ResponseEntity<?> getContactDomaine(@PathVariable("id") String identifiant,
                        @PathVariable("domaine") String domaine) {
                return null;
        }

        @RequestMapping(value = "/{domaine}/contact/{id}", method = RequestMethod.DELETE, consumes = {
                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
        public ResponseEntity<?> deleteContact(@PathVariable("id") String identifiant,
                        @PathVariable("domaine") String domaine) {
                return null;
        }

        @RequestMapping(value = "/{domaine}/contact/{id}/password", method = RequestMethod.POST, consumes = {
                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
        public ResponseEntity<?> reinitPassword(@PathVariable("id") String identifiant,
                        @PathVariable("domaine") String domaine,
                        @RequestParam("modeEnvoi") List<String> modeEnvoisString, PasswordChangeRequest pcr) {
                return null;
        }

        @RequestMapping(value = "/{domaine}/contact/{id}/password/first", method = RequestMethod.POST, consumes = {
                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
        public ResponseEntity<?> initPassword(@PathVariable("id") String identifiant,
                        @PathVariable("domaine") String domaine,
                        @RequestParam("modeEnvoi") List<String> modeEnvoisString, PasswordChangeRequest pcr) {
                return null;
        }

        @RequestMapping(value = "/{domaine}/contact/{id}/password", method = RequestMethod.PUT, consumes = {
                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
        public ResponseEntity<?> changePassword(@PathVariable("id") String identifiant,
                        @PathVariable("domaine") String domaine,
                        @RequestParam("modeEnvoi") List<String> modeEnvoisString, PasswordChangeRequest pcr) {
                return null;
        }

        @RequestMapping(value = "/{domaine}/contact/{id}/habilitations", method = RequestMethod.GET, consumes = {
                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
        public ResponseEntity<?> getHabilitation(@PathVariable("id") String identifiant,
                        @PathVariable("domaine") String domaine) {
                return null;
        }

        @RequestMapping(value = "/{domaine}/contact/{id}/habilitations/{application}", method = RequestMethod.PUT, consumes = {
                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
        public ResponseEntity<?> addHabilitations(@PathVariable("id") String identifiant,
                        @PathVariable("domaine") String domaine, @PathVariable("application") String appName,
                        @RequestParam("role") List<String> nomRoles) {
                return null;
        }

        @RequestMapping(value = "/{domaine}/contact/{id}/habilitations/{application}/{role}", method = RequestMethod.PUT, consumes = {
                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
        public ResponseEntity<?> addHabilitationsWithProperty(@PathVariable("id") String identifiant,
                        @PathVariable("domaine") String domaine, @PathVariable("application") String appName,
                        @PathVariable("role") @RequestParam("propriete") List<String> nomRoles) {
                return null;
        }

        @RequestMapping(value = "/{domaine}/contact/{id}/habilitations/{application}", method = RequestMethod.DELETE, consumes = {
                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
        public ResponseEntity<?> deleteHabilitations(@PathVariable("id") String identifiant,
                        @PathVariable("domaine") String domaine, @PathVariable("application") String appName,
                        @RequestParam("role") List<String> nomRoles) {
                return null;
        }

        @RequestMapping(value = "/{domaine}/contact/{id}/habilitations/{application}/{role}", method = RequestMethod.DELETE, consumes = {
                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
        public ResponseEntity<?> deleteHabilitationsWithProperty(@PathVariable("id") String identifiant,
                        @PathVariable("domaine") String domaine, @PathVariable("application") String appName,
                        @PathVariable("role") String nomRole, @RequestParam("propriete") List<String> proprietes) {
                return null;
        }

        @RequestMapping(value = "/{domaine}/contact/{id}/login", method = RequestMethod.POST, consumes = {
                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
        public ResponseEntity<?> envoiLogin(@PathVariable("id") String identifiant,
                        @PathVariable("domaine") String domaine,
                        @RequestParam("modeEnvoi") List<String> modeEnvoiStrings, InfoFormattage infoEnvoi) {
                return null;
        }

        @RequestMapping(value = "/{domaine}/contact/{id}/groupes", method = RequestMethod.GET, consumes = {
                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
        public ResponseEntity<?> getGroups(@PathVariable("id") String identifiant,
                        @PathVariable("domaine") String domaine) {
                return null;
        }

        @RequestMapping(value = "/{domaine}/contact/{id}/groupes/{nomappli}/{nomgroupe}", method = RequestMethod.PUT, consumes = {
                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
        public ResponseEntity<?> addToGroup(@PathVariable("id") String identifiant,
                        @PathVariable("domaine") String domaine, @PathVariable("nomappli") String nomAppli,
                        @PathVariable("nomgroupe") String nomGroupe) {
                return null;
        }

        @RequestMapping(value = "{domaine}/contact/{id}/groupes/{nomappli}/{nomgroupe}", method = RequestMethod.DELETE, consumes = {
                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
        public ResponseEntity<?> removeFromGroups(@PathVariable("id") String identifiant,
                        @PathVariable("domaine") String domaine, @PathVariable("nomappli") String nomAppli,
                        @PathVariable("nomgroupe") String nomGroupe) {
                return null;
        }

        @RequestMapping(value = "/{domaine}/contact/{id}/inseeroles", method = RequestMethod.GET, consumes = {
                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
        public ResponseEntity<?> getInseeRoles(@PathVariable("id") String identifiant,
                        @PathVariable("domaine") String domaine) {
                return null;
        }

        @RequestMapping(value = "/{domaine}/contact/{id}/inseeroles/{inseerole}", method = RequestMethod.PUT, consumes = {
                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
        public ResponseEntity<?> addInseeRoles(@PathVariable("id") String identifiant,
                        @PathVariable("domaine") String domaine, @PathVariable("inseerole") String inseeRole) {
                return null;
        }

        @RequestMapping(value = "/{domaine}/contact/{id}/inseeroles/{inseerole}", method = RequestMethod.DELETE, consumes = {
                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                                        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
        public ResponseEntity<?> removeInseeRole(@PathVariable("id") String identifiant,
                        @PathVariable("domaine") String domaine, @PathVariable("inseerole") String inseeRole) {
                return null;
        }

}
