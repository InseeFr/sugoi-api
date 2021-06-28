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
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.controls.ServerSideSortRequestControl;
import com.unboundid.ldap.sdk.controls.SimplePagedResultsControl;
import com.unboundid.ldap.sdk.controls.SortKey;
import com.unboundid.ldap.sdk.controls.VirtualListViewRequestControl;
import com.unboundid.ldap.sdk.controls.VirtualListViewResponseControl;
import com.unboundid.util.Base64;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
          page.setSearchToken(Base64.encode(cookie.getValue()));
          page.setHasMoreResult(true);
          page.setPageSize(searchResult.getEntryCount());
          // On met -1 car 0 par defaut et on veut preciser que la taille n'a pas ete
          // fournie
          page.setTotalElements(prc.getSize() == 0 ? -1 : prc.getSize());
        }
      }
      if (control instanceof VirtualListViewResponseControl) {
        VirtualListViewResponseControl vlvc = (VirtualListViewResponseControl) control;
        page.setPageSize(searchResult.getEntryCount());
        page.setHasMoreResult(true);
        page.setTotalElements(vlvc.getContentCount());
        page.setSearchToken(vlvc.getContextID().stringValue());
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

  public static void setRequestControls(
      SearchRequest searchRequest, PageableResult pageable, Map<String, String> config) {
    if (config.get(LdapConfigKeys.VLV_ENABLED).equalsIgnoreCase("true")) {
      // TODO test if this code works on vlv
      searchRequest.addControl(
          new ServerSideSortRequestControl(
              new SortKey(config.get(LdapConfigKeys.SORT_KEY), "caseExactOrderingMatch", false)));
      if (pageable.getFirst() == 0) {
        pageable.setOffset(1);
      }
      searchRequest.addControl(
          new VirtualListViewRequestControl(
              pageable.getFirst(),
              0,
              pageable.getSize() - 1,
              0,
              pageable.getSearchToken() != null
                  ? new ASN1OctetString(pageable.getSearchToken())
                  : null));

    } else {
      ASN1OctetString cookie = null;
      try {
        cookie =
            pageable.getSearchToken() != null
                ? new ASN1OctetString(Base64.decode(pageable.getSearchToken()))
                : null;
      } catch (ParseException e) {
        e.printStackTrace();
      }
      searchRequest.addControl(new SimplePagedResultsControl(pageable.getSize(), cookie, true));
    }
  }

  public static List<Modification> convertAttributesToModifications(List<Attribute> attributes) {
    return attributes.stream()
        .map(attribute -> attribute.getName())
        .filter(attributeName -> !attributeName.equalsIgnoreCase("objectClass"))
        .distinct()
        .map(
            attributeName ->
                new Modification(
                    ModificationType.REPLACE,
                    attributeName,
                    attributes.stream()
                        .filter(
                            filterAttribute ->
                                filterAttribute.getName().equalsIgnoreCase(attributeName))
                        .map(filterAttribute -> filterAttribute.getValues())
                        .flatMap(values -> Arrays.stream(values))
                        .toArray(String[]::new)))
        .collect(Collectors.toList());
  }
}
