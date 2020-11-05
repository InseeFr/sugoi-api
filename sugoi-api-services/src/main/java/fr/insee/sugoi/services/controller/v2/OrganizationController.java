package fr.insee.sugoi.services.controller.v2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.sugoiapicore.service.OrganizationService;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = { "/v2", "/" })
@Tag(name = "Organization")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @GetMapping(produces = { MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE }, value = "/{domaine}/organization/{name}")
    public Organization getMethodName(@PathVariable("domaine") String realm, @PathVariable("name") String name)
            throws Exception {
        return organizationService.searchOrganization(realm, name);
    }

}
