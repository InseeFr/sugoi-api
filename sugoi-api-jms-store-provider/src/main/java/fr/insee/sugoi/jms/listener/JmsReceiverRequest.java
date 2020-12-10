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

import fr.insee.sugoi.jms.model.BrokerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "fr.insee.sugoi.jms.receiver.request.enabled",
    havingValue = "true",
    matchIfMissing = false)
public class JmsReceiverRequest {

  @Autowired JmsRouter router;

  private static final Logger logger = LogManager.getLogger(JmsReceiverRequest.class);

  @Value("${fr.insee.sugoi.jms.queue.requests.name:}")
  private String queueRequestName;

  @Value("${fr.insee.sugoi.jms.queue.requests.name:}")
  private String queueUrgentRequestName;

  @JmsListener(destination = "${fr.insee.sugoi.jms.queue.requests.name:}")
  public void onRequest(BrokerRequest request) throws Exception {
    logger.info("New message on queue {}", queueRequestName);
    router.exec(request);
  }

  @JmsListener(destination = "${fr.insee.sugoi.jms.priority.queue.request.name:}")
  public void onUrgentRequest(BrokerRequest request) {
    logger.info("New message on queue {}", queueUrgentRequestName);
    System.out.println(request);
  }
}
