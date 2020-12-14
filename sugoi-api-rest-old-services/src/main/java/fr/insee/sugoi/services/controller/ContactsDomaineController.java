package fr.insee.sugoi.services.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.sugoi.converter.ouganext.Contact;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

@RestController
@RequestMapping("/v1/")
@Tags(value = { @Tag(name = "V1"), @Tag(name = "contacts") })
public class ContactsDomaineController {

    @RequestMapping(value = "/{domaine}/contacts", method = RequestMethod.POST, consumes = {
            MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                    MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> createContactIdsentifiant(@PathVariable("domaine") String domaine,
            @RequestHeader("Slug") String slug, Contact contact) {
        return null;
    }

    @RequestMapping(value = "/{domaine}/contacts", method = RequestMethod.GET, consumes = {
            MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                    MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> getContacts(@PathVariable("domaine") String domaine,
            @RequestParam("identifiant") String identifiant, @RequestParam("nomCommun") String nomCommun,
            @RequestParam("description") String description, @RequestParam("organisationId") String organisationId,
            @RequestParam("mail") String mail, @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam("start") int offset, @RequestParam("searchCookie") String searchCookie,
            @RequestParam(name = "typeRecherche", defaultValue = "et") String typeRecherche,
            @RequestParam("habilitation") List<String> habilitations, @RequestParam("application") String application,
            @RequestParam("role") String role, @RequestParam("rolePropriete") String rolePropriete,
            @RequestParam(name = "body", defaultValue = "false") boolean resultatsDansBody,
            @RequestParam(name = "idOnly", defaultValue = "false") boolean identifiantsSeuls,
            @RequestParam("certificat") String certificat) {
        return null;
    }

    @RequestMapping(value = "/{domaine}/contacts/certificat/{id}", method = RequestMethod.GET, consumes = {
            MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                    MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> getAPartirDeIdCertificat(@PathVariable("domaine") String domaine,
            @PathVariable("id") String id) {
        return null;
    }

    @RequestMapping(value = "/{domaine}/contacts/size", method = RequestMethod.GET, consumes = {
            MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
                    MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> getContactsSize(@PathVariable("domaine") String domaine) {
        return null;
    }
}
