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
package fr.insee.sugoi.seealso;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPURL;
import com.unboundid.ldap.sdk.SearchResultEntry;
import fr.insee.sugoi.core.seealso.SeeAlsoCredentialsConfiguration.SeeAlsoCredential;
import fr.insee.sugoi.core.seealso.SeeAlsoDecorator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LdapSeeAlsoDecorator implements SeeAlsoDecorator {

  private static final Map<String, LDAPConnectionPool> ldapConnectionByHost = new HashMap<>();
  private static final Logger logger = LoggerFactory.getLogger(LdapSeeAlsoDecorator.class);

  @Autowired Map<String, SeeAlsoCredential> credentialsByDomain;

  @Override
  public List<String> getProtocols() {
    return List.of("ldap", "ldaps");
  }

  /**
   * Deal with ldap requests.
   *
   * @param url Ldap URL without attribute (ex :
   *     ldap://localhost:10389/uid=testc,ou=contacts,ou=clients_domaine1,o=insee,c=fr)
   * @param subobject the attribute to extract
   * @return an Object that can be a String or a List<String> if attributes have several values.
   */
  @Override
  public Object getResourceFromUrl(String url, String subobject) {
    SearchResultEntry searchResultEntry = getResourceFromLdapURL(url);
    if (searchResultEntry != null) {
      return transformLdapResponseToValue(searchResultEntry, subobject);
    } else {
      return null;
    }
  }

  private SearchResultEntry getResourceFromLdapURL(String url) {
    try {
      LDAPURL ldapUrl = new LDAPURL(url);
      return getConnectionByHost(ldapUrl.getHost(), ldapUrl.getPort())
          .searchForEntry(ldapUrl.toSearchRequest());
    } catch (LDAPException e) {
      logger.error("Ldap request for seealso failed with : {}", e.getLocalizedMessage());
      return null;
    }
  }

  private Object transformLdapResponseToValue(
      SearchResultEntry searchResultEntry, String subobject) {
    Attribute attribute = searchResultEntry.getAttribute(subobject);
    if (attribute.getValues().length > 1) {
      return Arrays.stream(attribute.getValues()).collect(Collectors.toList());
    } else {
      return attribute.getValue();
    }
  }

  private LDAPConnectionPool getConnectionByHost(String host, int port) throws LDAPException {
    ldapConnectionByHost.putIfAbsent(host + "_" + port, createHostConnection(host, port));
    return ldapConnectionByHost.get(host + "_" + port);
  }

  private LDAPConnectionPool createHostConnection(String host, int port) throws LDAPException {
    if (credentialsByDomain != null && credentialsByDomain.containsKey(host)) {
      try (LDAPConnection initialConnection =
          new LDAPConnection(
              host,
              port,
              credentialsByDomain.get(host).getUsername(),
              credentialsByDomain.get(host).getPassword())) {
        return new LDAPConnectionPool(initialConnection, 10);
      }
    } else {
      try (LDAPConnection initialConnection = new LDAPConnection(host, port)) {
        return new LDAPConnectionPool(initialConnection, 10);
      }
    }
  }
}
