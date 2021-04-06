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
package fr.insee.sugoi.store.ldap;

import com.unboundid.ldap.sdk.LDAPConnectionPool;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.ldap.utils.mapper.ApplicationLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.GroupLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.OrganizationLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.UserLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.properties.AddressLdap;
import fr.insee.sugoi.ldap.utils.mapper.properties.ApplicationLdap;
import fr.insee.sugoi.ldap.utils.mapper.properties.GroupLdap;
import fr.insee.sugoi.ldap.utils.mapper.properties.LdapObjectClass;
import fr.insee.sugoi.ldap.utils.mapper.properties.OrganizationLdap;
import fr.insee.sugoi.ldap.utils.mapper.properties.UserLdap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LdapStore {

  protected LDAPConnectionPool ldapPoolConnection;

  protected static final Logger logger = LogManager.getLogger(LdapReaderStore.class);

  protected UserLdapMapper userLdapMapper;
  protected OrganizationLdapMapper organizationLdapMapper;
  protected GroupLdapMapper groupLdapMapper;
  protected ApplicationLdapMapper applicationLdapMapper;

  protected Map<String, String> config;

  protected String getGroupSource(String appName) {
    return config.get(LdapConfigKeys.GROUP_SOURCE_PATTERN).replace("{appliname}", appName);
  }

  protected String getGroupWildcardFilter(String appName) {
    return config
        .get(LdapConfigKeys.GROUP_FILTER_PATTERN)
        .replace("{appliname}", appName)
        .replace("{group}", "*");
  }

  protected String getApplicationDN(String applicationName) {
    return String.format(
        "%s=%s,%s",
        ApplicationLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
        applicationName,
        config.get(LdapConfigKeys.APP_SOURCE));
  }

  protected String getGroupDN(String applicationName, String groupName) {
    return String.format(
        "%s=%s,%s",
        GroupLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
        groupName,
        getGroupSource(applicationName));
  }

  protected String getOrganizationDN(String organizationId) {
    return String.format(
        "%s=%s,%s",
        OrganizationLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
        organizationId,
        config.get(LdapConfigKeys.ORGANIZATION_SOURCE));
  }

  protected String getUserDN(String username) {
    return String.format(
        "%s=%s,%s",
        UserLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
        username,
        config.get(LdapConfigKeys.USER_SOURCE));
  }

  protected String getAddressDN(String addressId) {
    return String.format(
        "%s=%s,%s",
        AddressLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
        addressId,
        config.get(LdapConfigKeys.ADDRESS_SOURCE));
  }
}
