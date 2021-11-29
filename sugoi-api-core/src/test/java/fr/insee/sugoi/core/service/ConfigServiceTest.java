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
import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.model.ProviderResponse.ProviderResponseStatus;
import fr.insee.sugoi.core.model.SugoiUser;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.impl.ConfigServiceImpl;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {ConfigServiceImpl.class, SugoiEventPublisher.class})
@TestPropertySource(locations = "classpath:/application.properties")
public class ConfigServiceTest {

  @MockBean private RealmProvider realmProvider;

  @Autowired private ConfigService configService;

  private Realm realm;

  @BeforeEach
  public void setup() {

    realm = new Realm();
    realm.setName("realm");
    UserStorage us1 = new UserStorage();
    us1.setName("us1");
    UserStorage us2 = new UserStorage();
    us2.setName("us2");
    realm.setUserStorages(List.of(us1, us2));

    Realm returnedRealm1 = new Realm();
    returnedRealm1.setName("realm");

    Mockito.when(realmProvider.load("realm")).thenReturn(Optional.of(returnedRealm1));
    Mockito.when(realmProvider.load("idonotexist")).thenReturn(Optional.empty());
  }

  @Test
  public void shouldUpdateExistingRealm() {
    ProviderRequest providerRequest =
        new ProviderRequest(new SugoiUser("toto", List.of("toto")), false, null, false);
    ProviderResponse mockedResponse =
        new ProviderResponse("1", "1", ProviderResponseStatus.OK, realm, null);
    Mockito.when(realmProvider.updateRealm(realm, providerRequest)).thenReturn(mockedResponse);

    ProviderResponse providerResponse = configService.updateRealm(realm, providerRequest);
    assertThat(
        "realm should have been updated",
        providerResponse.getStatus(),
        is(ProviderResponseStatus.OK));
  }

  @Test
  public void shouldFindRealmWhenPresent() {
    assertThat("Realm should be found", configService.getRealm("realm").getName(), is("realm"));
  }

  @Test
  @DisplayName("When searching a realm that do not exist, should throw RealmNotFoundException")
  public void shouldThrowNotFoundWhenNoRealmFound() {
    assertThrows(RealmNotFoundException.class, () -> configService.getRealm("idonotexist"));
  }
}
