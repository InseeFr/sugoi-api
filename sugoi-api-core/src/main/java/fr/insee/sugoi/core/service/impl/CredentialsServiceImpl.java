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
import fr.insee.sugoi.model.RealmConfigKeys;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.exceptions.PasswordPolicyNotMetException;
import fr.insee.sugoi.model.exceptions.RealmNotFoundException;
import fr.insee.sugoi.model.exceptions.UserNoEmailException;
import fr.insee.sugoi.model.exceptions.UserNotFoundException;
import fr.insee.sugoi.model.exceptions.UserStorageNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CredentialsServiceImpl implements CredentialsService {

  @Value("${sugoi.api.event.webhook.mail.secondaryMailAttribute}")
  private String secondaryMailAttribute;

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
    User user = userService.findById(realm, userStorage, userId, false);

    if (user == null || StringUtils.isEmpty(user.getUsername())) {
      throw new UserNotFoundException(realm, userStorage, userId);
    }

    if (computeReceiverMails(templateProperties, user).isEmpty()) {
      throw new UserNoEmailException(realm, userStorage, userId);
    }

    Map<RealmConfigKeys, List<String>> userStorageProperties =
        realmProvider
            .load(realm)
            .orElseThrow(() -> new RealmNotFoundException(realm))
            .getUserStorageByName(userStorage)
            .orElseThrow(() -> new UserStorageNotFoundException(realm, userStorage))
            .getProperties();
    String password =
        passwordService.generatePassword(
            getPasswordBooleanConfiguration(
                userStorageProperties, PasswordPolicyConstants.CREATE_PASSWORD_WITH_UPPERCASE),
            getPasswordBooleanConfiguration(
                userStorageProperties, PasswordPolicyConstants.CREATE_PASSWORD_WITH_LOWERCASE),
            getPasswordBooleanConfiguration(
                userStorageProperties, PasswordPolicyConstants.CREATE_PASSWORD_WITH_DIGITS),
            getPasswordBooleanConfiguration(
                userStorageProperties, PasswordPolicyConstants.CREATE_PASSWORD_WITH_SPECIAL),
            getPasswordIntConfiguration(
                userStorageProperties, PasswordPolicyConstants.CREATE_PASSWORD_SIZE));

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
            Map.entry(EventKeysConfig.MAILS, computeReceiverMails(templateProperties, user)),
            Map.entry(EventKeysConfig.USER, user),
            Map.entry(EventKeysConfig.WEBSERVICE_TAG, webserviceTag != null ? webserviceTag : ""),
            Map.entry(EventKeysConfig.PWD, password)));
    return response;
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

    User user = userService.findById(realm, userStorage, userId, false);

    Map<RealmConfigKeys, List<String>> userStorageProperties =
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
              Map.entry(EventKeysConfig.NEW_PWD, newPassword),
              Map.entry(EventKeysConfig.OLD_PWD, newPassword),
              Map.entry(EventKeysConfig.USER_ID, userId),
              Map.entry(EventKeysConfig.PROPERTIES, templateProperties),
              Map.entry(EventKeysConfig.MAILS, computeReceiverMails(templateProperties, user)),
              Map.entry(
                  EventKeysConfig.WEBSERVICE_TAG, webserviceTag != null ? webserviceTag : "")));
      return providerResponse;
    } else {
      throw new PasswordPolicyNotMetException("New password is not valid");
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

    Map<RealmConfigKeys, List<String>> realmProperties =
        realmProvider
            .load(realm)
            .orElseThrow(() -> new RealmNotFoundException(realm))
            .getUserStorageByName(userStorage)
            .orElseThrow(() -> new UserStorageNotFoundException(realm, userStorage))
            .getProperties();

    boolean newPasswordIsValid = validatePassword(newPassword, realmProperties);
    if (newPasswordIsValid) {
      return storeProvider
          .getWriterStore(realm, userStorage)
          .initPassword(userId, newPassword, changePasswordResetStatus, providerRequest);
    } else {
      throw new PasswordPolicyNotMetException("New password is not valid");
    }
  }

  @Override
  public boolean validateCredential(
      String realm, String userStorage, String userName, String password) {
    User user = userService.findById(realm, userStorage, userName, false);
    return storeProvider.getReaderStore(realm, userStorage).validateCredentials(user, password);
  }

  @Override
  public boolean sendLogin(
      String realm,
      String userStorage,
      String id,
      Map<String, String> templateProperties,
      String webhookTag) {
    try {
      User user = userService.findById(realm, userStorage, id, false);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          userStorage,
          SugoiEventTypeEnum.SEND_LOGIN,
          Map.ofEntries(
              Map.entry(EventKeysConfig.MAILS, computeReceiverMails(templateProperties, user)),
              Map.entry(EventKeysConfig.WEBSERVICE_TAG, webhookTag != null ? webhookTag : ""),
              Map.entry(EventKeysConfig.USER, user),
              Map.entry(EventKeysConfig.PROPERTIES, templateProperties)));
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private List<String> computeReceiverMails(Map<String, String> templateProperties, User user) {
    if (templateProperties.containsKey("mail")
        && StringUtils.isNotBlank(templateProperties.get("mail"))) {
      return List.of(templateProperties.get("mail"));
    }
    List<String> mails = new ArrayList<>();
    if (StringUtils.isNotBlank(user.getMail())) mails.add(user.getMail());
    if (StringUtils.isNotBlank(secondaryMailAttribute)
        && user.getAttributes().containsKey(secondaryMailAttribute)) {
      ((List<?>) user.getAttributes().get(secondaryMailAttribute))
          .forEach(mail -> mails.add((String) mail));
    }
    return mails;
  }

  private Boolean validatePassword(
      String password, Map<RealmConfigKeys, List<String>> userStorageProperties) {
    return passwordService.validatePassword(
        password,
        getPasswordBooleanConfiguration(
            userStorageProperties, PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_UPPERCASE),
        getPasswordBooleanConfiguration(
            userStorageProperties, PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_LOWERCASE),
        getPasswordBooleanConfiguration(
            userStorageProperties, PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_DIGITS),
        getPasswordBooleanConfiguration(
            userStorageProperties, PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_SPECIAL),
        getPasswordIntConfiguration(
            userStorageProperties, PasswordPolicyConstants.VALIDATE_PASSWORD_MIN_SIZE));
  }

  private Boolean getPasswordBooleanConfiguration(
      Map<RealmConfigKeys, List<String>> userStorageProperties, RealmConfigKeys configkey) {
    return (userStorageProperties.containsKey(configkey)
            && userStorageProperties.get(configkey) != null
            && !userStorageProperties.get(configkey).isEmpty())
        ? Boolean.parseBoolean(userStorageProperties.get(configkey).get(0))
        : null;
  }

  private Integer getPasswordIntConfiguration(
      Map<RealmConfigKeys, List<String>> userStorageProperties, RealmConfigKeys configkey) {
    return (userStorageProperties.containsKey(configkey)
            && userStorageProperties.get(configkey) != null
            && !userStorageProperties.get(configkey).isEmpty())
        ? Integer.parseInt(userStorageProperties.get(configkey).get(0))
        : null;
  }
}
