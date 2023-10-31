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
package fr.insee.sugoi.core.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.impl.GroupServiceImpl;
import fr.insee.sugoi.core.service.impl.UserServiceImpl;
import fr.insee.sugoi.core.store.ReaderStore;
import fr.insee.sugoi.core.store.Store;
import fr.insee.sugoi.core.store.StoreStorage;
import fr.insee.sugoi.core.store.impl.StoreProviderImpl;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.model.exceptions.GroupNotFoundException;
import fr.insee.sugoi.model.exceptions.RealmNotFoundException;
import fr.insee.sugoi.model.paging.PageableResult;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {GroupServiceImpl.class, UserServiceImpl.class, StoreProviderImpl.class})
@TestPropertySource(locations = "classpath:/application.properties")
public class GroupServiceTest {

  @MockBean private StoreStorage storeStorage;

  @MockBean private RealmProvider realmProvider;

  @Autowired private GroupServiceImpl groupService;

  private Realm realm;

  @Mock private Store store;
  @Mock private ReaderStore readerStore;

  @BeforeEach
  public void setup() {
    Mockito.when(realmProvider.load("idonotexist")).thenReturn(Optional.empty());

    realm = new Realm();
    realm.setName("realm");
    UserStorage us1 = new UserStorage();
    us1.setName("us1");
    realm.setUserStorages(List.of(us1));
    Mockito.when(realmProvider.load("realm")).thenReturn(Optional.of(realm));

    Mockito.when(storeStorage.getStore(Mockito.any(), Mockito.any())).thenReturn(store);
    Mockito.when(store.getReader()).thenReturn(readerStore);
    Mockito.when(readerStore.getGroup("application", "donotexist")).thenReturn(Optional.empty());
  }

  @Test
  @DisplayName(
      "Given we try to fetch a group on a realm that do not exist, "
          + "then throw RealmNotFound exception")
  public void getGroupShouldFailWhenRealmNotFound() {
    assertThrows(
        RealmNotFoundException.class,
        () -> groupService.findById("idonotexist", "application", "Toto"));
  }

  @Test
  @DisplayName(
      "Given we try to find groups on a realm that do not exist, "
          + "then throw RealmNotFound exception")
  public void findGroupsShouldFailWhenRealmNotFound() {
    Group searchGroup = new Group();
    searchGroup.setName("oto");
    assertThrows(
        RealmNotFoundException.class,
        () ->
            groupService.findByProperties(
                "idonotexist", "application", searchGroup, new PageableResult()));
  }

  @Test
  @DisplayName(
      "Given we try to fetch a group that do not exist, " + "then throw GroupNotFound exception")
  public void getGroupShouldFailWhenUserNotFound() {
    assertThrows(
        GroupNotFoundException.class,
        () -> groupService.findById("realm", "application", "donotexist"));
  }

  @Test
  @DisplayName(
      "Given we try to delete a group on a realm do not exist, "
          + "then throw RealmNotFoundException")
  public void deleteOrganizationShouldFailWhenRealmNotFound() {
    assertThrows(
        RealmNotFoundException.class,
        () -> groupService.delete("idonotexist", "application", "toto", null));
  }
}
