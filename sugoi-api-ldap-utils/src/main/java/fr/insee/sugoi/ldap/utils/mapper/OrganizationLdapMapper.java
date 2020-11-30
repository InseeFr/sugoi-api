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
package fr.insee.sugoi.ldap.utils.mapper;

import com.unboundid.ldap.sdk.SearchResultEntry;
import fr.insee.sugoi.core.mapper.properties.OrganizationLdap;
import fr.insee.sugoi.model.Organization;

public class OrganizationLdapMapper {

  public static Organization mapFromSearchEntry(SearchResultEntry searchResultEntry) {
    Organization org =
        GenericLdapMapper.transform(searchResultEntry, OrganizationLdap.class, Organization.class);
    org.setGpgkey(searchResultEntry.getAttribute("inseeClefChiffrement").getValueByteArray());
    return org;
  }
}
