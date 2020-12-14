package fr.insee.sugoi.services.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

@Tags(value = { @Tag(name = "V1"), @Tag(name = "credentials") })
@RestController
@RequestMapping("/v1/")
public class CredentialsDomaineController {

    @RequestMapping(value = "/{domaine}/credentials/{id}", method = RequestMethod.POST, consumes = {
            MediaType.TEXT_PLAIN_VALUE })
    public ResponseEntity<?> authentifierContact(@PathVariable("domaine") String domaine, @PathVariable("id") String id,
            String motDePasse) {
        return null;
    }

}
