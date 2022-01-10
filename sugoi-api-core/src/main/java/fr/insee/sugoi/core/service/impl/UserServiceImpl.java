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
import fr.insee.sugoi.core.exceptions.RealmNotFoundException;
import fr.insee.sugoi.core.exceptions.UnableToUpdateCertificateException;
import fr.insee.sugoi.core.exceptions.UserAlreadyExistException;
import fr.insee.sugoi.core.exceptions.UserNotFoundByMailException;
import fr.insee.sugoi.core.exceptions.UserNotFoundException;
import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.model.ProviderResponse.ProviderResponseStatus;
import fr.insee.sugoi.core.model.SugoiRandomIdCharacterData;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.seealso.SeeAlsoService;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.core.store.ReaderStore;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import fr.insee.sugoi.model.paging.SearchType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.passay.CharacterRule;
import org.passay.PasswordGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@ConfigurationProperties("fr.insee.sugoi")
public class UserServiceImpl implements UserService {

  /* Size of the ids randomly generated */
  private int idCreateLength = 7;

  /*
   * Is the reader store asynchronous, ie a difference can exist between what we
   * read in readerstore and the realty. Can occur if the current service is
   * connected by a broker to the real service
   */
  private boolean readerStoreAsynchronous = false;

  @Autowired private StoreProvider storeProvider;

  @Autowired private RealmProvider realmProvider;

  @Autowired private SugoiEventPublisher sugoiEventPublisher;

  @Autowired(required = false)
  private SeeAlsoService seeAlsoService;

  protected static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

