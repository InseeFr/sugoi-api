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

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.store.ReaderStore;
import fr.insee.sugoi.ldap.utils.LdapFactory;
import fr.insee.sugoi.ldap.utils.LdapUtils;
import fr.insee.sugoi.ldap.utils.mapper.AddressLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.ApplicationLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.GroupLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.OrganizationLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.UserLdapMapper;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LdapReaderStore implements ReaderStore {

  private LDAPConnectionPool ldapPoolConnection;

  private static final Logger logger = LogManager.getLogger(LdapReaderStore.class);

  private Map<String, String> config;

  public LdapReaderStore(Map<String, String> config) {
    logger.debug("Configuring LdapReaderStore with config : {}", config);
    try {
      LdapFactory.getSingleConnection(config);
      this.ldapPoolConnection = LdapFactory.getConnectionPool(config);
      this.config = config;
    } catch (LDAPException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public User getUser(String id) {
    logger.debug("Searching user {}", id);
    SearchResultEntry entry = getEntryByDn("uid=" + id + "," + config.get("user_source"));
    if (entry != null) {
      return UserLdapMapper.mapFromSearchEntry(entry);
    } else {
      return null;
    }
  }

  @Override
  public Organization getOrganization(String id) {
    SearchResultEntry entry = getEntryByDn("uid=" + id + "," + config.get("organization_source"));
    Organization org = (entry != null) ? OrganizationLdapMapper.mapFromSearchEntry(entry) : null;
    if (org != null
        && org.getAttributes().containsKey("adressDn")
        && org.getAttributes().get("adressDn") != null) {
      SearchResultEntry addressResult =
          getEntryByDn(org.getAttributes().get("adressDn").toString());
      org.setAddress(AddressLdapMapper.mapFromSearchEntry(addressResult));
    }
    return org;
  }

  @Override
  public PageResult<User> searchUsers(
      Map<String, String> properties, PageableResult pageable, String typeRecherche) {
    try {
      PageResult<User> page = new PageResult<>();
      Filter filter = LdapUtils.getFilterFromCriteria(properties);
      SearchRequest searchRequest =
          new SearchRequest(
              config.get("user_source"), SearchScope.SUBORDINATE_SUBTREE, filter, "*", "+");
      LdapUtils.setRequestControls(searchRequest, pageable);
      SearchResult searchResult = ldapPoolConnection.search(searchRequest);
      List<User> users =
          searchResult.getSearchEntries().stream()
              .map(e -> UserLdapMapper.mapFromSearchEntry(e))
              .collect(Collectors.toList());
      LdapUtils.setResponseControls(page, searchResult);
      page.setResults(users);
      return page;
    } catch (LDAPSearchException e) {
      throw new RuntimeException("Fail to execute user search", e);
    }
  }

  public SearchResultEntry getEntryByDn(String dn) {
    try {
      logger.debug("Fetching {}", dn);
      SearchResultEntry entry = ldapPoolConnection.getEntry(dn, "+", "*");

      return entry;
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to execute " + dn, e);
    }
  }

  @Override
  public PageResult<User> getUsersInGroup(String appName, String groupName) {
    PageResult<User> page = new PageResult<>();
    SearchResultEntry entry = getGroupResultEntry(appName, groupName);
    if (entry.hasAttribute("uniqueMember")) {
      page.setResults(
          Arrays.stream(entry.getAttribute("uniqueMember").getValues())
              .map(uniqueMember -> getUser(uniqueMember.split(",")[0].substring(4)))
              .collect(Collectors.toList()));
    } else {
      page.setResults(new ArrayList<>());
    }
    return page;
  }

  @Override
  public PageResult<Organization> searchOrganizations(
      Map<String, String> searchProperties, PageableResult pageable, String searchOperator) {
    Filter filter = LdapUtils.getFilterFromCriteria(searchProperties);
    try {
      SearchRequest searchRequest =
          new SearchRequest(
              config.get("organization_source"), SearchScope.SUBORDINATE_SUBTREE, filter, "*", "+");
      LdapUtils.setRequestControls(searchRequest, pageable);
      SearchResult searchResult = ldapPoolConnection.search(searchRequest);
      List<Organization> organizations =
          searchResult.getSearchEntries().stream()
              .map(e -> OrganizationLdapMapper.mapFromSearchEntry(e))
              .collect(Collectors.toList());
      PageResult<Organization> page = new PageResult<>();
      page.setResults(organizations);
      return page;
    } catch (LDAPSearchException e) {
      throw new RuntimeException("Fail to search organizations in ldap", e);
    }
  }

  @Override
  public Group getGroup(String appName, String groupName) {
    SearchResultEntry entry = getGroupResultEntry(appName, groupName);
    return (entry != null) ? GroupLdapMapper.mapFromSearchEntry(entry) : null;
  }

  private SearchResultEntry getGroupResultEntry(String appName, String groupName) {
    SearchRequest searchRequest =
        new SearchRequest(
            "ou=" + appName + "," + config.get("app_source"),
            SearchScope.SUBORDINATE_SUBTREE,
            Filter.createEqualityFilter("cn", groupName));
    try {
      SearchResult searchResult = ldapPoolConnection.search(searchRequest);
      if (searchResult.getEntryCount() == 1) {
        return searchResult.getSearchEntries().get(0);
      } else if (searchResult.getEntryCount() == 0) {
        logger.debug("No matching group found for " + groupName);
        return null;
      } else {
        logger.error("Found too many matches");
        return null;
      }
    } catch (LDAPSearchException e) {
      throw new RuntimeException("Fail to get group " + groupName + " in ldap", e);
    }
  }

  @Override
  public PageResult<Group> searchGroups(
      String appName,
      Map<String, String> searchProperties,
      PageableResult pageable,
      String searchOperator) {
    Filter isGroup = Filter.createSubAnyFilter("objectClass", "groupOfUniqueNames");
    Filter filter =
        Filter.createANDFilter(LdapUtils.getFilterFromCriteria(searchProperties), isGroup);
    try {
      SearchRequest searchRequest =
          new SearchRequest(
              "ou=" + appName + "," + config.get("app_source"),
              SearchScope.SUBORDINATE_SUBTREE,
              filter,
              "*",
              "+");
      LdapUtils.setRequestControls(searchRequest, pageable);
      SearchResult searchResult = ldapPoolConnection.search(searchRequest);
      List<Group> groups =
          searchResult.getSearchEntries().stream()
              .map(e -> GroupLdapMapper.mapFromSearchEntry(e))
              .collect(Collectors.toList());
      PageResult<Group> page = new PageResult<>();
      page.setResults(groups);
      return page;
    } catch (LDAPSearchException e) {
      throw new RuntimeException("Fail to search groups in ldap", e);
    }
  }

  @Override
  public boolean validateCredentials(User user, String credential) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Application getApplication(String applicationName) {
    SearchResultEntry entry =
        getEntryByDn("ou=" + applicationName + "," + config.get("app_source"));
    return (entry != null) ? ApplicationLdapMapper.mapFromSearchEntry(entry) : null;
  }

  @Override
  public PageResult<Application> searchApplications(
      Map<String, String> searchProperties, PageableResult pageable, String searchOperator) {
    Filter filter = LdapUtils.getFilterFromCriteria(searchProperties);
    try {
      SearchRequest searchRequest =
          new SearchRequest(config.get("app_source"), SearchScope.ONE, filter, "*", "+");
      LdapUtils.setRequestControls(searchRequest, pageable);
      SearchResult searchResult = ldapPoolConnection.search(searchRequest);
      List<Application> applications =
          searchResult.getSearchEntries().stream()
              .map(e -> ApplicationLdapMapper.mapFromSearchEntry(e))
              .collect(Collectors.toList());
      PageResult<Application> page = new PageResult<>();
      page.setResults(applications);
      return page;
    } catch (LDAPSearchException e) {
      throw new RuntimeException("Fail to search applications in ldap", e);
    }
  }
}
