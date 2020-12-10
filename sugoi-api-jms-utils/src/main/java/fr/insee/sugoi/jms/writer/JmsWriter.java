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
package fr.insee.sugoi.jms.writer;

import fr.insee.sugoi.jms.exception.BrokerException;
import fr.insee.sugoi.jms.model.BrokerRequest;
import java.util.ArrayList;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class JmsWriter {

  private static final Logger logger = LogManager.getLogger(JmsWriter.class);

  @Autowired JmsTemplate jmsTemplate;

  public void writeInQueue(String queueName, String methodName, Map<String, Object> params) {
    BrokerRequest request = new BrokerRequest();
    request.setMethod(methodName);
    for (String key : new ArrayList<>(params.keySet())) {
      request.setmethodParams(key, params.get(key));
    }
    try {
      jmsTemplate.convertAndSend(queueName, request);

    } catch (JmsException e) {
      logger.info("Error when sending message to broker");
      throw new BrokerException(e.getMessage());
    }
  }
}