  @Override
  public ProviderResponse create(
      String realm, String storage, User user, ProviderRequest providerRequest) {
    try {

      // Mail and Id unicity check, generated id if missing only when reader and
      // writer are synchronous
      if (!readerStoreAsynchronous) {

        Realm realmLoaded = realmProvider.load(realm).get();
        if (user.getUsername() == null) {
          // generate id if null
          boolean idGeneratedAndUnique = false;
          do {
            final String id = generateId(true, true, idCreateLength);
            user.setUsername(id);
            // unicity requiered at realm level
            idGeneratedAndUnique =
                !realmLoaded.getUserStorages().stream()
                    .map(us -> storeProvider.getReaderStore(realm, us.getName()).getUser(id))
                    .anyMatch(u -> u.isPresent());
          } while (!idGeneratedAndUnique);
        } else {
          // check id unicity
          if (realmLoaded.getUserStorages().stream()
              .map(
                  us ->
                      storeProvider.getReaderStore(realm, us.getName()).getUser(user.getUsername()))
              .anyMatch(u -> u.isPresent())) {
            throw new UserAlreadyExistException(
                "User " + user.getUsername() + " already exist in realm " + realm);
          }
        }

        // check mail unicity if needed
        if (Boolean.parseBoolean(
                realmLoaded
                    .getProperties()
                    .getOrDefault(GlobalKeysConfig.VERIFY_MAIL_UNICITY, "false"))
            && user.getMail() != null
            && realmLoaded.getUserStorages().stream()
                .map(
                    us ->
                        storeProvider
                            .getReaderStore(realm, us.getName())
                            .getUserByMail(user.getMail()))
                .anyMatch(u -> u.isPresent())) {
          throw new UserAlreadyExistException(
              "A user has the same mail " + user.getMail() + " in realm " + realm);
        }
      }

      ProviderResponse response =
          storeProvider.getWriterStore(realm, storage).createUser(user, providerRequest);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storage,
          SugoiEventTypeEnum.CREATE_USER,
          Map.ofEntries(Map.entry(EventKeysConfig.USER, user)));

      // TODO Must be done here because at the provider level we doesn't have the
      // readerstore
      if (!providerRequest.isAsynchronousAllowed()
          && response.getStatus().equals(ProviderResponseStatus.OK)) {
        user.setUsername(response.getEntityId());
        response.setEntity(findById(realm, storage, user.getUsername()));
      }
      return response;
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storage,
          SugoiEventTypeEnum.CREATE_USER_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.USER, user),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      throw e;
    }
  }

  @Override
  public ProviderResponse update(
      String realm, String storage, User user, ProviderRequest providerRequest) {

    // Mail unicity check only when reader and
    // writer are synchronous
    if (!readerStoreAsynchronous) {
      Realm realmLoaded = realmProvider.load(realm).get();

      // check mail unicity if needed (don't check is new mail is blank or empty as
      // this indicate
      // delete mail)
      if (Boolean.parseBoolean(
              realmLoaded
                  .getProperties()
                  .getOrDefault(GlobalKeysConfig.VERIFY_MAIL_UNICITY, "false"))
          && user.getMail() != null
          && !user.getMail().isBlank()) {
        if (realmLoaded.getUserStorages().stream()
            .map(
                us ->
                    storeProvider.getReaderStore(realm, us.getName()).getUserByMail(user.getMail()))
            .anyMatch(
                u ->
                    u.isPresent() && !u.get().getUsername().equalsIgnoreCase(user.getUsername()))) {
          throw new UserAlreadyExistException(
              "A user has the same mail " + user.getMail() + " in realm " + realm);
        }
      }
    }

    try {
      ProviderResponse response =
          storeProvider.getWriterStore(realm, storage).updateUser(user, providerRequest);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storage,
          SugoiEventTypeEnum.UPDATE_USER,
          Map.ofEntries(Map.entry(EventKeysConfig.USER, user)));
      if (!providerRequest.isAsynchronousAllowed()
          && response.getStatus().equals(ProviderResponseStatus.OK)) {
        response.setEntity(findById(realm, storage, user.getUsername()));
      }
      return response;
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storage,
          SugoiEventTypeEnum.UPDATE_USER_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.USER, user),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      throw e;
    }
  }

  @Override
  public ProviderResponse delete(
      String realmName, String storage, String id, ProviderRequest providerRequest) {
    try {
      ProviderResponse response =
          storeProvider.getWriterStore(realmName, storage).deleteUser(id, providerRequest);
      sugoiEventPublisher.publishCustomEvent(
          realmName,
          storage,
          SugoiEventTypeEnum.DELETE_USER,
          Map.ofEntries(Map.entry(EventKeysConfig.USER_ID, id)));
      return response;
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
  public User findById(String realmName, String storage, String id) {
    try {
      Realm realm =
          realmProvider.load(realmName).orElseThrow(() -> new RealmNotFoundException(realmName));
      String nonNullStorage =
          storage != null
              ? storage
              : realm.getUserStorages().stream()
                  .filter(us -> exist(realmName, us.getName(), id))
                  .findFirst()
                  .orElseThrow(() -> new UserNotFoundException(realmName, id))
                  .getName();
      User user =
          storeProvider
              .getReaderStore(realmName, nonNullStorage)
              .getUser(id)
              .orElseThrow(() -> new UserNotFoundException(realmName, nonNullStorage, id));
      user.addMetadatas(GlobalKeysConfig.REALM, realmName.toLowerCase());
      user.addMetadatas(GlobalKeysConfig.USERSTORAGE, nonNullStorage.toLowerCase());
      if (seeAlsoService != null
          && realm.getProperties().containsKey(GlobalKeysConfig.SEEALSO_ATTRIBUTES)) {
        for (String seeAlso : findUserSeeAlsos(realm, user)) {
          seeAlsoService.decorateWithSeeAlso(user, seeAlso);
        }
      }
      sugoiEventPublisher.publishCustomEvent(
          realmName,
          nonNullStorage,
          SugoiEventTypeEnum.FIND_USER_BY_ID,
          Map.ofEntries(Map.entry(EventKeysConfig.USER_ID, id != null ? id : "")));
      return user;
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realmName,
          storage,
          SugoiEventTypeEnum.FIND_USER_BY_ID_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.USER_ID, id != null ? id : ""),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      throw e;
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
        Realm r = realmProvider.load(realm).orElseThrow(() -> new RealmNotFoundException(realm));
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
          result.setTotalElements(
              temResult.getTotalElements() == -1
                  ? temResult.getTotalElements()
                  : result.getTotalElements() + temResult.getTotalElements());
          result.setSearchToken(temResult.getSearchToken());
          result.setHasMoreResult(temResult.isHasMoreResult());
          if (result.getResults().size() >= result.getPageSize()) {
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
      throw e;
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

  @Override
  public ProviderResponse addAppManagedAttribute(
      String realm,
      String storage,
      String userId,
      String attributeKey,
      String attribute,
      ProviderRequest providerRequest) {
    try {
      ProviderResponse response =
          storeProvider
              .getWriterStore(realm, storage)
              .addAppManagedAttribute(userId, attributeKey, attribute, providerRequest);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storage,
          SugoiEventTypeEnum.ADD_APP_MANAGED_ATTRIBUTES,
          Map.ofEntries(
              Map.entry(EventKeysConfig.ATTRIBUTE_KEY, attributeKey),
              Map.entry(EventKeysConfig.ATTRIBUTE_VALUE, attribute),
              Map.entry(EventKeysConfig.USER_ID, userId)));
      return response;
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storage,
          SugoiEventTypeEnum.ADD_APP_MANAGED_ATTRIBUTES_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.ATTRIBUTE_KEY, attributeKey),
              Map.entry(EventKeysConfig.ATTRIBUTE_VALUE, attribute),
              Map.entry(EventKeysConfig.USER_ID, userId),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      throw e;
    }
  }

  @Override
  public ProviderResponse deleteAppManagedAttribute(
      String realm,
      String storage,
      String userId,
      String attributeKey,
      String attribute,
      ProviderRequest providerRequest) {
    try {
      ProviderResponse response =
          storeProvider
              .getWriterStore(realm, storage)
              .deleteAppManagedAttribute(userId, attributeKey, attribute, providerRequest);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storage,
          SugoiEventTypeEnum.DELETE_APP_MANAGED_ATTRIBUTES,
          Map.ofEntries(
              Map.entry(EventKeysConfig.ATTRIBUTE_KEY, attributeKey),
              Map.entry(EventKeysConfig.ATTRIBUTE_VALUE, attribute),
              Map.entry(EventKeysConfig.USER_ID, userId)));
      return response;
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          storage,
          SugoiEventTypeEnum.DELETE_APP_MANAGED_ATTRIBUTES_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.ATTRIBUTE_KEY, attributeKey),
              Map.entry(EventKeysConfig.ATTRIBUTE_VALUE, attribute),
              Map.entry(EventKeysConfig.USER_ID, userId),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      throw e;
    }
  }

  @Override
  public User findByMail(String realmName, String storageName, String mail) {
    try {
      Realm realm =
          realmProvider.load(realmName).orElseThrow(() -> new RealmNotFoundException(realmName));
      String nonNullStorage =
          storageName != null
              ? storageName
              : realm.getUserStorages().stream()
                  .filter(us -> existByMail(realmName, us.getName(), mail))
                  .findFirst()
                  .orElseThrow(() -> new UserNotFoundByMailException(realmName, mail))
                  .getName();
      User user =
          storeProvider
              .getReaderStore(realmName, storageName)
              .getUserByMail(mail)
              .orElseThrow(() -> new UserNotFoundByMailException(realmName, nonNullStorage, mail));
      user.addMetadatas(GlobalKeysConfig.REALM, realmName.toLowerCase());
      user.addMetadatas(GlobalKeysConfig.USERSTORAGE, storageName.toLowerCase());
      if (seeAlsoService != null
          && realm.getProperties().containsKey(GlobalKeysConfig.SEEALSO_ATTRIBUTES)) {
        for (String seeAlso : findUserSeeAlsos(realm, user)) {
          seeAlsoService.decorateWithSeeAlso(user, seeAlso);
        }
      }
      sugoiEventPublisher.publishCustomEvent(
          realmName,
          storageName,
          SugoiEventTypeEnum.FIND_USER_BY_MAIL,
          Map.ofEntries(Map.entry(EventKeysConfig.USER_MAIL, mail != null ? mail : "")));
      return user;
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realmName,
          storageName,
          SugoiEventTypeEnum.FIND_USER_BY_MAIL_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.USER_MAIL, mail != null ? mail : ""),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      throw e;
    }
  }

  @Override
  public byte[] getCertificate(String realm, String userStorage, String userId) {
    return findById(realm, userStorage, userId).getCertificate();
  }

  @Override
  public ProviderResponse updateCertificate(
      String realm,
      String userStorage,
      String userId,
      byte[] certificat,
      ProviderRequest providerRequest) {
    try {
      User user = findById(realm, userStorage, userId);
      ProviderResponse response =
          storeProvider
              .getWriterStore(realm, userStorage)
              .updateUserCertificate(user, certificat, providerRequest);
      return response;
    } catch (Exception e) {
      throw new UnableToUpdateCertificateException(
          "Cannot update certificate because: " + e.toString(), e);
    }
  }

  @Override
  public ProviderResponse deleteCertificate(
      String realm, String userStorage, String id, ProviderRequest providerRequest) {
    User user = findById(realm, userStorage, id);
    ProviderResponse response =
        storeProvider
            .getWriterStore(realm, userStorage)
            .deleteUserCertificate(user, providerRequest);
    return response;
  }

  @Override
  public boolean exist(String realm, String userStorage, String userId) {
    return storeProvider.getReaderStore(realm, userStorage).getUser(userId).isPresent();
  }

  public String generateId(Boolean withUpperCase, Boolean withDigit, Integer size) {

    // Use of PasswordGenerator to generate random id
    PasswordGenerator passwordGenerator = new PasswordGenerator();
    String id =
        passwordGenerator.generatePassword(
            size != null ? size : idCreateLength,
            generateRandomIdCharacterRules(withUpperCase, withDigit));
    return id;
  }

  public List<CharacterRule> generateRandomIdCharacterRules(
      Boolean withUpperCase, Boolean withDigit) {
    List<CharacterRule> characterRules = new ArrayList<>();
    if (withUpperCase)
      characterRules.add(new CharacterRule(SugoiRandomIdCharacterData.UpperCase, 1));
    if (withDigit) characterRules.add(new CharacterRule(SugoiRandomIdCharacterData.Digit, 1));
    return characterRules;
  }

  public int getIdCreateLength() {
    return idCreateLength;
  }

  public void setIdCreateLength(int idCreateLength) {
    this.idCreateLength = idCreateLength;
  }

  public boolean isReaderStoreAsynchronous() {
    return readerStoreAsynchronous;
  }

  public void setReaderStoreAsynchronous(boolean readerStoreAsynchronous) {
    this.readerStoreAsynchronous = readerStoreAsynchronous;
  }

  private List<String> findUserSeeAlsos(Realm realm, User user) {
    String[] seeAlsosAttributes =
        realm.getProperties().get(GlobalKeysConfig.SEEALSO_ATTRIBUTES).replace(" ", "").split(",");
    List<String> seeAlsos = new ArrayList<>();
    for (String seeAlsoAttribute : seeAlsosAttributes) {
      Object seeAlsoAttributeValue = user.getAttributes().get(seeAlsoAttribute);
      if (seeAlsoAttributeValue instanceof String) {
        seeAlsos.add((String) seeAlsoAttributeValue);
      } else if (seeAlsoAttributeValue instanceof List) {
        ((List<?>) seeAlsoAttributeValue).forEach(seeAlso -> seeAlsos.add((String) seeAlso));
      }
    }
    return seeAlsos;
  }

  private boolean existByMail(String realm, String userStorage, String mail) {
    return storeProvider.getReaderStore(realm, userStorage).getUserByMail(mail).isPresent();
  }
}
