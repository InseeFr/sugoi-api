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
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;

import fr.insee.sugoi.core.event.publisher.SugoiEventPublisher;
import fr.insee.sugoi.core.exceptions.PasswordPolicyNotMetException;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.model.ProviderResponse.ProviderResponseStatus;
import fr.insee.sugoi.core.service.impl.CredentialsServiceImpl;
import fr.insee.sugoi.core.service.impl.PasswordServiceImpl;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.model.PasswordPolicyConstants;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {CredentialsServiceImpl.class, PasswordServiceImpl.class})
@TestPropertySource(locations = "classpath:/application.properties")
public class CredentialsServiceTest {

  @MockBean private StoreProvider storeProvider;

  @MockBean private SugoiEventPublisher sugoiEventPublisher;

  @MockBean private ConfigService configService;

  @MockBean private UserService userService;

  @Autowired CredentialsServiceImpl credentialsService;

  @Spy private WriterStore writerStore;

  @Captor ArgumentCaptor<String> argumentCaptorProperties;

  private User user1;

  private Realm realmUpperCase;
  private Realm realmNoUpperCase;

  @BeforeEach
  public void setup() {
    user1 = new User();
    user1.setUsername("Toto");
    user1.setMail("toto@insee.fr");

    realmUpperCase = new Realm();
    realmUpperCase.setName("realmWithUpperCase");
    realmUpperCase.addProperty(PasswordPolicyConstants.VALIDATE_PASSWORD_WITH_UPPERCASE, "true");
    Mockito.when(configService.getRealm("realmWithUpperCase")).thenReturn(realmUpperCase);

    realmNoUpperCase = new Realm();
    realmNoUpperCase.setName("realmWithoutUpperCase");
    realmNoUpperCase.addProperty("validate_password_WITHUpperCase", "false");
    realmNoUpperCase.addProperty(PasswordPolicyConstants.CREATE_PASSWORD_SIZE, "40");
    Mockito.when(configService.getRealm("realmWithoutUpperCase")).thenReturn(realmNoUpperCase);
    Mockito.when(storeProvider.getWriterStore("realmWithoutUpperCase", "us1"))
        .thenReturn(writerStore);
    ProviderResponse acceptedResponse = new ProviderResponse();
    acceptedResponse.setStatus(ProviderResponseStatus.ACCEPTED);
    Mockito.when(writerStore.changePassword(any(), any(), any(), any(), any(), any()))
        .thenReturn(acceptedResponse);
    Mockito.when(userService.findById(any(), any(), any())).thenReturn(new User());
  }

  @Test
  @DisplayName(
      "Given a realm which is configured to refuse password without upper case, "
          + "then a password without uppercase should be rejected on change password")
  public void passwordShouldRespectUpperCaseRealmPasswordPolicy() {
    Map<String, String> map = Map.of();
    assertThrows(
        PasswordPolicyNotMetException.class,
        () ->
            credentialsService.changePassword(
                "realmWithUpperCase",
                "us1",
                "toto",
                "oldPassword",
                "verylongpasswordwithoutuppercase@1",
                null,
                map,
                null));
  }

  @Test
  @DisplayName(
      "Given a realm which is configured to accept password without upper case, "
          + "then a password without uppercase should be accepted on change password")
  public void passwordShouldRespectNoUpperCaseRealmPasswordPolicy() {
    assertThat(
        "Password without uppercase should be accepted due to realm configuration",
        credentialsService
            .changePassword(
                "realmWithoutUpperCase",
                "us1",
                "toto",
                "oldPassword",
                "verylongpasswordwithoutuppercase@1",
                null,
                Map.of(),
                null)
            .getStatus(),
        is(ProviderResponseStatus.ACCEPTED));
  }

  @Test
  @DisplayName(
      "Given a realm which password length property is 40, then "
          + "a password of length 40 should be created on reinit password")
  public void passwordShouldFollowPasswordLengthRealmProperty() {
    credentialsService.reinitPassword(
        "realmWithoutUpperCase", "us1", "test", Map.of(), null, false, null);
    Mockito.verify(writerStore)
        .reinitPassword(
            any(), argumentCaptorProperties.capture(), anyBoolean(), any(), any(), any());
    String generatedPassword = argumentCaptorProperties.getValue();
    assertThat(
        "The password generated should be 40 characters long", generatedPassword.length() == 40);
  }
}
