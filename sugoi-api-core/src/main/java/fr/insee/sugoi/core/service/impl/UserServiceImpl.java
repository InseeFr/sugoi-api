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
package fr.insee.sugoi.core.service.impl;

import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.core.technics.RealmProvider;
import fr.insee.sugoi.core.technics.StoreProvider;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.UserStorage;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  @Autowired private StoreProvider storeProvider;

  @Autowired private RealmProvider realmProvider;

  public User searchUser(String domaine, String id) {
    try {
      Realm realm = realmProvider.load(domaine);
      UserStorage userStorage = realm.getUserStorages().get(0);
      User user =
          storeProvider
              .getStoreForUserStorage(realm.getName(), userStorage.getName())
              .getReader()
              .searchUser(domaine, id);
      return user;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PageResult<User> searchUsers(
      String identifiant,
      String nomCommun,
      String description,
      String organisationId,
      String domaineGestion,
      String mail,
      String cookie,
      int size,
      int offset,
      String typeRecherche,
      List<String> habilitations,
      String application,
      String role,
      String rolePropriete,
      String certificat) {
    try {
      PageableResult pageable = new PageableResult();
      pageable.setSize(size);
      if (cookie != null) {
        pageable.setCookie(cookie.getBytes());
      }
      pageable.setFirst(offset);
      Realm realm = realmProvider.load(domaineGestion);
      UserStorage userStorage = realm.getUserStorages().get(0);
      return storeProvider
          .getStoreForUserStorage(realm.getName(), userStorage.getName())
          .getReader()
          .searchUsers(
              identifiant,
              nomCommun,
              description,
              organisationId,
              domaineGestion,
              mail,
              pageable,
              typeRecherche,
              habilitations,
              application,
              role,
              rolePropriete,
              certificat);
    } catch (Exception e) {
      throw new RuntimeException("Erreur lors de la récupération des utilisateurs");
    }
  }

  @Override
  public User create(String realm, String storage, User user) {
    // TODO Auto-generated method stub
    return null;
  }
}
