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
package fr.insee.sugoi.jms;

import fr.insee.sugoi.core.event.publisher.SugoiEventPublisher;
import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.CredentialsService;
import fr.insee.sugoi.core.service.GroupService;
import fr.insee.sugoi.core.service.OrganizationService;
import fr.insee.sugoi.core.service.PasswordService;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.core.service.impl.CredentialsServiceImpl;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.jms.writer.JmsWriter;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Import(value = {CredentialsServiceImpl.class, SugoiEventPublisher.class})
public class CredentialsJmsTest {

  @Autowired JmsWriter jmsWriter;
  @SpyBean private CredentialsService credentialsServiceImpl;

  @Autowired
  @Qualifier("asynchronous")
  JmsTemplate jmsTemplate;

  private JmsWriterStore jmsWriterStore;
  @Mock private JmsWriterStore doNothingWriterStore;

  @MockitoBean private StoreProvider storeProvider;
  @MockitoBean private PasswordService passwordService;
  @MockitoBean private RealmProvider realmprovider;
  @MockitoBean private UserService userService;
  @MockitoBean private GroupService groupService;
  @MockitoBean private OrganizationService organizationService;

  @BeforeEach
  public void setUp() {

    Realm realm = new Realm();
    realm.setName("domaine1");
    UserStorage userStorage = new UserStorage();
    userStorage.setName("default");
    realm.setUserStorages(List.of(userStorage));

    jmsWriterStore =
        new JmsWriterStore(
            jmsWriter, "queue.request", "queue.response", null, null, realm, userStorage);

    Mockito.when(
            doNothingWriterStore.initPassword(
                Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any()))
        .thenReturn(null);

    Mockito.when(realmprovider.load("domaine1")).thenReturn(Optional.of(realm));
    Mockito.when(storeProvider.getWriterStore("domaine1", "default"))
        .thenReturn(jmsWriterStore)
        .thenReturn(doNothingWriterStore);
    Mockito.when(
            passwordService.validatePassword(
                Mockito.eq("averycomplexpassword"),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any()))
        .thenReturn(true);
  }

  @Test
  @DisplayName(
      "When using the initpassowrd service through a JMS, the message is written in JMS queue"
          + "then read and pass through credentialsService a second time")
  public void newPasswordIsSentThrewJmsTest() {

    ProviderRequest providerRequest = new ProviderRequest();
    providerRequest.setAsynchronousAllowed(true);
    credentialsServiceImpl.initPassword(
        "domaine1", "default", "toto", "averycomplexpassword", true, providerRequest);
    Mockito.verify(credentialsServiceImpl, Mockito.timeout(1000).times(2))
        .initPassword(
            Mockito.eq("domaine1"),
            Mockito.eq("default"),
            Mockito.eq("toto"),
            Mockito.eq("averycomplexpassword"),
            Mockito.eq(true),
            Mockito.any());
  }
}
