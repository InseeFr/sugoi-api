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

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.schema.Schema;
import com.unboundid.ldif.LDIFException;
import fr.insee.sugoi.app.service.utils.PropertiesLoaderService;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.util.logging.ConsoleHandler;

public class LdapEmbeddedService {

  private static int port =
      Integer.parseInt(PropertiesLoaderService.load("fr.insee.sugoi.full.env.ldap.embedded.port"));
  private static String ldifPath =
      PropertiesLoaderService.load("fr.insee.sugoi.full.env.ldap.embedded.ldif.path");
  private static String schemaPath =
      PropertiesLoaderService.load("fr.insee.sugoi.full.env.ldap.embedded.schema.path");
  private static String baseDn =
      PropertiesLoaderService.load("fr.insee.sugoi.full.env.ldap.embedded.base.dn");
  private static String username =
      PropertiesLoaderService.load("fr.insee.sugoi.full.env.ldap.embedded.username");
  private static String password =
      PropertiesLoaderService.load("fr.insee.sugoi.full.env.ldap.embedded.password");

  private static InMemoryDirectoryServer ds;

  public static void start() throws LDAPException, LDIFException, IOException {

    System.out.println("Start ldap server on port " + port);
    System.out.println("BaseDn used " + baseDn);
    System.out.println("Ldap username: " + username);
    System.out.println("Ldap password: " + password);
    System.out.println("Schema used: " + schemaPath);
    System.out.println("Ldif used: " + ldifPath);

    InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(baseDn);
    config.setAccessLogHandler(new ConsoleHandler());
    config.addAdditionalBindCredentials(username, password);
    File file =
        new File(
            URLDecoder.decode(LdapEmbeddedService.class.getResource(schemaPath).getFile(), "UTF8"));
    config.setSchema(Schema.getSchema(file));
    config.setEnforceSingleStructuralObjectClass(false);
    config.setEnforceAttributeSyntaxCompliance(false);
    config.setGenerateOperationalAttributes(true);

    InMemoryListenerConfig listenerConfig =
        new InMemoryListenerConfig(
            "listen-" + port, InetAddress.getLoopbackAddress(), port, null, null, null);
    config.setListenerConfigs(listenerConfig);

    ds = new InMemoryDirectoryServer(config);
    ds.importFromLDIF(
        true, URLDecoder.decode(LdapEmbeddedService.class.getResource(ldifPath).getFile(), "UTF8"));

    System.out.println("Started Ldap Server (localhost:" + port + ")");
    ds.startListening();
  }

  public static void stop() {
    ds.shutDown(true);
  }
}
