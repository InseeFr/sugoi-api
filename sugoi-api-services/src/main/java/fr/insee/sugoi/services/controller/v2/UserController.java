package fr.insee.sugoi.services.controller.v2;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.Technique.MyPage;
import fr.insee.sugoi.sugoiapicore.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Users")
@RequestMapping(value = { "/v2", "/" })
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(description = "Chercher un utilisateur par idep", operationId = "userSearch", summary = "Chercher un utilisateur par idep")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "search results matching criteria"),
            @ApiResponse(responseCode = "404", description = "entity not found") })
    @GetMapping(produces = { MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE }, value = "/{domaine}/user/{id}")
    public User getUserByIdAndDomaine(@PathVariable("id") String id, @PathVariable("domaine") String domaine)
            throws Exception {
        return userService.searchUser(domaine, id);
    }

    @GetMapping(produces = { MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE }, value = "/{domaine}/users")
    public MyPage<User> getUsersByDomaine(@RequestParam(name = "identifiant", required = false) String identifiant,
            @RequestParam(name = "nomCommun", required = false) String nomCommun,
            @RequestParam(name = "description", required = false) String description,
            @RequestParam(name = "organisationId", required = false) String organisationId,
            @PathVariable(name = "domaine") String domaineGestion,
            @RequestParam(name = "mail", required = false) String mail,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "start", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "searchCookie", required = false) String searchCookie,
            @RequestParam(name = "typeRecherche", defaultValue = "et", required = true) String typeRecherche,
            @RequestParam(name = "habilitation", required = false) List<String> habilitations,
            @RequestParam(name = "application", required = false) String application,
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "rolePropriete", required = false) String rolePropriete,
            @RequestParam(name = "body", defaultValue = "false", required = false) boolean resultatsDansBody,
            @RequestParam(name = "idOnly", defaultValue = "false", required = false) boolean identifiantsSeuls,
            @RequestParam(name = "certificat", required = false) String certificat) {

        MyPage<User> users = userService.searchUsers(identifiant, nomCommun, description, organisationId,
                domaineGestion, mail, searchCookie, size, offset, typeRecherche, habilitations, application, role,
                rolePropriete, certificat);
        return users;
    }

}
