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

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.ldap.utils.mapper.AddressLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.ApplicationLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.GroupLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.OrganizationLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.UserLdapMapper;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapStore {

  protected LDAPConnectionPool ldapPoolConnection;
  protected LDAPConnection ldapMonoConnection;

  protected static final Logger logger = LoggerFactory.getLogger(LdapStore.class);

  protected UserLdapMapper userLdapMapper;
  protected OrganizationLdapMapper organizationLdapMapper;
  protected GroupLdapMapper groupLdapMapper;
  protected ApplicationLdapMapper applicationLdapMapper;
  protected AddressLdapMapper addressLdapMapper;

  protected Map<String, String> config;

  protected String getGroupSource(String appName) {
    if (StringUtils.isNotBlank(config.get(LdapConfigKeys.GROUP_SOURCE_PATTERN))) {
      return config.get(LdapConfigKeys.GROUP_SOURCE_PATTERN).replace("{appliname}", appName);
    } else {
      throw new UnsupportedOperationException("Group feature is not set for this userstorage");
    }
  }

  protected String getGroupManagerSource(String appName) {
    if (StringUtils.isNotBlank(config.get(LdapConfigKeys.GROUP_MANAGER_SOURCE_PATTERN))) {
      return config
          .get(LdapConfigKeys.GROUP_MANAGER_SOURCE_PATTERN)
          .replace("{appliname}", appName);
    } else {
      throw new UnsupportedOperationException(
          "Group manager feature is not set for this userstorage");
    }
  }

  protected String getGroupWildcardFilter(String appName) {
    if (StringUtils.isNotBlank(config.get(LdapConfigKeys.GROUP_FILTER_PATTERN))) {
      return config
          .get(LdapConfigKeys.GROUP_FILTER_PATTERN)
          .replace("{appliname}", appName)
          .replace("{group}", "*");
    } else {
      throw new UnsupportedOperationException("Group feature is not set for this userstorage");
    }
  }

  protected boolean matchGroupWildcardPattern(String appName, String groupName) {
    if (StringUtils.isNotBlank(config.get(LdapConfigKeys.GROUP_FILTER_PATTERN))) {
      String dnPattern =
          config
              .get(LdapConfigKeys.GROUP_FILTER_PATTERN)
              .replace("{appliname}", appName)
              .replace("{group}", ".*");
      String simplePattern =
          dnPattern.substring(dnPattern.indexOf("=") + 1, dnPattern.length() - 1);
      return groupName.toLowerCase().matches(simplePattern.toLowerCase());
    } else {
      throw new UnsupportedOperationException("Group feature is not set for this userstorage");
    }
  }

  protected String getApplicationDN(String applicationName) {
    if (StringUtils.isNotBlank(config.get(GlobalKeysConfig.APP_SOURCE))) {
      return String.format(
          "%s=%s,%s",
          // TODO should be a param
          "ou",
          //
          applicationName,
          config.get(GlobalKeysConfig.APP_SOURCE));
    } else {
      throw new UnsupportedOperationException("Applications feature not configured for this realm");
    }
  }

  protected String getGroupDN(String applicationName, String groupName) {
    return String.format(
        "%s=%s,%s",
        // TODO should be a param
        "cn",
        //
        groupName,
        getGroupSource(applicationName));
  }

  protected String getOrganizationDN(String organizationId) {
    if (StringUtils.isNotBlank(config.get(GlobalKeysConfig.ORGANIZATION_SOURCE))) {
      return String.format(
          "%s=%s,%s", // TODO should be a param
          "uid",
          //
          organizationId,
          config.get(GlobalKeysConfig.ORGANIZATION_SOURCE));
    } else {
      throw new UnsupportedOperationException(
          "Organizations feature not configured for this storage");
    }
  }

  protected String getUserDN(String username) {
    return String.format(
        "%s=%s,%s",
        // TODO should be a param
        "uid",
        //
        username,
        config.get(GlobalKeysConfig.USER_SOURCE));
  }

  protected String getAddressDN(String addressId) {
    return String.format(
        "%s=%s,%s",
        // TODO should be a param
        "l",
        //
        addressId,
        config.get(GlobalKeysConfig.ADDRESS_SOURCE));
  }
}
