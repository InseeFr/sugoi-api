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
package fr.insee.sugoi.ldap.utils;

import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.controls.SimplePagedResultsControl;
import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LdapUtils {

  private static final Logger logger = LogManager.getLogger(LdapUtils.class);

  public static <T> void setResponseControls(PageResult<T> page, SearchResult searchResult) {
    Control[] responseControl = searchResult.getResponseControls();
    if (responseControl == null) {
      return;
    }
    for (Control control : responseControl) {
      if (control instanceof SimplePagedResultsControl) {
        SimplePagedResultsControl prc = (SimplePagedResultsControl) control;
        if (prc.getCookie().getValueLength() > 0) {
          ASN1OctetString cookie = prc.getCookie();
          page.setSearchCookie(cookie.getValue());
          page.setHasMoreResult(true);
          // On met -1 car 0 par defaut et on veut preciser que la taille n'a pas ete
          // fournie
          page.setTotalElements(prc.getSize() == 0 ? -1 : prc.getSize());
        }
      }
    }
  }

  public static String getNodeValueFromDN(String dn) {
    try {
      return (new DN(dn)).getRDN().getAttributeValues()[0];
    } catch (LDAPException e) {
      logger.info(String.format("%s is not a DN", dn));
      return null;
    }
  }

  public static void setRequestControls(SearchRequest searchRequest, PageableResult pageable) {
    ASN1OctetString cookie = new ASN1OctetString(pageable.getCookie());
    searchRequest.addControl(new SimplePagedResultsControl(pageable.getSize(), cookie, true));
  }
}
