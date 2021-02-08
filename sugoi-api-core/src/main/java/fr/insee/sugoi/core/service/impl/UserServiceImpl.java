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
import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.model.SearchType;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  @Autowired private StoreProvider storeProvider;

  @Autowired private SugoiEventPublisher sugoiEventPublisher;

  @Override
  public User create(String realm, String storage, User user) {
    sugoiEventPublisher.publishCustomEvent(realm, storage, SugoiEventTypeEnum.CREATE_USER, user);
    return storeProvider.getWriterStore(realm, storage).createUser(user);
  }

  @Override
  public void update(String realm, String storage, User user) {
    sugoiEventPublisher.publishCustomEvent(realm, storage, SugoiEventTypeEnum.UPDATE_USER, user);
    storeProvider.getWriterStore(realm, storage).updateUser(user);
  }

  @Override
  public void delete(String realmName, String storage, String id) {
    sugoiEventPublisher.publishCustomEvent(realmName, storage, SugoiEventTypeEnum.DELETE_USER, id);
    storeProvider.getWriterStore(realmName, storage).deleteUser(id);
  }

  @Override
  public User findById(String realmName, String storage, String id) {
    sugoiEventPublisher.publishCustomEvent(
        realmName, storage, SugoiEventTypeEnum.FIND_USER_BY_ID, id);
    try {
      return storeProvider.getReaderStore(realmName, storage).getUser(id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PageResult<User> findByProperties(
      String realm,
      String storage,
      User userProperties,
      PageableResult pageable,
      SearchType typeRecherche) {
    sugoiEventPublisher.publishCustomEvent(
        realm, storage, SugoiEventTypeEnum.FIND_USERS, userProperties);
    try {
      return storeProvider
          .getReaderStore(realm, storage)
          .searchUsers(userProperties, pageable, typeRecherche.name());
    } catch (Exception e) {
      throw new RuntimeException("Erreur lors de la récupération des utilisateurs", e);
    }
  }
}
