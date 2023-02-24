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

import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.service.CredentialsService;
import fr.insee.sugoi.core.service.GroupService;
import fr.insee.sugoi.core.service.OrganizationService;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.jms.listener.JmsRequestRouter;
import fr.insee.sugoi.jms.model.BrokerRequest;
import fr.insee.sugoi.jms.utils.Converter;
import fr.insee.sugoi.jms.utils.JmsAtttributes;
import fr.insee.sugoi.jms.utils.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = {JmsRequestRouter.class, Converter.class})
public class JmsRequestRouterTest {

  @MockBean private StoreProvider storeProvider;
  @MockBean private UserService userService;
  @MockBean private CredentialsService credentialsService;
  @MockBean private GroupService groupService;
  @MockBean private OrganizationService orgService;

  @Autowired JmsRequestRouter jmsRequestRouter;

  @Test
  void testCreateUserWithAddress() throws Exception {
    ProviderResponse providerResponse = new ProviderResponse();
    providerResponse.setEntityId("res");
    providerResponse.setStatus(ProviderResponse.ProviderResponseStatus.OK);
    Mockito.when(userService.create(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(providerResponse);

    LinkedHashMap<String, String> address =
        new LinkedHashMap<>(Map.of("line1", "adresse1", "line7", "7"));
    LinkedHashMap<String, Object> user =
        new LinkedHashMap<>(Map.of("lastName", "testAddress", "address", address));
    Map<String, Object> requestParams = Map.of(JmsAtttributes.USER, user);
    BrokerRequest brokerRequest = new BrokerRequest(Method.CREATE_USER, requestParams);
    assertThat(
        jmsRequestRouter.exec(brokerRequest).getStatus(),
        is(ProviderResponse.ProviderResponseStatus.OK));
  }
}
