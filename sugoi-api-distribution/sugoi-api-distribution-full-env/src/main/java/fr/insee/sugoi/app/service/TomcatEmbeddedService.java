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

import fr.insee.sugoi.app.SugoiTestService;
import fr.insee.sugoi.app.service.utils.UserDirService;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.RemoteIpValve;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.net.SSLHostConfig;

public class TomcatEmbeddedService {

  private static Map<String, Tomcat> tomcats = new HashMap<>();

  private static void launchTomcat(
      String name, String warURL, int httpPort, int httpsPort, String configFile)
      throws ServletException, IOException {

    String workUri = getWorkUri();

    File tomcatDir = new File(workUri + "/tomcatit/" + name);
    tomcatDir.mkdirs();

    File webapps = new File(workUri + "/tomcatit/" + name + "/webapps");
    webapps.mkdir();

    Tomcat tomcat = new Tomcat();
    tomcat.setBaseDir(tomcatDir.getAbsolutePath());

    // Connecteur HTTP
    Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
    connector.setPort(httpPort);
    connector.setScheme("http");
    connector.setSecure(false);
    connector.setProxyName(name);
    connector.setRedirectPort(httpsPort);
    tomcat.getService().addConnector(connector);

    // Connecteur HTTPS
    Connector connectorHttps = new Connector("org.apache.coyote.http11.Http11NioProtocol");
    connectorHttps.setPort(httpsPort);
    connectorHttps.setScheme("https");
    connectorHttps.setSecure(true);
    connectorHttps.setProperty("SSLEnabled", "true");
    connectorHttps.setProxyName(name);
    SSLHostConfig sslHostConfig = new SSLHostConfig();
    sslHostConfig.setSslProtocol("TLS");
    File keystoreFile =
        new File(
            URLDecoder.decode(
                SugoiTestService.class.getResource("/ssl/server.p12").getFile(), "UTF8"));
    sslHostConfig.setCertificateKeystoreFile(keystoreFile.getAbsolutePath());
    sslHostConfig.setCertificateKeystorePassword("changeit");
    sslHostConfig.setCertificateKeystoreType("PKCS12");
    connectorHttps.addSslHostConfig(sslHostConfig);
    tomcat.getService().addConnector(connectorHttps);

    // Valve pour HTTPS via HAProxy
    RemoteIpValve remoteIpValve = new RemoteIpValve();
    remoteIpValve.setProtocolHeader("X-Forwarded-Proto");

    tomcat.getHost().getPipeline().addValve(remoteIpValve);

    Context ctx = tomcat.addWebapp("", new File(warURL).getAbsolutePath());

    try {
      tomcat.start();
    } catch (LifecycleException e1) {
      e1.printStackTrace();
    }

    while (tomcat.getServer().getState() != LifecycleState.STARTED) {
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    // Copy properties on tomcat and reload
    FileUtils.copyFile(
        new File(workUri + "/../src/main/resources" + configFile),
        new File(
            workUri
                + "/tomcatit/"
                + name
                + "/webapps/ROOT/WEB-INF/classes/application.properties"));
    ctx.reload();

    while (tomcat.getServer().getState() != LifecycleState.STARTED) {
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    tomcat.getServer().await();
  }

  private static String getWorkUri() {
    File folder =
        new File(
            Paths.get(System.getProperty("user.dir")).toAbsolutePath()
                + "/sugoi-api-distribution/sugoi-api-distribution-war/target/");
    String workUri;
    if (folder.exists()) {
      workUri =
          Paths.get(System.getProperty("user.dir")).toAbsolutePath()
              + "/sugoi-api-distribution/sugoi-api-distribution-full-env/target/";
    } else {
      workUri =
          Paths.get(System.getProperty("user.dir")).toAbsolutePath()
              + "/../../sugoi-api-distribution/sugoi-api-distribution-full-env/target/";
    }
    return workUri;
  }

  public static Boolean start(String name, int httpPort, int httpsPort, String propertiesFile)
      throws Exception {
    launchTomcat(name, getWarUri(), httpPort, httpsPort, propertiesFile);
    return true;
  }

  public void stop(String name) {
    try {
      tomcats.get(name).stop();
    } catch (Exception e) {
    }
  }

  public static void stopAll() throws LifecycleException {
    for (String tomcatName : new ArrayList<String>(tomcats.keySet())) {
      tomcats.get(tomcatName).stop();
    }
  }

  public static String getWarUri() throws Exception {
    Path userDir = UserDirService.getUserDir();
    String warUri;
    File folder =
        new File(
            userDir.toAbsolutePath()
                + "/sugoi-api-distribution/sugoi-api-distribution-war/target/");
    if (folder.exists()) {
      warUri =
          userDir.toAbsolutePath()
              + "/sugoi-api-distribution/sugoi-api-distribution-war/target/sugoi-api.war";
    } else {
      warUri =
          userDir.toAbsolutePath()
              + "/../../sugoi-api-distribution/sugoi-api-distribution-war/target/sugoi-api.war";
    }
    if (!new File(warUri).exists()) {
      throw new Exception("War not present");
    }
    return warUri;
  }
}
