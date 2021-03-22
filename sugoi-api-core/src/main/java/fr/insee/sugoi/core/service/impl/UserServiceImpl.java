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

import fr.insee.sugoi.core.event.model.SugoiEventTypeEnum;
import fr.insee.sugoi.core.event.publisher.SugoiEventPublisher;
import fr.insee.sugoi.core.exceptions.EntityNotFoundException;
import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.model.SearchType;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.core.store.ReaderStore;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.UserStorage;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  @Autowired private StoreProvider storeProvider;

  @Autowired private RealmProvider realmProvider;

  @Autowired private SugoiEventPublisher sugoiEventPublisher;

  protected static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

  @Override
  public User create(String realm, String storage, User user) {
    sugoiEventPublisher.publishCustomEvent(
        realm, storage, SugoiEventTypeEnum.CREATE_USER, Map.ofEntries(Map.entry("user", user)));
    return storeProvider.getWriterStore(realm, storage).createUser(user);
  }

  @Override
  public void update(String realm, String storage, User user) {
    sugoiEventPublisher.publishCustomEvent(
        realm, storage, SugoiEventTypeEnum.UPDATE_USER, Map.ofEntries(Map.entry("user", user)));
    storeProvider.getWriterStore(realm, storage).updateUser(user);
  }

  @Override
  public void delete(String realmName, String storage, String id) {
    sugoiEventPublisher.publishCustomEvent(
        realmName, storage, SugoiEventTypeEnum.DELETE_USER, Map.ofEntries(Map.entry("userId", id)));
    storeProvider.getWriterStore(realmName, storage).deleteUser(id);
  }

  @Override
  public User findById(String realmName, String storage, String id) {
    if (id != null) {
      sugoiEventPublisher.publishCustomEvent(
          realmName,
          storage,
          SugoiEventTypeEnum.FIND_USER_BY_ID,
          Map.ofEntries(Map.entry("userId", id)));
    }
    if (storage != null) {
      try {
        User user = storeProvider.getReaderStore(realmName, storage).getUser(id);
        user.addMetadatas("realm", realmName.toLowerCase());
        user.addMetadatas("userStorage", storage.toLowerCase());
        return user;
      } catch (Exception e) {
        throw new EntityNotFoundException(
            "User not found in realm " + realmName + " and userStorage " + storage);
      }
    } else {
      Realm r = realmProvider.load(realmName);
      for (UserStorage us : r.getUserStorages()) {
        try {
          User user = storeProvider.getReaderStore(realmName, us.getName()).getUser(id);
          if (user != null) {
            user.addMetadatas("realm", realmName);
            user.addMetadatas("userStorage", us.getName());
            return user;
          }
        } catch (Exception e) {
          logger.debug(
              "User " + id + "not in realm " + realmName + " and userstorage " + us.getName());
        }
      }
    }
    throw new EntityNotFoundException("User not found in realm " + realmName);
  }

  @Override
  public PageResult<User> findByProperties(
      String realm,
      String storage,
      User userProperties,
      PageableResult pageable,
      SearchType typeRecherche) {

    sugoiEventPublisher.publishCustomEvent(
        realm,
        storage,
        SugoiEventTypeEnum.FIND_USERS,
        Map.ofEntries(
            Map.entry("userProperties", userProperties),
            Map.entry("pageable", pageable),
            Map.entry("typeRecherche", typeRecherche)));
    PageResult<User> result = new PageResult<>();
    result.setPageSize(pageable.getSize());
    try {
      if (storage != null) {
        result =
            storeProvider
                .getReaderStore(realm, storage)
                .searchUsers(userProperties, pageable, typeRecherche.name());
        result
            .getResults()
            .forEach(
                user -> {
                  user.addMetadatas("realm", realm);
                  user.addMetadatas("userStorage", storage);
                });
      } else {
        Realm r = realmProvider.load(realm);
        for (UserStorage us : r.getUserStorages()) {
          ReaderStore readerStore =
              storeProvider.getStoreForUserStorage(realm, us.getName()).getReader();
          PageResult<User> temResult =
              readerStore.searchUsers(userProperties, pageable, typeRecherche.name());
          temResult
              .getResults()
              .forEach(
                  user -> {
                    user.addMetadatas("realm", realm);
                    user.addMetadatas("userStorage", us.getName());
                  });
          result.getResults().addAll(temResult.getResults());
          result.setTotalElements(result.getResults().size());
          if (result.getTotalElements() >= result.getPageSize()) {
            return result;
          }
          pageable.setSize(pageable.getSize() - result.getTotalElements());
        }
      }

    } catch (Exception e) {
      throw new RuntimeException("Erreur lors de la récupération des utilisateurs", e);
    }
    return result;
  }

  @Override
  public void addUserToGroup(
      String realm, String storage, String userId, String appName, String groupName) {
    sugoiEventPublisher.publishCustomEvent(
        realm,
        storage,
        SugoiEventTypeEnum.ADD_USER_TO_GROUP,
        Map.ofEntries(
            Map.entry("user", userId),
            Map.entry("appName", appName),
            Map.entry("groupName", groupName)));
    storeProvider.getWriterStore(realm, storage).addUserToGroup(appName, groupName, userId);
  }

  @Override
  public void deleteUserFromGroup(
      String realm, String storage, String userId, String appName, String groupName) {
    sugoiEventPublisher.publishCustomEvent(
        realm,
        storage,
        SugoiEventTypeEnum.DELETE_USER_FROM_GROUP,
        Map.ofEntries(
            Map.entry("user", userId),
            Map.entry("appName", appName),
            Map.entry("groupName", groupName)));
    storeProvider.getWriterStore(realm, storage).deleteUserFromGroup(appName, groupName, userId);
  }
}
