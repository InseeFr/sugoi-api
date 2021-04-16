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
import fr.insee.sugoi.core.exceptions.GroupAlreadyExistException;
import fr.insee.sugoi.core.exceptions.GroupNotCreatedException;
import fr.insee.sugoi.core.exceptions.GroupNotFoundException;
import fr.insee.sugoi.core.exceptions.UserNotFoundException;
import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.model.SearchType;
import fr.insee.sugoi.core.service.GroupService;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.User;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupServiceImpl implements GroupService {

  @Autowired private StoreProvider storeProvider;

  @Autowired private SugoiEventPublisher sugoiEventPublisher;

  @Autowired private UserService userService;

  @Override
  public Group create(String realm, String appName, Group group) {
    if (!findById(realm, appName, group.getName()).isPresent()) {
      storeProvider.getWriterStore(realm).createGroup(appName, group);
      sugoiEventPublisher.publishCustomEvent(
          realm,
          null,
          SugoiEventTypeEnum.CREATE_GROUP,
          Map.ofEntries(
              Map.entry(EventKeysConfig.GROUP, group),
              Map.entry(EventKeysConfig.APPLICATION_NAME, appName)));
      return findById(realm, appName, group.getName())
          .orElseThrow(
              () ->
                  new GroupNotCreatedException(
                      "Cannot find group " + group + " in app " + appName + " in realm " + realm));
    }
    throw new GroupAlreadyExistException(
        "Group " + group.getName() + " already exist in " + appName + " in realm " + realm);
  }

  @Override
  public void delete(String realm, String appName, String id) {
    findById(realm, appName, id)
        .orElseThrow(
            () ->
                new GroupNotFoundException(
                    "Cannot find group " + id + " in app " + appName + " in realm " + realm));
    storeProvider.getWriterStore(realm).deleteGroup(appName, id);
    sugoiEventPublisher.publishCustomEvent(
        realm,
        null,
        SugoiEventTypeEnum.DELETE_GROUP,
        Map.ofEntries(Map.entry(EventKeysConfig.GROUP_ID, id)));
  }

  @Override
  public Optional<Group> findById(String realm, String appName, String id) {
    Group group = storeProvider.getReaderStore(realm).getGroup(appName, id);
    sugoiEventPublisher.publishCustomEvent(
        realm,
        null,
        SugoiEventTypeEnum.FIND_GROUP_BY_ID,
        Map.ofEntries(
            Map.entry(EventKeysConfig.GROUP_ID, id),
            Map.entry(EventKeysConfig.APPLICATION_NAME, appName)));
    return Optional.ofNullable(group);
  }

  @Override
  public PageResult<Group> findByProperties(
      String realm, String appName, Group groupFilter, PageableResult pageableResult) {
    PageResult<Group> groups =
        storeProvider
            .getReaderStore(realm)
            .searchGroups(appName, groupFilter, pageableResult, SearchType.AND.name());
    sugoiEventPublisher.publishCustomEvent(
        realm,
        null,
        SugoiEventTypeEnum.FIND_GROUPS,
        Map.ofEntries(
            Map.entry(EventKeysConfig.APPLICATION_NAME, appName),
            Map.entry(EventKeysConfig.GROUP_FILTER, groupFilter)));
    return groups;
  }

  @Override
  public void update(String realm, String appName, Group group) {
    findById(realm, appName, group.getName())
        .orElseThrow(
            () ->
                new GroupNotFoundException(
                    "Cannot find group "
                        + group.getName()
                        + " in app "
                        + appName
                        + " in realm "
                        + realm));
    storeProvider.getWriterStore(realm).updateGroup(appName, group);
    sugoiEventPublisher.publishCustomEvent(
        realm,
        null,
        SugoiEventTypeEnum.UPDATE_GROUP,
        Map.ofEntries(
            Map.entry(EventKeysConfig.GROUP, group),
            Map.entry(EventKeysConfig.APPLICATION_NAME, appName)));
  }

  @Override
  public void addUserToGroup(String realm, String userId, String appName, String groupName) {
    findById(realm, appName, groupName)
        .orElseThrow(
            () ->
                new GroupNotFoundException(
                    "Cannot find group "
                        + groupName
                        + " in app "
                        + appName
                        + " in realm "
                        + realm));
    User user =
        userService
            .findById(realm, null, userId)
            .orElseThrow(
                () ->
                    new UserNotFoundException(
                        "Cannot find user with id " + userId + " in realm " + realm));
    storeProvider
        .getWriterStore(realm, (String) user.getMetadatas().get(EventKeysConfig.USERSTORAGE))
        .addUserToGroup(appName, groupName, userId);
    sugoiEventPublisher.publishCustomEvent(
        realm,
        (String) user.getMetadatas().get(EventKeysConfig.USERSTORAGE),
        SugoiEventTypeEnum.ADD_USER_TO_GROUP,
        Map.ofEntries(
            Map.entry(EventKeysConfig.USER, userId),
            Map.entry(EventKeysConfig.APPLICATION_NAME, appName),
            Map.entry(EventKeysConfig.GROUP_NAME, groupName)));
  }

  @Override
  public void deleteUserFromGroup(String realm, String userId, String appName, String groupName) {
    findById(realm, appName, groupName)
        .orElseThrow(
            () ->
                new GroupNotFoundException(
                    "Cannot find group "
                        + groupName
                        + " in app "
                        + appName
                        + " in realm "
                        + realm));
    User user =
        userService
            .findById(realm, null, userId)
            .orElseThrow(
                () ->
                    new UserNotFoundException(
                        "Cannot find user with id " + userId + " in realm " + realm));
    storeProvider
        .getWriterStore(realm, (String) user.getMetadatas().get(EventKeysConfig.USERSTORAGE))
        .deleteUserFromGroup(appName, groupName, userId);
    sugoiEventPublisher.publishCustomEvent(
        realm,
        (String) user.getMetadatas().get(EventKeysConfig.USERSTORAGE),
        SugoiEventTypeEnum.DELETE_USER_FROM_GROUP,
        Map.ofEntries(
            Map.entry(EventKeysConfig.USER, userId),
            Map.entry(EventKeysConfig.APPLICATION_NAME, appName),
            Map.entry(EventKeysConfig.GROUP_NAME, groupName)));
  }
}
