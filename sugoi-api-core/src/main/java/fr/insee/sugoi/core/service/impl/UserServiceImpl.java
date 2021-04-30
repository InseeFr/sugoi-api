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

import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.core.event.configuration.EventKeysConfig;
import fr.insee.sugoi.core.event.model.SugoiEventTypeEnum;
import fr.insee.sugoi.core.event.publisher.SugoiEventPublisher;
import fr.insee.sugoi.core.exceptions.UserAlreadyExistException;
import fr.insee.sugoi.core.exceptions.UserNotCreatedException;
import fr.insee.sugoi.core.exceptions.UserNotFoundException;
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
import java.util.Optional;
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
    try {

      if (findById(realm, storage, user.getUsername()).isEmpty()) {
        String userName =
            storeProvider.getWriterStore(realm, storage).createUser(user).getUsername();
        sugoiEventPublisher.publishCustomEvent(
            realm,
            storage,
            SugoiEventTypeEnum.CREATE_USER,
            Map.ofEntries(Map.entry(EventKeysConfig.USER, user)));
        return findById(realm, storage, userName)
            .orElseThrow(
                () ->
                    new UserNotCreatedException(
                        "Cannot create user " + userName + " in realm " + realm));
      }
      throw new UserAlreadyExistException(
          "User " + user.getUsername() + " already exist in realm " + realm);
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storage,
          SugoiEventTypeEnum.CREATE_USER_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.USER, user),
              Map.entry(EventKeysConfig.ERROR, e.toString())));

      if (e instanceof UserAlreadyExistException) {
        throw (UserAlreadyExistException) e;
      } else if (e instanceof UserNotCreatedException) {
        throw (UserNotCreatedException) e;
      } else {
        throw e;
      }
    }
  }

  @Override
  public void update(String realm, String storage, User user) {

    try {
      findById(realm, storage, user.getUsername())
          .orElseThrow(
              () ->
                  new UserNotFoundException(
                      "Cannot find user " + user.getUsername() + " in realm " + realm));
      storeProvider.getWriterStore(realm, storage).updateUser(user);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storage,
          SugoiEventTypeEnum.UPDATE_USER,
          Map.ofEntries(Map.entry(EventKeysConfig.USER, user)));
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storage,
          SugoiEventTypeEnum.UPDATE_USER_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.USER, user),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      if (e instanceof UserNotFoundException) {
        throw (UserNotFoundException) e;
      } else {
        throw e;
      }
    }
  }

  @Override
  public void delete(String realmName, String storage, String id) {
    try {
      findById(realmName, storage, id)
          .orElseThrow(
              () -> new UserNotFoundException("Cannot find user " + id + " in realm " + realmName));
      storeProvider.getWriterStore(realmName, storage).deleteUser(id);
      sugoiEventPublisher.publishCustomEvent(
          realmName,
          storage,
          SugoiEventTypeEnum.DELETE_USER,
          Map.ofEntries(Map.entry(EventKeysConfig.USER_ID, id)));
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realmName,
          storage,
          SugoiEventTypeEnum.DELETE_USER_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.USER_ID, id),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      if (e instanceof UserNotFoundException) {
        throw (UserNotFoundException) e;
      } else {
        throw e;
      }
    }
  }

  @Override
  public Optional<User> findById(String realmName, String storage, String id) {
    User user = null;
    try {
      if (id != null) {
        if (storage != null) {
          user = storeProvider.getReaderStore(realmName, storage).getUser(id);
          user.addMetadatas(GlobalKeysConfig.REALM, realmName.toLowerCase());
          user.addMetadatas(GlobalKeysConfig.USERSTORAGE, storage.toLowerCase());
        } else {
          Realm r = realmProvider.load(realmName);
          for (UserStorage us : r.getUserStorages()) {
            try {
              user = storeProvider.getReaderStore(realmName, us.getName()).getUser(id);
              user.addMetadatas(GlobalKeysConfig.REALM, realmName);
              user.addMetadatas(GlobalKeysConfig.USERSTORAGE, us.getName());
            } catch (Exception e) {
              logger.debug(
                  "Error when trying to find user "
                      + id
                      + " on realm "
                      + realmName
                      + " and userstorage "
                      + us
                      + " error "
                      + e.getMessage());
            }
          }
        }
        sugoiEventPublisher.publishCustomEvent(
            realmName,
            storage,
            SugoiEventTypeEnum.FIND_USER_BY_ID,
            Map.ofEntries(Map.entry(EventKeysConfig.USER_ID, id)));
      }
      return Optional.ofNullable(user);
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realmName,
          storage,
          SugoiEventTypeEnum.FIND_USER_BY_ID_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.USER_ID, id),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      return Optional.ofNullable(user);
    }
  }

  @Override
  public PageResult<User> findByProperties(
      String realm,
      String storage,
      User userProperties,
      PageableResult pageable,
      SearchType typeRecherche) {

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
                  user.addMetadatas(EventKeysConfig.REALM, realm);
                  user.addMetadatas(EventKeysConfig.USERSTORAGE, storage);
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
                    user.addMetadatas(EventKeysConfig.REALM, realm);
                    user.addMetadatas(EventKeysConfig.USERSTORAGE, us.getName());
                  });
          result.getResults().addAll(temResult.getResults());
          result.setTotalElements(result.getResults().size());
          if (result.getTotalElements() >= result.getPageSize()) {
            sugoiEventPublisher.publishCustomEvent(
                realm,
                storage,
                SugoiEventTypeEnum.FIND_USERS,
                Map.ofEntries(
                    Map.entry(EventKeysConfig.USER_PROPERTIES, userProperties),
                    Map.entry(EventKeysConfig.PAGEABLE, pageable),
                    Map.entry(EventKeysConfig.TYPE_RECHERCHE, typeRecherche)));
            return result;
          }
          pageable.setSize(pageable.getSize() - result.getTotalElements());
        }
      }

    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storage,
          SugoiEventTypeEnum.FIND_USERS_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.USER_PROPERTIES, userProperties),
              Map.entry(EventKeysConfig.PAGEABLE, pageable),
              Map.entry(EventKeysConfig.TYPE_RECHERCHE, typeRecherche),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      throw new RuntimeException("Erreur lors de la récupération des utilisateurs", e);
    }
    sugoiEventPublisher.publishCustomEvent(
        realm,
        storage,
        SugoiEventTypeEnum.FIND_USERS,
        Map.ofEntries(
            Map.entry(EventKeysConfig.USER_PROPERTIES, userProperties),
            Map.entry(EventKeysConfig.PAGEABLE, pageable),
            Map.entry(EventKeysConfig.TYPE_RECHERCHE, typeRecherche)));
    return result;
  }
}
