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

import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.impl.OrganizationServiceImpl;
import fr.insee.sugoi.core.store.ReaderStore;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.model.exceptions.OrganizationNotFoundException;
import fr.insee.sugoi.model.exceptions.RealmNotFoundException;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = OrganizationServiceImpl.class)
@TestPropertySource(locations = "classpath:/application.properties")
public class OrganizationServiceTest {
  @MockitoBean private StoreProvider storeProvider;

  @MockitoBean private RealmProvider realmProvider;

  @Mock private ReaderStore readerStore1;

  @Mock private ReaderStore readerStore2;

  @Mock private WriterStore writerStore;

  @Autowired private OrganizationServiceImpl orgaService;

  private Organization orga1;

  private Realm realm;

  @BeforeEach
  public void setup() {
    orga1 = new Organization();
    orga1.setIdentifiant("Toto");

    Mockito.when(realmProvider.load("idonotexist")).thenReturn(Optional.empty());

    realm = new Realm();
    realm.setName("realm");
    UserStorage us1 = new UserStorage();
    us1.setName("us1");
    UserStorage us2 = new UserStorage();
    us2.setName("us2");
    realm.setUserStorages(List.of(us1, us2));
    Mockito.when(realmProvider.load("realm")).thenReturn(Optional.of(realm));

    Mockito.when(storeProvider.getReaderStore("realm", "us1")).thenReturn(readerStore1);
    Mockito.when(readerStore1.getOrganization("Toto")).thenReturn(Optional.empty());

    Mockito.when(storeProvider.getReaderStore("realm", "us2")).thenReturn(readerStore2);
    Mockito.when(readerStore2.getOrganization("Toto")).thenReturn(Optional.of(orga1));
    Mockito.when(readerStore2.getOrganization("donotexist")).thenReturn(Optional.empty());

    Mockito.when(storeProvider.getReaderStore(Mockito.eq("idonotexist"), Mockito.anyString()))
        .thenThrow(RealmNotFoundException.class);
    Mockito.when(storeProvider.getWriterStore(Mockito.eq("idonotexist"), Mockito.anyString()))
        .thenThrow(RealmNotFoundException.class);

    Mockito.when(storeProvider.getWriterStore("realm", "us2")).thenReturn(writerStore);
    Mockito.when(writerStore.deleteOrganization(Mockito.eq("donotexist"), Mockito.any()))
        .thenThrow(OrganizationNotFoundException.class);
  }

  @Test
  public void getOrganizationOnMultipleStorage() {
    assertThat("get organization Toto", orgaService.findById("realm", null, "Toto"), is(orga1));
  }

  @Test
  @DisplayName(
      "Given we try to fetch a organization on a realm that do not exist, "
          + "then throw RealmNotFound exception")
  public void getOrganizationShouldFailWhenRealmNotFound() {
    assertThrows(
        RealmNotFoundException.class, () -> orgaService.findById("idonotexist", "us2", "Toto"));
  }

  @Test
  @DisplayName(
      "Given we try to find organizations on a realm that do not exist, "
          + "then throw RealmNotFound exception")
  public void findOrganizationsShouldFailWhenRealmNotFound() {
    Organization searchOrganization = new Organization();
    searchOrganization.setIdentifiant("oto");
    assertThrows(
        RealmNotFoundException.class,
        () ->
            orgaService.findByProperties(
                "idonotexist", "us2", searchOrganization, new PageableResult(), SearchType.AND));
  }

  @Test
  @DisplayName(
      "Given we try to fetch a organization that do not exist, "
          + "then throw OrganizationNotFound exception")
  public void getOrganizationShouldFailWhenUserNotFound() {
    assertThrows(
        OrganizationNotFoundException.class,
        () -> orgaService.findById("realm", "us2", "donotexist"));
  }

  @Test
  @DisplayName(
      "Given we try to fetch an organization that do not exist on a realm without giving storage, "
          + "then throw OrganizationNotFound exception")
  public void getOrganizationShouldFailWhenUserNotFoundAndNoStorage() {
    assertThrows(
        OrganizationNotFoundException.class,
        () -> orgaService.findById("realm", null, "donotexist"));
  }

  @Test
  @DisplayName(
      "Given we try to delete a organization on a realm do not exist, "
          + "then throw RealmNotFoundException")
  public void deleteOrganizationShouldFailWhenRealmNotFound() {
    assertThrows(
        RealmNotFoundException.class, () -> orgaService.delete("idonotexist", "us2", "toto", null));
  }
}
