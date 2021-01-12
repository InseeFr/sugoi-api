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

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.SearchResultEntry;
import fr.insee.sugoi.ldap.utils.mapper.properties.ApplicationLdap;
import fr.insee.sugoi.model.Application;
import java.util.List;

public class ApplicationLdapMapper implements LdapMapper<Application> {

  public Application mapFromSearchEntry(SearchResultEntry searchResultEntry) {
    return GenericLdapMapper.transform(searchResultEntry, ApplicationLdap.class, Application.class);
  }

  public List<Attribute> mapToAttribute(Application application) {
    return GenericLdapMapper.toAttribute(application, ApplicationLdap.class, Application.class);
  }

  public static List<Modification> createMods(Application updatedApplication) {
    return GenericLdapMapper.createMods(
        updatedApplication, ApplicationLdap.class, Application.class);
  }
}
