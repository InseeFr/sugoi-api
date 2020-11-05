package fr.insee.sugoi.sugoiapicore.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.model.Technique.MyPage;
import fr.insee.sugoi.model.Technique.MyPageable;
import fr.insee.sugoi.sugoiapicore.configuration.RealmStorage;
import fr.insee.sugoi.sugoiapicore.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private RealmStorage realmStorage;

    public User searchUser(String domaine, String id) {
        try {
            Realm realm = realmStorage.getRealm(domaine);
            UserStorage userStorage = realm.getUserStorages().get(0);
            User user = userStorage.getStore().getReader().searchUser(domaine, id);
            return user;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MyPage<User> searchUsers(String identifiant, String nomCommun, String description, String organisationId,
            String domaineGestion, String mail, String cookie, int size, int offset, String typeRecherche,
            List<String> habilitations, String application, String role, String rolePropriete, String certificat) {
        try {
            MyPageable pageable = new MyPageable();
            pageable.setSize(size);
            if (cookie != null) {
                pageable.setCookie(cookie.getBytes());
            }
            pageable.setFirst(offset);
            Realm realm = realmStorage.getRealm(domaineGestion);
            UserStorage userStorage = realm.getUserStorages().get(0);
            return userStorage.getStore().getReader().searchUsers(identifiant, nomCommun, description, organisationId,
                    domaineGestion, mail, pageable, typeRecherche, habilitations, application, role, rolePropriete,
                    certificat);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des utilisateurs");
        }
    }

}
