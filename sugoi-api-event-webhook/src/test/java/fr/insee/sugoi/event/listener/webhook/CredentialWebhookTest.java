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
package fr.insee.sugoi.event.listener.webhook;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import fr.insee.sugoi.core.event.publisher.SugoiEventPublisher;
import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.core.service.PasswordService;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.core.service.impl.CredentialsServiceImpl;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.event.listener.webhook.service.impl.WebHookServiceImpl;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
    classes = {
      CredentialsServiceImpl.class,
      SugoiEventPublisher.class,
      SugoiEventWebHookProducer.class,
    })
@TestPropertySource(locations = "classpath:/application.properties")
public class CredentialWebhookTest {

  @Autowired CredentialsServiceImpl credentialsServiceImpl;

  @MockBean private StoreProvider storeProvider;

  @MockBean private PasswordService passwordService;

  @SpyBean private WebHookServiceImpl webHookServiceImpl;

  @MockBean private ConfigService configService;

  @MockBean private UserService userService;

  @MockBean private WriterStore writerStore;

  @Captor ArgumentCaptor<Map<String, Object>> argumentCaptorProperties;

  @BeforeEach
  public void setUp() {
    Mockito.when(configService.getRealm("domaine1")).thenReturn(Optional.of(new Realm()));
    Mockito.when(
            passwordService.generatePassword(
                Mockito.isNull(),
                Mockito.isNull(),
                Mockito.isNull(),
                Mockito.isNull(),
                Mockito.isNull()))
        .thenReturn("verycomplicatedpassword");
    Mockito.when(storeProvider.getWriterStore(Mockito.eq("domaine1"), Mockito.eq("default")))
        .thenReturn(writerStore);

    User toto = new User("toto");
    toto.setMail("toto@insee.fr");

    Mockito.when(
            userService.findById(Mockito.eq("domaine1"), Mockito.eq("default"), Mockito.eq("toto")))
        .thenReturn(Optional.of(toto));
  }

  @Test
  @DisplayName(
      "When the reset password service is called, with the appropriate webhook being configured,"
          + " this webhook should be called and the template completed.")
  public void callResetWebhookWhenResetServiceTest() {

    credentialsServiceImpl.reinitPassword(
        "domaine1",
        "default",
        "toto",
        Map.of("new_property", "prop"),
        "SPOOC",
        false,
        new ProviderRequest());

    Mockito.verify(webHookServiceImpl, Mockito.timeout(1000))
        .resetPassword(Mockito.eq("spooc"), argumentCaptorProperties.capture());

    Map<String, Object> properties = argumentCaptorProperties.getValue();

    Map<?, ?> propertiesOfProperties = (Map<?, ?>) properties.get("properties");

    assertThat(
        "Property from template properties should have been kept",
        "prop",
        is(propertiesOfProperties.get("new_property")));
    assertThat("Mail should be the user one", properties.get("mail"), is("toto@insee.fr"));
    assertThat(
        "Password should be the one returned by service",
        properties.get("password"),
        is("verycomplicatedpassword"));
    assertThat(
        "User should have its username", ((User) properties.get("user")).getUsername(), is("toto"));
  }

  @Test
  @DisplayName("If a mail is given as a template property, this mail should be used in webhook")
  public void mailCanBeOverridenTest() {

    credentialsServiceImpl.reinitPassword(
        "domaine1",
        "default",
        "toto",
        Map.of("new_property", "prop", "mail", "admin@insee.fr"),
        "SPOOC",
        false,
        new ProviderRequest());

    Mockito.verify(webHookServiceImpl, Mockito.timeout(1000))
        .resetPassword(Mockito.eq("spooc"), argumentCaptorProperties.capture());

    Map<String, Object> properties = argumentCaptorProperties.getValue();
    assertThat(
        "Mail should be the one defined in properties",
        "admin@insee.fr",
        is(properties.get("mail")));
  }

  @Test
  @DisplayName(
      "When the send login service is called, with the appropriate webhook being configured,"
          + " this webhook should be called and the template completed.")
  public void callResetWebhookWhenSendLoginTest() {

    credentialsServiceImpl.sendLogin(
        "domaine1", "default", "toto", Map.of("new_property", "prop"), "SPOOC");

    Mockito.verify(webHookServiceImpl, Mockito.timeout(1000))
        .sendLogin(Mockito.eq("spooc"), argumentCaptorProperties.capture());

    Map<String, Object> properties = argumentCaptorProperties.getValue();

    Map<?, ?> propertiesOfProperties = (Map<?, ?>) properties.get("properties");

    assertThat(
        "Property from template properties should have been kept",
        "prop",
        is(propertiesOfProperties.get("new_property")));
    assertThat("Mail should be the user one", properties.get("mail"), is("toto@insee.fr"));
    assertThat(
        "User should have its username", ((User) properties.get("user")).getUsername(), is("toto"));
  }
}
