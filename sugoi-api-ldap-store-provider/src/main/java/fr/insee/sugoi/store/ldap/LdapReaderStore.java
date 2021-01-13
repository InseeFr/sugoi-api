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
import fr.insee.sugoi.ldap.utils.LdapFilter;
import fr.insee.sugoi.ldap.utils.LdapUtils;
import fr.insee.sugoi.ldap.utils.mapper.AddressLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.ApplicationLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.GroupLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.LdapMapper;
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

  private UserLdapMapper userLdapMapper = new UserLdapMapper();
  private OrganizationLdapMapper organizationLdapMapper = new OrganizationLdapMapper();
  private GroupLdapMapper groupLdapMapper = new GroupLdapMapper();
  private ApplicationLdapMapper applicationLdapMapper = new ApplicationLdapMapper();

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
    return (entry != null) ? userLdapMapper.mapFromSearchEntry(entry) : null;
  }

  @Override
  public Organization getOrganization(String id) {
    SearchResultEntry entry = getEntryByDn("uid=" + id + "," + config.get("organization_source"));
    Organization org = (entry != null) ? organizationLdapMapper.mapFromSearchEntry(entry) : null;
    if (org != null
        && org.getAttributes().containsKey("adressDn")
        && org.getAttributes().get("adressDn") != null) {
      SearchResultEntry addressResult =
          getEntryByDn(org.getAttributes().get("adressDn").toString());
      org.setAddress(AddressLdapMapper.mapFromSearchEntry(addressResult));
    }
    return org;
  }

  private <MapperType> Filter getFilterFromObject(
      MapperType object, LdapMapper<MapperType> mapper) {
    return LdapFilter.and(
        mapper.mapToAttribute(object).stream()
            .map(attribute -> LdapFilter.contains(attribute.getName(), attribute.getValue()))
            .collect(Collectors.toList()));
  }

  @Override
  public PageResult<User> searchUsers(
      User userFilter, PageableResult pageable, String typeRecherche) {
    try {
      return searchOnLdap(
          config.get("user_source"),
          SearchScope.SUBORDINATE_SUBTREE,
          getFilterFromObject(userFilter, userLdapMapper),
          pageable,
          userLdapMapper);
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

  private <ResultType> PageResult<ResultType> searchOnLdap(
      String baseDn,
      SearchScope scope,
      Filter filter,
      PageableResult pageableResult,
      LdapMapper<ResultType> mapper)
      throws LDAPSearchException {
    SearchRequest searchRequest = new SearchRequest(baseDn, scope, filter, "*", "+");
    LdapUtils.setRequestControls(searchRequest, pageableResult);
    SearchResult searchResult = ldapPoolConnection.search(searchRequest);
    PageResult<ResultType> pageResult = new PageResult<>();
    List<ResultType> results =
        searchResult.getSearchEntries().stream()
            .map(e -> mapper.mapFromSearchEntry(e))
            .collect(Collectors.toList());
    pageResult.setResults(results);
    return pageResult;
  }

  @Override
  public PageResult<Organization> searchOrganizations(
      Organization organizationFilter, PageableResult pageable, String searchOperator) {
    try {
      return searchOnLdap(
          config.get("organization_source"),
          SearchScope.SUBORDINATE_SUBTREE,
          getFilterFromObject(organizationFilter, organizationLdapMapper),
          pageable,
          organizationLdapMapper);
    } catch (LDAPSearchException e) {
      throw new RuntimeException("Fail to search organizations in ldap", e);
    }
  }

  @Override
  public Group getGroup(String appName, String groupName) {
    SearchResultEntry entry = getGroupResultEntry(appName, groupName);
    return (entry != null) ? groupLdapMapper.mapFromSearchEntry(entry) : null;
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
      String appName, Group groupFilter, PageableResult pageable, String searchOperator) {
    Filter isGroup = Filter.createSubAnyFilter("objectClass", "groupOfUniqueNames");
    try {
      return searchOnLdap(
          "ou=" + appName + "," + config.get("app_source"),
          SearchScope.SUBORDINATE_SUBTREE,
          Filter.createANDFilter(isGroup, getFilterFromObject(groupFilter, groupLdapMapper)),
          pageable,
          groupLdapMapper);
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
    return (entry != null) ? applicationLdapMapper.mapFromSearchEntry(entry) : null;
  }

  @Override
  public PageResult<Application> searchApplications(
      Application applicationFilter, PageableResult pageable, String searchOperator) {
    try {
      return searchOnLdap(
          config.get("app_source"),
          SearchScope.ONE,
          getFilterFromObject(applicationFilter, applicationLdapMapper),
          pageable,
          applicationLdapMapper);
    } catch (LDAPSearchException e) {
      throw new RuntimeException("Fail to search applications in ldap", e);
    }
  }
}
