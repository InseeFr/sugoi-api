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
import fr.insee.sugoi.model.exceptions.NoCertificateOnUserException;
import fr.insee.sugoi.model.exceptions.RealmNotFoundException;
import fr.insee.sugoi.model.exceptions.UnableToUpdateCertificateException;
import fr.insee.sugoi.model.exceptions.UserAlreadyExistException;
import fr.insee.sugoi.model.exceptions.UserNotFoundByMailException;
import fr.insee.sugoi.model.exceptions.UserNotFoundException;
import fr.insee.sugoi.model.exceptions.UserStorageNotFoundException;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import fr.insee.sugoi.model.paging.SearchType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

  private boolean verifyUniqueMail = false;

  private int usersMaxoutputsize = 1000;

  /* Size of the ids randomly generated */
  private int idCreateLength = 7;

  /*
   * Is the reader store asynchronous, ie a difference can exist between what we
   * read in readerstore and the realty. Can occur if the current service is
   * connected by a broker to the real service
   */
  private boolean readerStoreAsynchronous = false;

  private boolean fuzzySearchAllowed = false;

  @Autowired private StoreProvider storeProvider;

  @Autowired private RealmProvider realmProvider;

  @Autowired(required = false)
  private SeeAlsoService seeAlsoService;

  protected static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

  @Override
  public ProviderResponse create(
      String realm, String storage, User user, ProviderRequest providerRequest) {
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
              realmLoaded.getUserStorages().stream()
                  .map(us -> storeProvider.getReaderStore(realm, us.getName()).getUser(id))
                  .noneMatch(Optional::isPresent);
        } while (!idGeneratedAndUnique);
      } else {
        // check id unicity
        if (realmLoaded.getUserStorages().stream()
            .map(
                us -> storeProvider.getReaderStore(realm, us.getName()).getUser(user.getUsername()))
            .anyMatch(Optional::isPresent)) {
          throw new UserAlreadyExistException(
              "User " + user.getUsername() + " already exist in realm " + realm);
        }
      }

      // check mail unicity if needed
      if (Boolean.parseBoolean(
              realmLoaded
                  .getProperties()
                  .getOrDefault(
                      GlobalKeysConfig.VERIFY_MAIL_UNICITY,
                      List.of(Boolean.toString(verifyUniqueMail)))
                  .get(0))
          && user.getMail() != null
          && realmLoaded.getUserStorages().stream()
              .map(
                  us ->
                      storeProvider
                          .getReaderStore(realm, us.getName())
                          .getUserByMail(user.getMail()))
              .anyMatch(Optional::isPresent)) {
        throw new UserAlreadyExistException(
            "A user has the same mail " + user.getMail() + " in realm " + realm);
      }
    }

    ProviderResponse response =
        storeProvider.getWriterStore(realm, storage).createUser(user, providerRequest);

    // TODO Must be done here because at the provider level we don't have the
    // readerstore
    if (!providerRequest.isAsynchronousAllowed()
        && response.getStatus().equals(ProviderResponseStatus.OK)) {
      user.setUsername(response.getEntityId());
      response.setEntity(findById(realm, storage, user.getUsername(), false));
    }
    return response;
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
                  .getOrDefault(
                      GlobalKeysConfig.VERIFY_MAIL_UNICITY,
                      List.of(Boolean.toString(verifyUniqueMail)))
                  .get(0))
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

    ProviderResponse response =
        storeProvider.getWriterStore(realm, storage).updateUser(user, providerRequest);
    if (!providerRequest.isAsynchronousAllowed()
        && response.getStatus().equals(ProviderResponseStatus.OK)) {
      response.setEntity(findById(realm, storage, user.getUsername(), false));
    }
    return response;
  }

  @Override
  public ProviderResponse delete(
      String realmName, String storage, String id, ProviderRequest providerRequest) {
    return storeProvider.getWriterStore(realmName, storage).deleteUser(id, providerRequest);
  }

  @Override
  public User findById(
      String realmName, String storage, String id, boolean externalResolutionAllowed) {
    Realm realm =
        realmProvider.load(realmName).orElseThrow(() -> new RealmNotFoundException(realmName));
    UserStorage userStorage =
        storage == null
            ? realm.getUserStorages().stream()
                .filter(us -> exist(realmName, us.getName(), id))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(realmName, id))
            : realm
                .getUserStorageByName(storage)
                .orElseThrow(() -> new UserStorageNotFoundException(realmName, storage));
    User user =
        storeProvider
            .getReaderStore(realmName, userStorage.getName())
            .getUser(id)
            .orElseThrow(() -> new UserNotFoundException(realmName, userStorage.getName(), id));
    user.addMetadatas(GlobalKeysConfig.REALM.getName(), realmName.toLowerCase());
    user.addMetadatas(GlobalKeysConfig.USERSTORAGE.getName(), userStorage.getName().toLowerCase());
    userStorage
        .getAddUsDefinedAttributesTransformer(GlobalKeysConfig.USER_USERSTORAGE_DEFINED_ATTRIBUTES)
        .accept(user);
    if (externalResolutionAllowed
        && seeAlsoService != null
        && realm.getProperties().containsKey(GlobalKeysConfig.SEEALSO_ATTRIBUTES)) {
      for (String seeAlso : findUserSeeAlsos(realm, user)) {
        seeAlsoService.decorateWithSeeAlso(user, seeAlso);
      }
    }
    return user;
  }

  @Override
  public PageResult<User> findByProperties(
      String realm,
      String storage,
      User userProperties,
      PageableResult pageable,
      SearchType typeRecherche,
      boolean fuzzySearchEnabled) {

    PageResult<User> result = new PageResult<>();
    Realm r = realmProvider.load(realm).orElseThrow(() -> new RealmNotFoundException(realm));
    pageable.setSizeWithMax(
        Integer.parseInt(
            r.getProperties()
                .getOrDefault(
                    GlobalKeysConfig.USERS_MAX_OUTPUT_SIZE,
                    List.of(Integer.toString(usersMaxoutputsize)))
                .get(0)));
    result.setPageSize(pageable.getSize());

    List<String> userStoragesToBrowse =
        storage != null
            ? List.of(storage)
            : r.getUserStorages().stream().map(UserStorage::getName).collect(Collectors.toList());
    for (String usName : userStoragesToBrowse) {
      ReaderStore readerStore = storeProvider.getReaderStore(realm, usName);
      PageResult<User> temResult =
          fuzzySearchEnabled && fuzzySearchAllowed
              ? readerStore.fuzzySearchUsers(userProperties, pageable, typeRecherche.name())
              : readerStore.searchUsers(userProperties, pageable, typeRecherche.name());
      temResult
          .getResults()
          .forEach(
              user -> {
                user.addMetadatas(EventKeysConfig.REALM, realm);
                user.addMetadatas(EventKeysConfig.USERSTORAGE, usName);
              });
      result.getResults().addAll(temResult.getResults());
      result.setTotalElements(
          temResult.getTotalElements() == -1
              ? temResult.getTotalElements()
              : result.getTotalElements() + temResult.getTotalElements());
      result.setSearchToken(temResult.getSearchToken());
      result.setHasMoreResult(temResult.isHasMoreResult());
      if (result.getResults().size() >= result.getPageSize()) {
        return result;
      }
      pageable.setSize(pageable.getSize() - result.getTotalElements());
    }

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
    return storeProvider
        .getWriterStore(realm, storage)
        .addAppManagedAttribute(userId, attributeKey, attribute, providerRequest);
  }

  @Override
  public ProviderResponse deleteAppManagedAttribute(
      String realm,
      String storage,
      String userId,
      String attributeKey,
      String attribute,
      ProviderRequest providerRequest) {
    return storeProvider
        .getWriterStore(realm, storage)
        .deleteAppManagedAttribute(userId, attributeKey, attribute, providerRequest);
  }

  @Override
  public User findByMail(
      String realmName, String storageName, String mail, boolean externalResolutionAllowed) {
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
    user.addMetadatas(GlobalKeysConfig.REALM.getName(), realmName.toLowerCase());
    user.addMetadatas(GlobalKeysConfig.USERSTORAGE.getName(), nonNullStorage.toLowerCase());
    if (externalResolutionAllowed
        && seeAlsoService != null
        && realm.getProperties().containsKey(GlobalKeysConfig.SEEALSO_ATTRIBUTES)) {
      for (String seeAlso : findUserSeeAlsos(realm, user)) {
        seeAlsoService.decorateWithSeeAlso(user, seeAlso);
      }
    }
    return user;
  }

  @Override
  public byte[] getCertificate(String realm, String userStorage, String userId) {
    User user = findById(realm, userStorage, userId, false);
    if (user.getCertificate() == null) {
      throw new NoCertificateOnUserException(realm, userId);
    } else {
      return user.getCertificate();
    }
  }

  @Override
  public ProviderResponse updateCertificate(
      String realm,
      String userStorage,
      String userId,
      byte[] certificat,
      ProviderRequest providerRequest) {
    try {
      User user = findById(realm, userStorage, userId, false);
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
    User user = findById(realm, userStorage, id, false);
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

  public boolean getVerifyUniqueMail() {
    return verifyUniqueMail;
  }

  public void setVerifyUniqueMail(boolean verifyUniqueMail) {
    this.verifyUniqueMail = verifyUniqueMail;
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

  public boolean isFuzzySearchAllowed() {
    return fuzzySearchAllowed;
  }

  public void setFuzzySearchAllowed(boolean fuzzySearchAllowed) {
    this.fuzzySearchAllowed = fuzzySearchAllowed;
  }

  private List<String> findUserSeeAlsos(Realm realm, User user) {
    String[] seeAlsosAttributes =
        realm
            .getProperties()
            .get(GlobalKeysConfig.SEEALSO_ATTRIBUTES)
            .get(0)
            .replace(" ", "")
            .split(",");
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
