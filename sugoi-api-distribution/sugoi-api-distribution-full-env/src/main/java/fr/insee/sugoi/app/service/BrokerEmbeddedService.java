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
package fr.insee.sugoi.app.service;

import fr.insee.sugoi.app.service.utils.PropertiesLoaderService;
import fr.insee.sugoi.app.service.utils.UserDirService;
import java.net.URI;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;

public class BrokerEmbeddedService {

  private static BrokerService broker;
  private static int port =
      Integer.parseInt(
          PropertiesLoaderService.load("fr.insee.sugoi.full.env.broker.embedded.port"));
  private static String dataBase =
      PropertiesLoaderService.load("fr.insee.sugoi.full.env.broker.embedded.database");

  public static void start() throws Exception {
    System.out.println("Start Broker on port " + port);
    System.out.println(
        "Created broker database in " + UserDirService.getUserDir() + "/target/" + dataBase);
    broker = new BrokerService();
    // configure the broker
    TransportConnector connector = new TransportConnector();
    connector.setUri(new URI("tcp://localhost:" + port));
    broker.addConnector(connector);
    broker.setBrokerName("localhost");
    broker.setUseJmx(false);
    broker.setDataDirectory(UserDirService.getUserDir() + "/target/" + dataBase);
    // broker.setStartAsync(true);
    broker.setNetworkConnectorStartAsync(true);
    broker.start();
    System.out.println("Started Broker (localhost:" + port + ")");
    // broker.setWaitForSlave(false);
    broker.toString();
  }

  public static void stop() throws Exception {
    broker.stop();
  }
}
