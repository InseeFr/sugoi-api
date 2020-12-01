package fr.insee.sugoi.store.file;

import java.util.List;

import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.technics.ReaderStore;
import fr.insee.sugoi.model.Habilitation;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;

public class FileReaderStore implements ReaderStore {

    @Override
    public User searchUser(String domaine, String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PageResult<User> searchUsers(String identifiant, String nomCommun, String description, String organisationId,
            String domaineGestion, String mail, PageableResult pageable, String typeRecherche,
            List<String> habilitations, String application, String role, String rolePropriete, String certificat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Organization searchOrganization(String domaine, String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Habilitation getHabilitation(String domaine, String id) {
        // TODO Auto-generated method stub
        return null;
    }

}
