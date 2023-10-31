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
import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.model.ProviderResponse.ProviderResponseStatus;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.GroupService;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.exceptions.GroupNotFoundException;
import fr.insee.sugoi.model.exceptions.ManagerGroupNotFoundException;
import fr.insee.sugoi.model.exceptions.RealmNotFoundException;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import fr.insee.sugoi.model.paging.SearchType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupServiceImpl implements GroupService {

  @Autowired private StoreProvider storeProvider;

  @Autowired private UserService userService;

  @Autowired private RealmProvider realmProvider;

  @Override
  public ProviderResponse create(
      String realm, String appName, Group group, ProviderRequest providerRequest) {
    ProviderResponse response =
        storeProvider.getWriterStore(realm).createGroup(appName, group, providerRequest);
    if (!providerRequest.isAsynchronousAllowed()
        && response.getStatus().equals(ProviderResponseStatus.OK)) {
      response.setEntity(findById(realm, appName, group.getName()));
    }
    return response;
  }

  @Override
  public ProviderResponse delete(
      String realm, String appName, String id, ProviderRequest providerRequest) {
    return storeProvider.getWriterStore(realm).deleteGroup(appName, id, providerRequest);
  }

  @Override
  public Group findById(String realm, String appName, String id) {
    return storeProvider
        .getReaderStore(realm)
        .getGroup(appName, id)
        .orElseThrow(() -> new GroupNotFoundException(realm, appName, id));
  }

  @Override
  public PageResult<Group> findByProperties(
      String realm, String appName, Group groupFilter, PageableResult pageableResult) {
    Realm r = realmProvider.load(realm).orElseThrow(() -> new RealmNotFoundException(realm));
    pageableResult.setSizeWithMax(
        Integer.parseInt(r.getProperties().get(GlobalKeysConfig.GROUPS_MAX_OUTPUT_SIZE).get(0)));
    PageResult<Group> groups =
        storeProvider
            .getReaderStore(realm)
            .searchGroups(appName, groupFilter, pageableResult, SearchType.AND.name());
    return groups;
  }

  @Override
  public ProviderResponse update(
      String realm, String appName, Group group, ProviderRequest providerRequest) {
    ProviderResponse response =
        storeProvider.getWriterStore(realm).updateGroup(appName, group, providerRequest);
    if (!providerRequest.isAsynchronousAllowed()
        && response.getStatus().equals(ProviderResponseStatus.OK)) {
      response.setEntity(findById(realm, appName, group.getName()));
    }
    return response;
  }

  @Override
  public ProviderResponse addUserToGroup(
      String realm,
      String storage,
      String userId,
      String appName,
      String groupName,
      ProviderRequest providerRequest) {
    if (storage == null) {
      User user = userService.findById(realm, null, userId, false);
      storage = (String) user.getMetadatas().get(GlobalKeysConfig.USERSTORAGE.getName());
    }
    return storeProvider
        .getWriterStore(realm, storage)
        .addUserToGroup(appName, groupName, userId, providerRequest);
  }

  @Override
  public ProviderResponse deleteUserFromGroup(
      String realm,
      String storage,
      String userId,
      String appName,
      String groupName,
      ProviderRequest providerRequest) {
    if (storage == null) {
      User user = userService.findById(realm, null, userId, false);
      storage = (String) user.getMetadatas().get(GlobalKeysConfig.USERSTORAGE.getName());
    }
    return storeProvider
        .getWriterStore(realm, storage)
        .deleteUserFromGroup(appName, groupName, userId, providerRequest);
  }

  @Override
  public ProviderResponse addUserToGroupManager(
      String realm,
      String storage,
      String userId,
      String applicationName,
      ProviderRequest providerRequest) {
    if (storage == null) {
      User user = userService.findById(realm, null, userId, false);
      storage = (String) user.getMetadatas().get(GlobalKeysConfig.USERSTORAGE.getName());
    }
    return storeProvider
        .getWriterStore(realm, storage)
        .addUserToGroupManager(applicationName, userId, providerRequest);
  }

  @Override
  public ProviderResponse deleteUserFromManagerGroup(
      String realm,
      String storage,
      String userId,
      String applicationName,
      ProviderRequest providerRequest) {
    if (storage == null) {
      User user = userService.findById(realm, null, userId, false);
      storage = (String) user.getMetadatas().get(GlobalKeysConfig.USERSTORAGE.getName());
    }
    return storeProvider
        .getWriterStore(realm, storage)
        .deleteUserFromManagerGroup(applicationName, userId, providerRequest);
  }

  @Override
  public Group getManagerGroup(String realm, String applicationName) {
    return storeProvider
        .getReaderStore(realm)
        .getManagerGroup(applicationName)
        .orElseThrow(() -> new ManagerGroupNotFoundException(realm, applicationName));
  }
}
