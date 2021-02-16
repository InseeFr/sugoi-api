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
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  @Autowired private StoreProvider storeProvider;

  @Autowired private SugoiEventPublisher sugoiEventPublisher;

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
    sugoiEventPublisher.publishCustomEvent(
        realmName,
        storage,
        SugoiEventTypeEnum.FIND_USER_BY_ID,
        Map.ofEntries(Map.entry("userId", id)));
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
        realm,
        storage,
        SugoiEventTypeEnum.FIND_USERS,
        Map.ofEntries(
            Map.entry("userProperties", userProperties),
            Map.entry("pageable", pageable),
            Map.entry("typeRecherche", typeRecherche)));
    try {
      return storeProvider
          .getReaderStore(realm, storage)
          .searchUsers(userProperties, pageable, typeRecherche.name());
    } catch (Exception e) {
      throw new RuntimeException("Erreur lors de la récupération des utilisateurs", e);
    }
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
