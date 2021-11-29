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

import fr.insee.sugoi.core.event.publisher.SugoiEventPublisher;
import fr.insee.sugoi.core.exceptions.ApplicationNotFoundException;
import fr.insee.sugoi.core.exceptions.RealmNotFoundException;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.impl.ApplicationServiceImpl;
import fr.insee.sugoi.core.store.ReaderStore;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.model.Realm;
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

@SpringBootTest(classes = ApplicationServiceImpl.class)
@TestPropertySource(locations = "classpath:/application.properties")
public class ApplicationServiceTest {
  @MockBean private StoreProvider storeProvider;

  @MockBean private RealmProvider realmProvider;

  @MockBean private SugoiEventPublisher sugoiEventPublisher;

  @Mock private ReaderStore readerStore2;

  @Mock private WriterStore writerStore;

  @Autowired private ApplicationServiceImpl applicationService;

  private Realm realm;

  @BeforeEach
  public void setup() {
    Mockito.when(realmProvider.load("idonotexist")).thenReturn(Optional.empty());

    realm = new Realm();
    realm.setName("realm");
    Mockito.when(realmProvider.load("realm")).thenReturn(Optional.of(realm));

    Mockito.when(storeProvider.getReaderStore("realm", "us2")).thenReturn(readerStore2);
    Mockito.when(readerStore2.getApplication("donotexist")).thenReturn(Optional.empty());

    Mockito.when(storeProvider.getReaderStore(Mockito.eq("idonotexist")))
        .thenThrow(RealmNotFoundException.class);
    Mockito.when(storeProvider.getWriterStore(Mockito.eq("idonotexist")))
        .thenThrow(RealmNotFoundException.class);

    Mockito.when(storeProvider.getWriterStore("realm")).thenReturn(writerStore);
    Mockito.when(writerStore.deleteApplication(Mockito.eq("donotexist"), Mockito.any()))
        .thenThrow(ApplicationNotFoundException.class);
  }

  @Test
  @DisplayName(
      "Given we try to delete a organization that do not exist, "
          + "then throw OrganizationNotFoundException")
  public void deleteApplicationThatDoNotExistShouldFail() {
    assertThrows(
        ApplicationNotFoundException.class,
        () -> applicationService.delete("realm", "donotexist", null));
  }

  @Test
  @DisplayName(
      "Given we try to delete a organization on a realm do not exist, "
          + "then throw RealmNotFoundException")
  public void deleteApplicationShouldFailWhenRealmNotFound() {
    assertThrows(
        RealmNotFoundException.class, () -> applicationService.delete("idonotexist", "toto", null));
  }
}
