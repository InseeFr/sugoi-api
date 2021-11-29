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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.insee.sugoi.core.event.publisher.SugoiEventPublisher;
import fr.insee.sugoi.core.exceptions.RealmNotFoundException;
import fr.insee.sugoi.core.exceptions.UserNotFoundException;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.impl.UserServiceImpl;
import fr.insee.sugoi.core.store.ReaderStore;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.model.paging.PageableResult;
import fr.insee.sugoi.model.paging.SearchType;
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

@SpringBootTest(classes = UserServiceImpl.class)
@TestPropertySource(locations = "classpath:/application.properties")
public class UserServiceTest {

  @MockBean private StoreProvider storeProvider;

  @MockBean private RealmProvider realmProvider;

  @MockBean private SugoiEventPublisher sugoiEventPublisher;

  @Mock private ReaderStore readerStore1;

  @Mock private ReaderStore readerStore2;

  @Mock private WriterStore writerStore;

  @Autowired private UserServiceImpl userService;

  private User user1;

  private Realm realm;

  @BeforeEach
  public void setup() {
    user1 = new User();
    user1.setUsername("Toto");
    user1.setMail("toto@insee.fr");

    realm = new Realm();
    realm.setName("realm");
    UserStorage us1 = new UserStorage();
    us1.setName("us1");
    UserStorage us2 = new UserStorage();
    us2.setName("us2");
    realm.setUserStorages(List.of(us1, us2));
    Mockito.when(realmProvider.load("realm")).thenReturn(Optional.of(realm));

    Mockito.when(storeProvider.getReaderStore("realm", "us1")).thenReturn(readerStore1);
    Mockito.when(readerStore1.getUser("Toto")).thenReturn(Optional.empty());

    Mockito.when(storeProvider.getReaderStore("realm", "us2")).thenReturn(readerStore2);
    Mockito.when(readerStore2.getUser("Toto")).thenReturn(Optional.of(user1));
    Mockito.when(readerStore2.getUser("donotexist")).thenReturn(Optional.empty());

    Mockito.when(storeProvider.getReaderStore(Mockito.eq("idonotexist"), Mockito.anyString()))
        .thenThrow(RealmNotFoundException.class);
    Mockito.when(storeProvider.getWriterStore(Mockito.eq("idonotexist"), Mockito.anyString()))
        .thenThrow(RealmNotFoundException.class);

    Mockito.when(storeProvider.getWriterStore("realm", "us2")).thenReturn(writerStore);
    Mockito.when(writerStore.deleteUser(Mockito.eq("donotexist"), Mockito.any()))
        .thenThrow(UserNotFoundException.class);
  }

  @Test
  public void getUserOnMultipleStorage() {
    assertThat("get user Toto", userService.findById("realm", null, "Toto"), is(user1));
  }

  @Test
  @DisplayName(
      "Given we try to fetch a user on a realm that do not exist, "
          + "then throw RealmNotFound exception")
  public void getUserShouldFailWhenRealmNotFound() {
    assertThrows(
        RealmNotFoundException.class, () -> userService.findById("idonotexist", "us2", "Toto"));
  }

  @Test
  @DisplayName(
      "Given we try to find users on a realm that do not exist, "
          + "then throw RealmNotFound exception")
  public void findUsersShouldFailWhenRealmNotFound() {
    assertThrows(
        RealmNotFoundException.class,
        () ->
            userService.findByProperties(
                "idonotexist", "us2", new User("Toto"), new PageableResult(), SearchType.AND));
  }

  @Test
  @DisplayName(
      "Given we try to find user by its mail on a realm that do not exist, "
          + "then throw RealmNotFound exception")
  public void findUserByMailShouldFailWhenRealmNotFound() {
    assertThrows(
        RealmNotFoundException.class,
        () -> userService.findByMail("idonotexist", "us2", "toto@insee.fr"));
  }

  @Test
  @DisplayName(
      "Given we try to delete a user that do not exist, " + "then throw UserNotFoundException")
  public void deleteUserThatDoNotExistShouldFail() {
    assertThrows(
        UserNotFoundException.class, () -> userService.delete("realm", "us2", "donotexist", null));
  }

  @Test
  @DisplayName(
      "Given we try to delete a user on a realm do not exist, "
          + "then throw RealmNotFoundException")
  public void deleteUserShouldFailWhenRealmNotFound() {
    assertThrows(
        RealmNotFoundException.class, () -> userService.delete("idonotexist", "us2", "toto", null));
  }
}
