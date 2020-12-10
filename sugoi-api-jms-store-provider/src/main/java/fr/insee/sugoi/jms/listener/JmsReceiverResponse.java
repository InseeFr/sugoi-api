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

import fr.insee.sugoi.jms.model.BrokerResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;

@ConditionalOnProperty(
    name = "fr.insee.sugoi.jms.receiver.response.enabled",
    havingValue = "true",
    matchIfMissing = false)
public class JmsReceiverResponse {

  private static final Logger logger = LogManager.getLogger(JmsReceiverRequest.class);

  @Value("${fr.insee.sugoi.jms.queue.requests.name:}")
  private String queueResponseName;

  @Value("${fr.insee.sugoi.jms.queue.requests.name:}")
  private String queueUrgentResponseName;

  @JmsListener(destination = "${fr.insee.sugoi.jms.queue.response.name:}")
  public void onResponse(BrokerResponse response) {
    logger.info("New message on queue {}", queueResponseName);
    System.out.println(response);
  }

  @JmsListener(destination = "${fr.insee.sugoi.jms.priority.queue.response.name}")
  public void onUrgentResponse(BrokerResponse response) {
    logger.info("New message on queue {}", queueUrgentResponseName);
    System.out.println(response);
  }
}
