package fr.insee.sugoi.services.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@Tag(name = "Gestion des applications")
@RequestMapping(value = { "/v2", "/" })
public class ApplicationController {

    @GetMapping(path = { "/{realm}/applications", "/{realm}/{storage}/applications" }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastConsultant(#realm,#userStorage)")
    public ResponseEntity<?> getApplications(@PathVariable("realm") String realm,
            @PathVariable(name = "storage", required = false) String userStorage) {
        return null;
    }

}
