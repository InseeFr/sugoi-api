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
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResultEntry;
import fr.insee.sugoi.core.seealso.SeeAlsoDecorator;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LdapSeeAlsoDecorator implements SeeAlsoDecorator {

  @Override
  public List<String> getProtocols() {
    return List.of("ldap");
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
    return transformLdapResponseToValue(searchResultEntry, subobject);
  }

  private SearchResultEntry getResourceFromLdapURL(String url) {
    try (LDAPConnection ldapConnection = new LDAPConnection()) {
      String portString = url.substring(url.lastIndexOf(":") + 1, url.lastIndexOf("/"));
      int ldapPort = portString.matches("-?\\d+") ? Integer.parseInt(portString) : 389;
      String host =
          url.substring(
              url.indexOf("/") + 2,
              url.lastIndexOf(":") != -1 ? url.lastIndexOf(":") : url.lastIndexOf("/"));
      String dn = url.substring(url.lastIndexOf("/") + 1);
      ldapConnection.connect(host, ldapPort, 1000);
      return ldapConnection.getEntry(dn);
    } catch (LDAPException e) {
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
}
