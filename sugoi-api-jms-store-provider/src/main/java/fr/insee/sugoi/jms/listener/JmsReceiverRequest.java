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
package fr.insee.sugoi.jms.listener;

import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.jms.model.BrokerRequest;
import fr.insee.sugoi.jms.model.BrokerResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "fr.insee.sugoi.jms.receiver.request.enabled",
    havingValue = "true",
    matchIfMissing = false)
public class JmsReceiverRequest {

  @Autowired JmsRequestRouter router;

  private static final Logger logger = LoggerFactory.getLogger(JmsReceiverRequest.class);

  @Value("${fr.insee.sugoi.jms.queue.response.name:}")
  private String queueResponseName;

  @Value("${fr.insee.sugoi.jms.queue.response.asynchronous.name:}")
  private String queueResponseAsynchronousName;

  @Autowired
  @Qualifier("synchronous")
  JmsTemplate jmsTemplateSynchronous;

  @Autowired
  @Qualifier("asynchronous")
  JmsTemplate jmsTemplateAsynchronous;

  @JmsListener(
      destination = "${fr.insee.sugoi.jms.queue.requests.name:}",
      containerFactory = "myFactory")
  public void onQueueSynchronousRequest(BrokerRequest request) throws Exception {
    onRequest(request, jmsTemplateSynchronous);
  }

  @JmsListener(
      destination = "${fr.insee.sugoi.jms.queue.requests.asynchronous.name:}",
      containerFactory = "myFactory")
  public void onQueueAsynchronousRequest(BrokerRequest request) throws Exception {
    onRequest(request, jmsTemplateAsynchronous);
  }

  private void onRequest(BrokerRequest request, JmsTemplate jmsTemplate) throws Exception {
    logger.debug(
        "New message with correlactionId {} message: {}", request.getCorrelationId(), request);
    ProviderResponse response = router.exec(request);
    BrokerResponse br = new BrokerResponse();
    br.setProviderResponse(response);
    boolean isAsync =
        (boolean)
            ((java.util.LinkedHashMap<String, Object>)
                    request
                        .getmethodParams()
                        .get(fr.insee.sugoi.jms.utils.JmsAtttributes.PROVIDER_REQUEST))
                .get("asynchronousAllowed");
    jmsTemplate.convertAndSend(
        isAsync && StringUtils.isNotEmpty(queueResponseAsynchronousName)
            ? queueResponseAsynchronousName
            : queueResponseName,
        br,
        m -> {
          m.setJMSCorrelationID(request.getCorrelationId());
          return m;
        });
  }
}
