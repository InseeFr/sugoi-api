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

import fr.insee.sugoi.jms.exception.BrokerException;
import fr.insee.sugoi.jms.writer.JmsWriter;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class JmsTests {

  @Autowired private JmsWriter jmsWriter;

  private static BrokerService broker;

  public static void launchBroker() throws Exception {
    Path userDir = Paths.get(System.getProperty("user.dir"));

    if (userDir.endsWith("sugoi-api")) {
      userDir = userDir.resolve("sugoi-api-jms-utils");
    }

    broker = new BrokerService();
    TransportConnector connector = new TransportConnector();
    connector.setUri(new URI("tcp://localhost:" + 61616));
    broker.addConnector(connector);
    broker.setBrokerName("localhost");
    broker.setDataDirectory(userDir + "/target/active-mq/database");
    broker.setUseJmx(false);
    broker.setNetworkConnectorStartAsync(true);
    broker.start();
  }

  @Test
  public void testWithNoBroker() throws Exception {
    Throwable exception =
        Assertions.assertThrows(
            BrokerException.class,
            () -> {
              Map<String, Object> params = new HashMap<>();
              jmsWriter.writeInQueue("queueTests", "toto", params);
            });
  }

  public static void stopBroker() throws Exception {
    broker.stop();
  }
}
