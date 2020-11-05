package fr.insee.sugoi.sugoiapicore.service;

import java.util.List;

import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.Technique.MyPage;

public interface UserService {

	User searchUser(String domaine, String idep);

	MyPage<User> searchUsers(String identifiant, String nomCommun, String description, String organisationId,
			String domaineGestion, String mail, String cookie, int size, int offset, String typeRecherche,
			List<String> habilitations, String application, String role, String rolePropriete, String certificat);

}
