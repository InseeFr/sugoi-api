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

import fr.insee.sugoi.core.event.configuration.EventKeysConfig;
import fr.insee.sugoi.core.event.model.SugoiEventTypeEnum;
import fr.insee.sugoi.core.event.publisher.SugoiEventPublisher;
import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.CredentialsService;
import fr.insee.sugoi.core.service.PasswordService;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.PasswordPolicyConstants;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.exceptions.PasswordPolicyNotMetException;
import fr.insee.sugoi.model.exceptions.RealmNotFoundException;
import fr.insee.sugoi.model.exceptions.UserNoEmailException;
import fr.insee.sugoi.model.exceptions.UserNotFoundException;
import fr.insee.sugoi.model.exceptions.UserStorageNotFoundException;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CredentialsServiceImpl implements CredentialsService {

  @Autowired private StoreProvider storeProvider;

  @Autowired private PasswordService passwordService;

  @Autowired private SugoiEventPublisher sugoiEventPublisher;

  @Autowired private RealmProvider realmProvider;

  @Autowired private UserService userService;

  @Override
  public ProviderResponse reinitPassword(
      String realm,
      String userStorage,
      String userId,
      Map<String, String> templateProperties,
      String webserviceTag,
      boolean changePasswordResetStatus,
      ProviderRequest providerRequest) {
    try {
      User user = userService.findById(realm, userStorage, userId);

      if (user == null || StringUtils.isEmpty(user.getUsername())) {
        throw new UserNotFoundException(realm, userStorage, userId);
      }

      if (StringUtils.isEmpty(computeReceiverMail(templateProperties, user.getMail()))) {
        throw new UserNoEmailException(realm, userStorage, userId);
      }
      Map<String, String> userStorageProperties =
          realmProvider
              .load(realm)
              .orElseThrow(() -> new RealmNotFoundException(realm))
              .getUserStorageByName(userStorage)
              .orElseThrow(() -> new UserStorageNotFoundException(realm, userStorage))
              .getProperties();

      String password =
          passwordService.generatePassword(
              userStorageProperties.get(PasswordPolicyConstants.CREATE_PASSWORD_WITH_UPPERCASE)
                      != null
                  ? Boolean.parseBoolean(PasswordPolicyConstants.CREATE_PASSWORD_WITH_UPPERCASE)
                  : null,
              userStorageProperties.get(PasswordPolicyConstants.CREATE_PASSWORD_WITH_LOWERCASE)
                      != null
                  ? Boolean.parseBoolean(
                      userStorageProperties.get(
                          PasswordPolicyConstants.CREATE_PASSWORD_WITH_LOWERCASE))
                  : null,
              userStorageProperties.get(PasswordPolicyConstants.CREATE_PASSWORD_WITH_DIGITS) != null
                  ? Boolean.parseBoolean(
                      userStorageProperties.get(
                          PasswordPolicyConstants.CREATE_PASSWORD_WITH_DIGITS))
                  : null,
              userStorageProperties.get(PasswordPolicyConstants.CREATE_PASSWORD_WITH_SPECIAL)
                      != null
                  ? Boolean.parseBoolean(
                      userStorageProperties.get(
                          PasswordPolicyConstants.CREATE_PASSWORD_WITH_SPECIAL))
                  : null,
              userStorageProperties.get(PasswordPolicyConstants.CREATE_PASSWORD_SIZE) != null
                  ? Integer.parseInt(
                      userStorageProperties.get(PasswordPolicyConstants.CREATE_PASSWORD_SIZE))
                  : null);

      ProviderResponse response =
          storeProvider
              .getWriterStore(realm, userStorage)
              .reinitPassword(
                  userId,
                  password,
                  changePasswordResetStatus,
                  templateProperties,
                  webserviceTag,
                  providerRequest);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          userStorage,
          SugoiEventTypeEnum.REINIT_PASSWORD,
          Map.ofEntries(
              Map.entry(EventKeysConfig.PROPERTIES, templateProperties),
              Map.entry(
                  EventKeysConfig.MAIL, computeReceiverMail(templateProperties, user.getMail())),
              Map.entry(EventKeysConfig.USER, user),
              Map.entry(EventKeysConfig.WEBSERVICE_TAG, webserviceTag != null ? webserviceTag : ""),
              Map.entry(EventKeysConfig.PASSWORD, password)));
      return response;
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          userStorage,
          SugoiEventTypeEnum.RESET_PASSWORD_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.USER_ID, userId),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      throw e;
    }
  }

  @Override
  public ProviderResponse changePassword(
      String realm,
      String userStorage,
      String userId,
      String oldPassword,
      String newPassword,
      String webserviceTag,
      Map<String, String> templateProperties,
      ProviderRequest providerRequest) {
    try {

      User user = userService.findById(realm, userStorage, userId);

      Map<String, String> userStorageProperties =
          realmProvider
              .load(realm)
              .orElseThrow(() -> new RealmNotFoundException(realm))
              .getUserStorageByName(userStorage)
              .orElseThrow(() -> new UserStorageNotFoundException(realm, userStorage))
              .getProperties();

      boolean newPasswordIsValid = validatePassword(newPassword, userStorageProperties);

      if (newPasswordIsValid) {
        ProviderResponse providerResponse =
            storeProvider
                .getWriterStore(realm, userStorage)
                .changePassword(
                    userId,
                    oldPassword,
                    newPassword,
                    webserviceTag,
                    templateProperties,
                    providerRequest);
        sugoiEventPublisher.publishCustomEvent(
            realm,
            userStorage,
            SugoiEventTypeEnum.CHANGE_PASSWORD,
            Map.ofEntries(
                Map.entry(EventKeysConfig.NEW_PASSWORD, newPassword),
                Map.entry(EventKeysConfig.OLD_PASSWORD, newPassword),
                Map.entry(EventKeysConfig.USER_ID, userId),
                Map.entry(EventKeysConfig.PROPERTIES, templateProperties),
                Map.entry(
                    EventKeysConfig.MAIL, computeReceiverMail(templateProperties, user.getMail())),
                Map.entry(
                    EventKeysConfig.WEBSERVICE_TAG, webserviceTag != null ? webserviceTag : "")));
        return providerResponse;
      } else {
        throw new PasswordPolicyNotMetException("New password is not valid");
      }
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          userStorage,
          SugoiEventTypeEnum.CHANGE_PASSWORD_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.ERROR, e.toString()),
              Map.entry(EventKeysConfig.USER_ID, userId)));
      throw e;
    }
  }

  @Override
  public ProviderResponse initPassword(
      String realm,
      String userStorage,
      String userId,
      String newPassword,
      boolean changePasswordResetStatus,
      ProviderRequest providerRequest) {
    try {

      Map<String, String> realmProperties =
          realmProvider
              .load(realm)
              .orElseThrow(() -> new RealmNotFoundException(realm))
              .getUserStorageByName(userStorage)
              .orElseThrow(() -> new UserStorageNotFoundException(realm, userStorage))
              .getProperties();

      boolean newPasswordIsValid = validatePassword(newPassword, realmProperties);
      if (newPasswordIsValid) {
        ProviderResponse response =
            storeProvider
                .getWriterStore(realm, userStorage)
                .initPassword(userId, newPassword, changePasswordResetStatus, providerRequest);
        sugoiEventPublisher.publishCustomEvent(
            realm,
            userStorage,
            SugoiEventTypeEnum.INIT_PASSWORD,
            Map.ofEntries(Map.entry(EventKeysConfig.USER_ID, userId)));
        return response;
      } else {
        throw new PasswordPolicyNotMetException("New password is not valid");
      }
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          userStorage,
          SugoiEventTypeEnum.INIT_PASSWORD_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.USER_ID, userId),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      throw e;
    }
  }

  @Override
  public boolean validateCredential(
      String realm, String userStorage, String userName, String password) {
    try {
      User user = userService.findById(realm, userStorage, userName);
      boolean valid =
          storeProvider.getReaderStore(realm, userStorage).validateCredentials(user, password);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          userStorage,
          SugoiEventTypeEnum.VALIDATE_CREDENTIAL,
          Map.ofEntries(Map.entry(EventKeysConfig.USER, user)));
      return valid;
    } catch (Exception e) {
      sugoiEventPublisher.publishCustomEvent(
          realm,
          userStorage,
          SugoiEventTypeEnum.VALIDATE_CREDENTIAL_ERROR,
          Map.ofEntries(
              Map.entry(EventKeysConfig.USER, userName),
              Map.entry(EventKeysConfig.ERROR, e.toString())));
      if (e instanceof UserNotFoundException) {
        throw (UserNotFoundException) e;
      } else {
        throw e;
      }
    }
  }

  @Override
  public boolean sendLogin(
      String realm,
      String userStorage,
      String id,
      Map<String, String> templateProperties,
      String webhookTag) {
    try {
      User user = userService.findById(realm, userStorage, id);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          userStorage,
          SugoiEventTypeEnum.SEND_LOGIN,
          Map.ofEntries(
              Map.entry(
                  EventKeysConfig.MAIL, computeReceiverMail(templateProperties, user.getMail())),
              Map.entry(EventKeysConfig.WEBSERVICE_TAG, webhookTag != null ? webhookTag : ""),
              Map.entry(EventKeysConfig.USER, user),
              Map.entry(EventKeysConfig.PROPERTIES, templateProperties)));
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private String computeReceiverMail(Map<String, String> templateProperties, String userMail) {
    if (templateProperties.containsKey("mail")
        && templateProperties.get("mail") != null
        && !templateProperties.get("mail").isBlank()) {
      return templateProperties.get("mail");
    } else if (userMail != null) {
      return userMail;
    } else {
      return "";
    }
  }

  private Boolean validatePassword(String password, Map<String, String> userStorageProperties) {
    return passwordService.validatePassword(
        password,
        userStorageProperties.get(PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_UPPERCASE) != null
            ? Boolean.parseBoolean(
                userStorageProperties.get(PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_UPPERCASE))
            : null,
        userStorageProperties.get(PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_LOWERCASE) != null
            ? Boolean.parseBoolean(
                userStorageProperties.get(PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_LOWERCASE))
            : null,
        userStorageProperties.get(PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_DIGITS) != null
            ? Boolean.parseBoolean(
                userStorageProperties.get(PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_DIGITS))
            : null,
        userStorageProperties.get(PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_SPECIAL) != null
            ? Boolean.parseBoolean(
                userStorageProperties.get(PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_SPECIAL))
            : null,
        userStorageProperties.get(PasswordPolicyConstants.VALIDATE_PASSWORD_MIN_SIZE) != null
            ? Integer.parseInt(
                userStorageProperties.get(PasswordPolicyConstants.VALIDATE_PASSWORD_MIN_SIZE))
            : null);
  }
}
