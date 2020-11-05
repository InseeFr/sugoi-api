package fr.insee.sugoi.model.Technique;

import java.util.List;

import fr.insee.sugoi.model.Habilitation;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;

public interface ReaderStore {

    public User searchUser(String domaine, String id);

    public MyPage<User> searchUsers(String identifiant, String nomCommun, String description, String organisationId,
            String domaineGestion, String mail, MyPageable pageable, String typeRecherche, List<String> habilitations,
            String application, String role, String rolePropriete, String certificat);

    public Organization searchOrganization(String domaine, String id);

    public Habilitation getHabilitation(String domaine, String id);

    
}
