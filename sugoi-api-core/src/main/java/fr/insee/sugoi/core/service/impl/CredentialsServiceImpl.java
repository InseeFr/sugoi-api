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

import fr.insee.sugoi.core.exceptions.InvalidPasswordException;
import fr.insee.sugoi.core.model.PasswordChangeRequest;
import fr.insee.sugoi.core.service.CredentialsService;
import fr.insee.sugoi.core.service.PasswordService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CredentialsServiceImpl implements CredentialsService {

  @Autowired
  private StoreProvider storeProvider;

  @Autowired
  private PasswordService passwordService;

  @Override
  public void reinitPassword(String realm, String userStorage, String userId, PasswordChangeRequest pcr) {
    User user = storeProvider.getReaderStore(realm, userStorage).getUser(userId);
    String password = passwordService.generatePassword();
    WriterStore writerStore = storeProvider.getWriterStore(realm, userStorage);
    writerStore.reinitPassword(user, password);
    writerStore.changePasswordResetStatus(user, true);
  }

  @Override
  public void changePassword(String realm, String userStorage, String userId, PasswordChangeRequest pcr) {
    User user = storeProvider.getReaderStore(realm, userStorage).getUser(userId);
    boolean newPasswordIsValid = passwordService.validatePassword(pcr.getNewPassword());
    if (newPasswordIsValid) {
      storeProvider.getWriterStore(realm, userStorage).changePassword(user, pcr.getOldPassword(), pcr.getNewPassword());
    }
    throw new InvalidPasswordException("New password is not valid");
  }

  @Override
  public void initPassword(String realm, String userStorage, String userId, PasswordChangeRequest pcr) {
    User user = storeProvider.getReaderStore(realm, userStorage).getUser(userId);
    storeProvider.getWriterStore(realm, userStorage).initPassword(user, pcr.getNewPassword());
  }
}