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
import fr.insee.sugoi.core.exceptions.PasswordPolicyNotMetException;
import fr.insee.sugoi.core.model.PasswordChangeRequest;
import fr.insee.sugoi.core.model.SendMode;
import fr.insee.sugoi.core.service.CredentialsService;
import fr.insee.sugoi.core.service.PasswordService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.model.User;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CredentialsServiceImpl implements CredentialsService {

  @Autowired private StoreProvider storeProvider;

  @Autowired private PasswordService passwordService;

  @Autowired private SugoiEventPublisher sugoiEventPublisher;

  @Override
  public void reinitPassword(
      String realm,
      String userStorage,
      String userId,
      PasswordChangeRequest pcr,
      List<SendMode> sendMode) {
    User user = storeProvider.getReaderStore(realm, userStorage).getUser(userId);
    String password = passwordService.generatePassword();
    WriterStore writerStore = storeProvider.getWriterStore(realm, userStorage);
    writerStore.reinitPassword(user, password, pcr, sendMode);
    writerStore.changePasswordResetStatus(
        user,
        (pcr.getProperties() != null && pcr.getProperties().get("mustChangePassword") != null)
            ? Boolean.parseBoolean(pcr.getProperties().get("mustChangePassword"))
            : false);
    sugoiEventPublisher.publishCustomEvent(
        realm,
        userStorage,
        SugoiEventTypeEnum.RESET_PASSWORD,
        Map.ofEntries(
            Map.entry("pcr", pcr),
            Map.entry("user", user),
            Map.entry("password", password),
            Map.entry("sendModes", sendMode)));
  }

  @Override
  public void changePassword(
      String realm, String userStorage, String userId, PasswordChangeRequest pcr) {
    User user = storeProvider.getReaderStore(realm, userStorage).getUser(userId);
    sugoiEventPublisher.publishCustomEvent(
        realm,
        userStorage,
        SugoiEventTypeEnum.CHANGE_PASSWORD,
        Map.ofEntries(Map.entry("pcr", pcr), Map.entry("user", user)));
    boolean newPasswordIsValid = passwordService.validatePassword(pcr.getNewPassword());
    if (newPasswordIsValid) {
      storeProvider
          .getWriterStore(realm, userStorage)
          .changePassword(user, pcr.getOldPassword(), pcr.getNewPassword(), pcr);
    } else {
      throw new PasswordPolicyNotMetException("New password is not valid");
    }
  }

  @Override
  public void initPassword(
      String realm,
      String userStorage,
      String userId,
      PasswordChangeRequest pcr,
      List<SendMode> sendMode) {
    boolean newPasswordIsValid = passwordService.validatePassword(pcr.getNewPassword());
    if (newPasswordIsValid) {
      User user = storeProvider.getReaderStore(realm, userStorage).getUser(userId);
      storeProvider
          .getWriterStore(realm, userStorage)
          .initPassword(user, pcr.getNewPassword(), pcr, sendMode);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          userStorage,
          SugoiEventTypeEnum.INIT_PASSWORD,
          Map.ofEntries(
              Map.entry("pcr", pcr),
              Map.entry("sendModes", sendMode),
              Map.entry("user", user),
              Map.entry("password", pcr.getNewPassword())));
    } else {
      throw new PasswordPolicyNotMetException("New password is not valid");
    }
  }

  @Override
  public boolean validateCredential(
      String realm, String userStorage, String userName, String password) {
    User user = storeProvider.getReaderStore(realm, userStorage).getUser(userName);
    sugoiEventPublisher.publishCustomEvent(
        realm,
        userStorage,
        SugoiEventTypeEnum.VALIDATE_CREDENTIAL,
        Map.ofEntries(Map.entry("user", user)));
    return storeProvider.getReaderStore(realm, userStorage).validateCredentials(user, password);
  }
}
