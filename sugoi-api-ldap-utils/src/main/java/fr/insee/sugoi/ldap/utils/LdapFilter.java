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

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;
import java.util.List;

public class LdapFilter {

  public static Filter exists(String property) {
    return Filter.createPresenceFilter(property);
  }

  public static Filter contains(String property, String value) {
    return property.equalsIgnoreCase("objectclass")
        ? Filter.createEqualityFilter("objectclass", value)
        : Filter.createSubAnyFilter(property, value);
  }

  public static Filter equalsProperty(String property, String value) {
    return Filter.createEqualityFilter(property, value);
  }

  public static Filter likeFilterOrganisation(String value) {
    return Filter.createSubInitialFilter("inseeOrganisationDN", "uid=" + encodeString(value));
  }

  public static Filter lessThan(String property, String value) {
    return Filter.createLessOrEqualFilter(property, value);
  }

  public static Filter greaterThan(String property, String value) {
    return Filter.createGreaterOrEqualFilter(property, value);
  }

  public static Filter and(List<Filter> andFilterCollection) {
    return Filter.createANDFilter(andFilterCollection);
  }

  public static Filter or(List<Filter> orFilterCollection) {
    return Filter.createORFilter(orFilterCollection);
  }

  public static Filter not(Filter filter) {
    return Filter.createNOTFilter(filter);
  }

  public static Filter create(String filter) throws LDAPException {
    return Filter.create(filter);
  }

  private static String encodeString(String propertyValue) {
    return Filter.encodeValue(propertyValue);
  }
}
