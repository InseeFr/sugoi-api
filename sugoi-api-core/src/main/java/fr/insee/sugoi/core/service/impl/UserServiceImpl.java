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
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  @Autowired private StoreProvider storeProvider;

  @Autowired private RealmProvider realmProvider;

  @Override
  public User create(String realm, User user, String storage) {
    return storeProvider
        .getStoreForUserStorage(
            realm,
            (storage != null) ? storage : realmProvider.load(realm).getDefaultUserStorageName())
        .getWriter()
        .createUser(user);
  }

  @Override
  public void update(String realm, User user, String storage) {
    storeProvider
        .getStoreForUserStorage(
            realm,
            (storage != null) ? storage : realmProvider.load(realm).getDefaultUserStorageName())
        .getWriter()
        .updateUser(user);
  }

  @Override
  public void delete(String realmName, String id, String storage) {
    storeProvider
        .getStoreForUserStorage(
            realmName,
            (storage != null) ? storage : realmProvider.load(realmName).getDefaultUserStorageName())
        .getWriter()
        .deleteUser(id);
  }

  @Override
  public User findById(String realmName, String id, String storage) {
    try {
      return storeProvider
          .getStoreForUserStorage(
              realmName,
              (storage != null)
                  ? storage
                  : realmProvider.load(realmName).getDefaultUserStorageName())
          .getReader()
          .getUser(id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PageResult<User> findByProperties(
      String realm, User userProperties, PageableResult pageable, String storage) {
    try {
      return storeProvider
          .getStoreForUserStorage(
              realm,
              (storage != null) ? storage : realmProvider.load(realm).getDefaultUserStorageName())
          .getReader()
          .searchUsers(userProperties, pageable, "");
    } catch (Exception e) {
      throw new RuntimeException("Erreur lors de la récupération des utilisateurs");
    }
  }
}
