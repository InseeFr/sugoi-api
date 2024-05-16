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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.RemoteIpValve;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.apache.tomcat.util.scan.StandardJarScanFilter;

public class TomcatEmbeddedService {

  private static Map<String, Tomcat> tomcats = new HashMap<>();

  private static void launchTomcat(
      String name,
      String configFile,
      String name2,
      String configFile2,
      String warURL,
      int httpPort,
      int httpsPort)
      throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {

    String workUri = getWorkUri();

    File tomcatDir = new File(workUri + "/tomcatit/");
    tomcatDir.mkdirs();

    File webapps = new File(workUri + "/tomcatit/webapps");
    webapps.mkdir();

    Tomcat tomcat = new Tomcat();
    tomcat.setBaseDir(tomcatDir.getAbsolutePath());

    // Connecteur HTTP
    Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
    connector.setPort(httpPort);
    connector.setScheme("http");
    connector.setSecure(false);
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
        new File(UserDirService.getUserDir() + "/src/main/resources/ssl/server.p12");
    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    keyStore.load(new FileInputStream(keystoreFile.getAbsolutePath()), "changeit".toCharArray());
    sslHostConfig.setTrustStore(keyStore);
    SSLHostConfigCertificate certificate =
        new SSLHostConfigCertificate(new SSLHostConfig(), SSLHostConfigCertificate.Type.RSA);
    certificate.setCertificateKeystore(keyStore);
    sslHostConfig.addCertificate(certificate);
    connectorHttps.addSslHostConfig(sslHostConfig);
    tomcat.getService().addConnector(connectorHttps);

    // Valve pour HTTPS via HAProxy
    RemoteIpValve remoteIpValve = new RemoteIpValve();
    remoteIpValve.setProtocolHeader("X-Forwarded-Proto");
    tomcat.getHost().getPipeline().addValve(remoteIpValve);

    Context ctx = tomcat.addWebapp("/" + name, new File(warURL).getAbsolutePath());
    Context ctx2 = tomcat.addWebapp("/" + name2, new File(warURL).getAbsolutePath());
    ctx.getJarScanner()
        .setJarScanFilter(
            new StandardJarScanFilter() {
              @Override
              public boolean check(JarScanType jarScanType, String jarName) {
                /**/
                if (jarName.contains("xalan")
                    || jarName.contains("xml")
                    || jarName.contains("jakarta") | jarName.contains("asm")
                    || jarName.contains("hk2")
                    || jarName.contains("class")) {
                  return false;
                }
                return super.check(jarScanType, jarName);
              }
            });
    ctx2.getJarScanner()
        .setJarScanFilter(
            new StandardJarScanFilter() {
              @Override
              public boolean check(JarScanType jarScanType, String jarName) {
                /**/
                if (jarName.contains("xalan")
                    || jarName.contains("xml")
                    || jarName.contains("jakarta")
                    || jarName.contains("asm")
                    || jarName.contains("hk2")
                    || jarName.contains("class")) {
                  return false;
                }
                return super.check(jarScanType, jarName);
              }
            });
    try {
      tomcat.start();
    } catch (LifecycleException e1) {
      e1.printStackTrace();
    }

    while (tomcat.getServer().getState() != LifecycleState.STARTED) {
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    // Copy properties on tomcat and reload
    FileUtils.copyFile(
        new File(UserDirService.getUserDir() + configFile),
        new File(
            workUri + "/tomcatit/webapps/" + name + "/WEB-INF/classes/application.properties"));
    ctx.reload();
    FileUtils.copyFile(
        new File(UserDirService.getUserDir() + configFile2),
        new File(
            workUri + "/tomcatit/webapps/" + name2 + "/WEB-INF/classes/application.properties"));
    ctx2.reload();

    while (tomcat.getServer().getState() != LifecycleState.STARTED) {
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    tomcat.getServer().await();
  }

  private static String getWorkUri() {
    return UserDirService.getUserDir() + "/target/";
  }

  public static Boolean start(
      String name,
      String propertiesFile,
      String name2,
      String propertiesFile2,
      int httpPort,
      int httpsPort)
      throws Exception {
    launchTomcat(name, propertiesFile, name2, propertiesFile2, getWarUri(), httpPort, httpsPort);
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
    String warRelativePath =
        PropertiesLoaderService.load(
            "fr.insee.sugoi.full.env.war.relative.path",
            "/sugoi-api-distribution/sugoi-api-distribution-war/target/");
    Path userDir = UserDirService.getUserDir();
    return userDir + warRelativePath;
  }
}
