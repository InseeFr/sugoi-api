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
package fr.insee.sugoi.app;

import fr.insee.sugoi.app.service.BrokerEmbeddedService;
import fr.insee.sugoi.app.service.LdapEmbeddedService;
import fr.insee.sugoi.app.service.TomcatEmbeddedService;
import fr.insee.sugoi.app.service.utils.PropertiesLoaderService;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class SugoiTestService {

  private static boolean fork;

  private static boolean enabledEmbeddedLdap =
      PropertiesLoaderService.load("fr.insee.sugoi.full.env.ldap.embedded.enabled").equals("true");

  private static boolean enabledEmbeddedBroker =
      PropertiesLoaderService.load("fr.insee.sugoi.full.env.broker.embedded.enabled")
          .equals("true");

  private static boolean enabledEmbeddedTomcat1 =
      PropertiesLoaderService.load("fr.insee.sugoi.full.env.tomcat1.embedded.enabled")
          .equals("true");
  private static int httpPortEmbeddedTomcat1 =
      Integer.parseInt(PropertiesLoaderService.load("fr.insee.sugoi.full.env.tomcat1.port.http"));
  private static int httpsPortEmbeddedTomcat1 =
      Integer.parseInt(PropertiesLoaderService.load("fr.insee.sugoi.full.env.tomcat1.port.https"));
  private static String nameEmbeddedTomcat1 =
      PropertiesLoaderService.load("fr.insee.sugoi.full.env.tomcat1.name");
  private static String propertiesFileEmbeddedTomcat1 =
      PropertiesLoaderService.load("fr.insee.sugoi.full.env.tomcat1.properties.file");

  private static boolean enabledEmbeddedTomcat2 =
      PropertiesLoaderService.load("fr.insee.sugoi.full.env.tomcat2.embedded.enabled")
          .equals("true");
  private static int httpPortEmbeddedTomcat2 =
      Integer.parseInt(PropertiesLoaderService.load("fr.insee.sugoi.full.env.tomcat2.port.http"));
  private static int httpsPortEmbeddedTomcat2 =
      Integer.parseInt(PropertiesLoaderService.load("fr.insee.sugoi.full.env.tomcat2.port.https"));
  private static String nameEmbeddedTomcat2 =
      PropertiesLoaderService.load("fr.insee.sugoi.full.env.tomcat2.name");
  private static String propertiesFileEmbeddedTomcat2 =
      PropertiesLoaderService.load("fr.insee.sugoi.full.env.tomcat2.properties.file");

  /**
   * Démarrage des services de test.
   *
   * @throws JAXBException
   */
  public static void main(String[] args) throws InterruptedException, IOException {

    if (args.length == 0 || "start".equalsIgnoreCase(args[0])) {
      if (args.length > 1 && args[1].equals("fork")) {
        fork = true;
      }
      startServers();
    }
    if (args.length > 0 && "stop".equalsIgnoreCase(args[0])) {
      Socket socket = new Socket("localhost", 4567);
      socket.getInputStream();
      socket.close();
    }
  }

  private static void startServers() throws InterruptedException {
    ExecutorService execs =
        Executors.newFixedThreadPool(
            4,
            new ThreadFactory() {

              @Override
              public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                return thread;
              }
            });
    if (enabledEmbeddedLdap) {
      execs.submit(
          () -> {
            try {
              LdapEmbeddedService.start();
            } catch (Exception e) {
              e.printStackTrace();
            }
          });
    }
    if (enabledEmbeddedBroker) {
      execs.submit(
          () -> {
            try {
              BrokerEmbeddedService.start();
            } catch (Exception e) {
              e.printStackTrace();
            }
          });
    }
    if (enabledEmbeddedTomcat1) {
      execs.submit(
          () -> {
            try {
              TomcatEmbeddedService.start(
                  nameEmbeddedTomcat1,
                  httpPortEmbeddedTomcat1,
                  httpsPortEmbeddedTomcat1,
                  propertiesFileEmbeddedTomcat1);
            } catch (Exception e) {
              e.printStackTrace();
            }
          });
    }
    if (enabledEmbeddedTomcat2) {
      execs.submit(
          () -> {
            try {
              TomcatEmbeddedService.start(
                  nameEmbeddedTomcat2,
                  httpPortEmbeddedTomcat2,
                  httpsPortEmbeddedTomcat2,
                  propertiesFileEmbeddedTomcat2);
            } catch (Exception e) {
              e.printStackTrace();
            }
          });
    }
    execs.submit(
        () -> {
          try {
            ServerSocket shutdownSocket = new ServerSocket(4567);
            Socket sock = shutdownSocket.accept();
            sock.close();
            shutdownSocket.close();
            stopAll();
          } catch (Exception e) {
            e.printStackTrace();
          }
        });

    if (!fork) {
      execs.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }
  }

  private static void stopAll() throws Exception {

    TomcatEmbeddedService.stopAll();

    if (enabledEmbeddedLdap) {
      LdapEmbeddedService.stop();
    }

    if (enabledEmbeddedBroker) {
      BrokerEmbeddedService.stop();
    }
  }
}
