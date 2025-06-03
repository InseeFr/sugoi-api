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

import fr.insee.sugoi.core.event.configuration.EventKeysConfig;
import fr.insee.sugoi.core.event.model.SugoiEvent;
import fr.insee.sugoi.core.event.model.SugoiEventTypeEnum;
import fr.insee.sugoi.event.listener.webhook.service.impl.WebHookServiceImpl;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = {SugoiEventWebHookProducer.class, WebHookServiceImpl.class})
@TestPropertySource(locations = "classpath:/application2.properties")
class SugoiEventWebHookProducerTest {
  @MockitoBean WebHookServiceImpl webHookService;

  @Autowired SugoiEventWebHookProducer sugoiEventWebHookProducer;
  private SugoiEvent sugoiEventSendLogin;
  private SugoiEvent sugoiEventChangePasswd;

  @BeforeEach
  void setUp() {
    Map<String, Object> eventProperties =
        Map.of(
            EventKeysConfig.USER_ID,
            "toto",
            EventKeysConfig.WEBSERVICE_TAG,
            "SPOOC",
            EventKeysConfig.MAILS,
            List.of("toto@titi.fr"));
    sugoiEventSendLogin =
        new SugoiEvent("domaine1", "storage1", SugoiEventTypeEnum.SEND_LOGIN, eventProperties);
    sugoiEventChangePasswd =
        new SugoiEvent("domaine1", "storage1", SugoiEventTypeEnum.CHANGE_PASSWORD, eventProperties);
  }

  @Test
  @DisplayName(
      "Given the webhook.enabled.events contains SEND_LOGIN, "
          + "then the webhook should be called")
  void shouldCallWebhookForSendLogin() {

    sugoiEventWebHookProducer.handleContextStart(sugoiEventSendLogin);

    Mockito.verify(webHookService, Mockito.timeout(100))
        .sendLogin(Mockito.anyString(), Mockito.anyMap());
  }

  @Test
  @DisplayName(
      "Given the webhook.enabled.events does not contain CHANGE_PASSWORD, "
          + "then the webhook should not be called")
  void shouldNotCallWebhookForChangePasswd() {

    sugoiEventWebHookProducer.handleContextStart(sugoiEventChangePasswd);

    Mockito.verify(webHookService, Mockito.after(100).never())
        .changePassword(Mockito.anyString(), Mockito.anyMap());
  }
}
