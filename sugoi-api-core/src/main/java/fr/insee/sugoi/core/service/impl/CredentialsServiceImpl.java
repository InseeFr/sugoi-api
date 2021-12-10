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
import fr.insee.sugoi.core.exceptions.PasswordPolicyNotMetException;
import fr.insee.sugoi.core.exceptions.UserNotFoundException;
import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.core.service.CredentialsService;
import fr.insee.sugoi.core.service.PasswordService;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.PasswordChangeRequest;
import fr.insee.sugoi.model.PasswordPolicyConstants;
import fr.insee.sugoi.model.User;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CredentialsServiceImpl implements CredentialsService {

  @Autowired private StoreProvider storeProvider;

  @Autowired private PasswordService passwordService;

  @Autowired private SugoiEventPublisher sugoiEventPublisher;

  @Autowired private ConfigService configService;

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

      Map<String, String> realmProperties = configService.getRealm(realm).getProperties();

      String password =
          passwordService.generatePassword(
              realmProperties.get(PasswordPolicyConstants.CREATE_PASSWORD_WITH_UPPERCASE) != null
                  ? Boolean.parseBoolean(PasswordPolicyConstants.CREATE_PASSWORD_WITH_UPPERCASE)
                  : null,
              realmProperties.get(PasswordPolicyConstants.CREATE_PASSWORD_WITH_LOWERCASE) != null
                  ? Boolean.parseBoolean(
                      realmProperties.get(PasswordPolicyConstants.CREATE_PASSWORD_WITH_LOWERCASE))
                  : null,
              realmProperties.get(PasswordPolicyConstants.CREATE_PASSWORD_WITH_DIGITS) != null
                  ? Boolean.parseBoolean(
                      realmProperties.get(PasswordPolicyConstants.CREATE_PASSWORD_WITH_DIGITS))
                  : null,
              realmProperties.get(PasswordPolicyConstants.CREATE_PASSWORD_WITH_SPECIAL) != null
                  ? Boolean.parseBoolean(
                      realmProperties.get(PasswordPolicyConstants.CREATE_PASSWORD_WITH_SPECIAL))
                  : null,
              realmProperties.get(PasswordPolicyConstants.CREATE_PASSWORD_SIZE) != null
                  ? Integer.parseInt(
                      realmProperties.get(PasswordPolicyConstants.CREATE_PASSWORD_SIZE))
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
          SugoiEventTypeEnum.RESET_PASSWORD,
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
      ProviderRequest providerRequest) {
    try {

      Map<String, String> realmProperties = configService.getRealm(realm).getProperties();

      boolean newPasswordIsValid = validatePassword(newPassword, realmProperties);

      if (newPasswordIsValid) {
        ProviderResponse providerResponse =
            storeProvider
                .getWriterStore(realm, userStorage)
                .changePassword(userId, oldPassword, newPassword, providerRequest);
        sugoiEventPublisher.publishCustomEvent(
            realm,
            userStorage,
            SugoiEventTypeEnum.CHANGE_PASSWORD,
            Map.ofEntries(
                Map.entry(EventKeysConfig.NEW_PASSWORD, newPassword),
                Map.entry(EventKeysConfig.OLD_PASSWORD, newPassword),
                Map.entry(EventKeysConfig.USER, userId)));
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
      PasswordChangeRequest pcr,
      ProviderRequest providerRequest) {
    try {

      Map<String, String> realmProperties = configService.getRealm(realm).getProperties();

      boolean newPasswordIsValid = validatePassword(pcr.getNewPassword(), realmProperties);
      if (newPasswordIsValid) {
        ProviderResponse response =
            storeProvider
                .getWriterStore(realm, userStorage)
                .initPassword(userId, pcr.getNewPassword(), pcr, providerRequest);
        sugoiEventPublisher.publishCustomEvent(
            realm,
            userStorage,
            SugoiEventTypeEnum.INIT_PASSWORD,
            Map.ofEntries(
                Map.entry(EventKeysConfig.PASSWORD_CHANGE_REQUEST, pcr),
                Map.entry(EventKeysConfig.USER_ID, userId),
                Map.entry(EventKeysConfig.PASSWORD, pcr.getNewPassword())));
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
              Map.entry(EventKeysConfig.PASSWORD_CHANGE_REQUEST, pcr),
              Map.entry(EventKeysConfig.USER_ID, userId),
              Map.entry(EventKeysConfig.PASSWORD, pcr.getNewPassword()),
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

  private Boolean validatePassword(String password, Map<String, String> realmProperties) {
    return passwordService.validatePassword(
        password,
        realmProperties.get(PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_UPPERCASE) != null
            ? Boolean.parseBoolean(
                realmProperties.get(PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_UPPERCASE))
            : null,
        realmProperties.get(PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_LOWERCASE) != null
            ? Boolean.parseBoolean(
                realmProperties.get(PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_LOWERCASE))
            : null,
        realmProperties.get(PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_DIGITS) != null
            ? Boolean.parseBoolean(
                realmProperties.get(PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_DIGITS))
            : null,
        realmProperties.get(PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_SPECIAL) != null
            ? Boolean.parseBoolean(
                realmProperties.get(PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_SPECIAL))
            : null,
        realmProperties.get(PasswordPolicyConstants.VALIDATE_PASSWORD_MIN_SIZE) != null
            ? Integer.parseInt(
                realmProperties.get(PasswordPolicyConstants.VALIDATE_PASSWORD_MIN_SIZE))
            : null);
  }
}
