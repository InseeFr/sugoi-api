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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.model.ProviderResponse.ProviderResponseStatus;
import fr.insee.sugoi.core.model.SugoiUser;
import fr.insee.sugoi.jms.model.BrokerResponse;
import fr.insee.sugoi.jms.writer.JmsWriter;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.UserStorage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = JmsWriter.class)
public class JmsWriterStoreTest {

  JmsWriterStore jmsWriterStore;

  @MockitoBean private JmsWriter jmsWriter;

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
  }

  @Test
  public void createUserSynchronousTest() {

    User user = new User("synchronous");
    user.setMail("mail@mail.fr");

    ProviderResponse mockedProviderResponse =
        new ProviderResponse("synchronous", "", ProviderResponseStatus.OK, user, null);
    BrokerResponse mockedBrokerResponse = new BrokerResponse();
    mockedBrokerResponse.setProviderResponse(mockedProviderResponse);

    Mockito.when(jmsWriter.writeRequestInQueueSynchronous(anyString(), anyString(), any()))
        .thenReturn("myid");
    Mockito.when(jmsWriter.checkResponseInQueue(anyString(), anyString()))
        .thenReturn(mockedBrokerResponse);

    ProviderRequest providerRequest = new ProviderRequest(new SugoiUser(), false, null);
    ProviderResponse providerResponse = jmsWriterStore.createUser(user, providerRequest);

    assertThat(
        "Response status should be OK",
        providerResponse.getStatus(),
        is(ProviderResponseStatus.OK));
  }
}
