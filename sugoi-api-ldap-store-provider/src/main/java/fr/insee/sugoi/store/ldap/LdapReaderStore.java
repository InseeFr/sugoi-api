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

public class LdapReaderStore extends LdapStore implements ReaderStore {

  public LdapReaderStore(Map<String, String> config) {
    logger.debug("Configuring LdapReaderStore with config : {}", config);
    try {
      this.ldapPoolConnection = LdapFactory.getConnectionPool(config);
      this.config = config;
      userLdapMapper = new UserLdapMapper(config);
      organizationLdapMapper = new OrganizationLdapMapper(config);
      groupLdapMapper = new GroupLdapMapper(config);
      applicationLdapMapper = new ApplicationLdapMapper(config);
    } catch (LDAPException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public User getUser(String id) {
    logger.debug("Searching user {}", id);
    SearchResultEntry entry = getEntryByDn(getUserDN(id));
    User user = (entry != null) ? userLdapMapper.mapFromAttributes(entry.getAttributes()) : null;
    return user;
  }

  @Override
  public Organization getOrganization(String id) {
    SearchResultEntry entry = getEntryByDn(getOrganizationDN(id));
    Organization org =
        (entry != null) ? organizationLdapMapper.mapFromAttributes(entry.getAttributes()) : null;
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

  @Override
  public PageResult<User> getUsersInGroup(String appName, String groupName) {
    PageResult<User> page = new PageResult<>();
    SearchResultEntry entry = getEntryByDn(getGroupDN(appName, groupName));
    if (entry.hasAttribute("uniqueMember")) {
      page.setResults(
          Arrays.stream(entry.getAttribute("uniqueMember").getValues())
              .map(uniqueMember -> getUser(LdapUtils.getNodeValueFromDN(uniqueMember)))
              .collect(Collectors.toList()));
    } else {
      page.setResults(new ArrayList<>());
    }
    return page;
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
    try {
      SearchResultEntry entry = getEntryByDn(getGroupDN(appName, groupName));
      return ((entry != null)
              && (Filter.create(getGroupWildcardFilter(appName)).matchesEntry(entry)))
          ? groupLdapMapper.mapFromAttributes(entry.getAttributes())
          : null;
    } catch (LDAPException e) {
      throw new RuntimeException("Fail to get group in ldap", e);
    }
  }

  @Override
  public PageResult<Group> searchGroups(
      String appName, Group groupFilter, PageableResult pageable, String searchOperator) {
    try {
      return searchOnLdap(
          getApplicationDN(appName),
          SearchScope.SUBORDINATE_SUBTREE,
          Filter.createANDFilter(
              Filter.create(getGroupWildcardFilter(appName)),
              getFilterFromObject(groupFilter, groupLdapMapper)),
          pageable,
          groupLdapMapper);
    } catch (LDAPException e) {
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
    SearchResultEntry entry = getEntryByDn(getApplicationDN(applicationName));
    Application application =
        (entry != null) ? applicationLdapMapper.mapFromAttributes(entry.getAttributes()) : null;
    return application;
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

  private <MapperType> Filter getFilterFromObject(
      MapperType object, LdapMapper<MapperType> mapper) {
    return LdapFilter.and(
        mapper.mapToAttributes(object).stream()
            .map(attribute -> LdapFilter.contains(attribute.getName(), attribute.getValue()))
            .collect(Collectors.toList()));
  }

  private SearchResultEntry getEntryByDn(String dn) {
    try {
      logger.debug("Fetching {}", dn);
      SearchResultEntry entry = ldapPoolConnection.getEntry(dn, "+", "*");

      return entry;
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to execute " + dn, e);
    }
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
            .map(e -> mapper.mapFromAttributes(e.getAttributes()))
            .collect(Collectors.toList());
    pageResult.setResults(results);
    return pageResult;
  }
}
