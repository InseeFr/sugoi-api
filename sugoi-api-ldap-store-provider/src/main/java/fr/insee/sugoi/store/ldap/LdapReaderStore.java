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

import com.unboundid.ldap.sdk.*;
import fr.insee.sugoi.core.exceptions.MultipleUserWithSameMailException;
import fr.insee.sugoi.core.store.ReaderStore;
import fr.insee.sugoi.ldap.utils.LdapFactory;
import fr.insee.sugoi.ldap.utils.LdapFilter;
import fr.insee.sugoi.ldap.utils.LdapUtils;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.ldap.utils.mapper.*;
import fr.insee.sugoi.model.*;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import fr.insee.sugoi.model.paging.SearchType;
import fr.insee.sugoi.model.technics.StoreMapping;
import java.util.*;
import java.util.stream.Collectors;

public class LdapReaderStore extends LdapStore implements ReaderStore {

  public LdapReaderStore(
      Map<String, String> config, Map<MappingType, List<StoreMapping>> mappings) {
    logger.debug("Configuring LdapReaderStore with config : {}", config);
    try {
      if (Boolean.valueOf(config.get(LdapConfigKeys.READ_CONNECTION_AUTHENTICATED))) {
        this.ldapPoolConnection = LdapFactory.getConnectionPoolAuthenticated(config);
        this.ldapMonoConnection = LdapFactory.getSingleConnectionAuthenticated(config);
      } else {
        this.ldapPoolConnection = LdapFactory.getConnectionPool(config);
        this.ldapMonoConnection = LdapFactory.getSingleConnection(config);
      }
      this.config = config;
      userLdapMapper = new UserLdapMapper(config, mappings.get(MappingType.USERMAPPING));
      organizationLdapMapper =
          new OrganizationLdapMapper(config, mappings.get(MappingType.ORGANIZATIONMAPPING));
      groupLdapMapper = new GroupLdapMapper(config, mappings.get(MappingType.GROUPMAPPING));
      applicationLdapMapper =
          new ApplicationLdapMapper(config, mappings.get(MappingType.APPLICATIONMAPPING));
      addressLdapMapper = new AddressLdapMapper(config);
    } catch (LDAPException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Retrieve the user ldap resource then complete it by retrieving the address ldap resource and
   * the organization ldap resource
   */
  @Override
  public Optional<User> getUser(String id) {
    logger.debug("Searching user {}", id);
    SearchResultEntry entry = getEntryByDn(getUserDN(id));
    User user = (entry != null) ? userLdapMapper.mapFromAttributes(entry.getAttributes()) : null;
    if (user != null && user.getAddress() != null && user.getAddress().getId() != null) {
      PostalAddress address = getAddress(user.getAddress().getId());
      if (address != null) {
        address.setId(user.getAddress().getId());
        user.setAddress(address);
      }
    }
    if (user != null && user.getOrganization() != null) {
      user.setOrganization(getOrganization(user.getOrganization().getIdentifiant()).orElse(null));
    }
    return Optional.ofNullable(user);
  }

  /**
   * Retrieve the organization ldap resource then complete it by retrieving the address ldap
   * resource and the sub organization ldap resource
   */
  @Override
  public Optional<Organization> getOrganization(String id) {
    if (config.get(LdapConfigKeys.ORGANIZATION_SOURCE) != null) {
      return getOrganization(id, false);
    } else {
      throw new UnsupportedOperationException(
          "Organizations feature not configured for this storage");
    }
  }

  /** Search users matching userFilter set properties under the user_source */
  @Override
  public PageResult<User> searchUsers(
      User userFilter, PageableResult pageable, String typeRecherche) {
    try {
      return searchOnLdap(
          config.get(LdapConfigKeys.USER_SOURCE),
          SearchScope.SUBORDINATE_SUBTREE,
          getFilterFromObject(userFilter, userLdapMapper, typeRecherche),
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
    if (entry != null && entry.hasAttribute("uniqueMember")) {
      page.setResults(
          Arrays.stream(entry.getAttribute("uniqueMember").getValues())
              .map(uniqueMember -> getUser(LdapUtils.getNodeValueFromDN(uniqueMember)))
              .filter(optionalUser -> optionalUser.isPresent())
              .map(optionalUser -> optionalUser.get())
              .collect(Collectors.toList()));
    } else {
      page.setResults(new ArrayList<>());
    }
    return page;
  }

  @Override
  public PageResult<Organization> searchOrganizations(
      Organization organizationFilter, PageableResult pageable, String searchOperator) {
    if (config.get(LdapConfigKeys.ORGANIZATION_SOURCE) != null) {
      try {
        return searchOnLdap(
            config.get(LdapConfigKeys.ORGANIZATION_SOURCE),
            SearchScope.SUBORDINATE_SUBTREE,
            getFilterFromObject(organizationFilter, organizationLdapMapper, searchOperator),
            pageable,
            organizationLdapMapper);
      } catch (LDAPSearchException e) {
        throw new RuntimeException("Fail to search organizations in ldap", e);
      }
    } else {
      throw new UnsupportedOperationException(
          "Organizations feature not configured for this storage");
    }
  }

  /**
   * Retrieve the specified group with the members' username. A check is made ex post to verify that
   * the retrieve object is a group as defined in the group_filter_pattern
   */
  @Override
  public Optional<Group> getGroup(String appName, String groupName) {
    try {
      SearchResultEntry entry = getEntryByDn(getGroupDN(appName, groupName));
      return ((entry != null)
              && (Filter.create(getGroupWildcardFilter(appName)).matchesEntry(entry)))
          ? Optional.of(groupLdapMapper.mapFromAttributes(entry.getAttributes()))
          : Optional.empty();
    } catch (LDAPException e) {
      throw new RuntimeException("Fail to get group in ldap", e);
    }
  }

  /** Search groups with the group_filter under the group source */
  @Override
  public PageResult<Group> searchGroups(
      String appName, Group groupFilter, PageableResult pageable, String searchOperator) {
    try {
      return searchOnLdap(
          getGroupSource(appName),
          SearchScope.SUBORDINATE_SUBTREE,
          Filter.createANDFilter(
              Filter.create(getGroupWildcardFilter(appName)),
              getFilterFromObject(groupFilter, groupLdapMapper, searchOperator)),
          pageable,
          groupLdapMapper);
    } catch (LDAPException e) {
      throw new RuntimeException("Fail to search groups in ldap", e);
    }
  }

  @Override
  public boolean validateCredentials(User user, String credential) {
    try {
      return LdapFactory.validateUserPassword(config, getUserDN(user.getUsername()), credential);
    } catch (LDAPException e) {
      return false;
    }
  }

  @Override
  public Optional<Application> getApplication(String applicationName) {
    SearchResultEntry entry = getEntryByDn(getApplicationDN(applicationName));
    Application application =
        (entry != null) ? applicationLdapMapper.mapFromAttributes(entry.getAttributes()) : null;
    if (application != null) {
      List<Group> groups = new ArrayList<>();
      try {
        groups =
            searchGroups(applicationName, new Group(), new PageableResult(200, 0, null), "AND")
                .getResults();
      } catch (Exception e) {
        logger.error(e.getMessage());
      }
      application.setGroups(groups);
    }
    return Optional.ofNullable(application);
  }

  /** Search applications matching applicationFilter set properties just under application source */
  @Override
  public PageResult<Application> searchApplications(
      Application applicationFilter, PageableResult pageable, String searchOperator) {
    try {
      if (config.get(LdapConfigKeys.APP_SOURCE) != null) {
        return searchOnLdap(
            config.get(LdapConfigKeys.APP_SOURCE),
            SearchScope.ONE,
            getFilterFromObject(applicationFilter, applicationLdapMapper, searchOperator),
            pageable,
            applicationLdapMapper);
      } else {
        throw new UnsupportedOperationException(
            "Applications feature not configured for this realm");
      }
    } catch (LDAPSearchException e) {
      throw new RuntimeException("Fail to search applications in ldap", e);
    }
  }

  /**
   * Create a filter from an object using a mapper class. Each set field of the object is
   * transformed to a filter.
   *
   * @param <MapperType> the type of object we need to create a filter from
   * @param object the object to create a filter from, only set properties are taken into account
   * @param mapper a mapper used to transform object to filter
   * @return a filter corresponding to the properties of object
   */
  private <MapperType> Filter getFilterFromObject(
      MapperType object, LdapMapper<MapperType> mapper, String searchType) {
    if (searchType.equalsIgnoreCase("AND")) {
      return LdapFilter.and(
          mapper.createAttributesForFilter(object).stream()
              .filter(attribute -> !attribute.getValue().equals(""))
              .map(attribute -> LdapFilter.contains(attribute.getName(), attribute.getValue()))
              .collect(Collectors.toList()));
    } else if (searchType.equalsIgnoreCase("OR")) {
      List<Filter> objectClassListFilter =
          mapper.createAttributesForFilter(object).stream()
              .filter(attribute -> attribute.getName().equals("objectClass"))
              .map(attribute -> LdapFilter.contains(attribute.getName(), attribute.getValue()))
              .collect(Collectors.toList());

      List<Filter> attributeListFilter =
          mapper.createAttributesForFilter(object).stream()
              .filter(
                  attribute ->
                      !attribute.getName().equals("objectClass")
                          && !attribute.getValue().equals(""))
              .map(attribute -> LdapFilter.contains(attribute.getName(), attribute.getValue()))
              .collect(Collectors.toList());

      if (objectClassListFilter.size() > 0 & attributeListFilter.size() == 0) {
        return LdapFilter.and(objectClassListFilter);
      } else if (objectClassListFilter.size() == 0 & attributeListFilter.size() > 0) {
        return LdapFilter.or(attributeListFilter);
      } else {
        return LdapFilter.and(
            Arrays.asList(
                LdapFilter.and(objectClassListFilter), LdapFilter.or(attributeListFilter)));
      }
    }
    throw new RuntimeException("Invalid searchType must be AND or OR");
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

  /**
   * @param <ResultType> the type of the resource searched
   * @param baseDn DN where to make the search
   * @param scope search scope value
   * @param filter filter to apply on the search
   * @param pageableResult
   * @param mapper mapper to convert the attributes found to a ResultType
   * @return
   * @throws LDAPSearchException
   */
  private <ResultType> PageResult<ResultType> searchOnLdap(
      String baseDn,
      SearchScope scope,
      Filter filter,
      PageableResult pageableResult,
      LdapMapper<ResultType> mapper)
      throws LDAPSearchException {
    SearchRequest searchRequest = new SearchRequest(baseDn, scope, filter, "*", "+");
    LdapUtils.setRequestControls(searchRequest, pageableResult, config);
    SearchResult searchResult = null;
    try {
      searchResult = ldapMonoConnection.search(searchRequest);
    } catch (LDAPException e) {
      if (e.getResultCode().intValue() == ResultCode.SERVER_DOWN_INT_VALUE) {
        try {
          if (Boolean.valueOf(config.get(LdapConfigKeys.READ_CONNECTION_AUTHENTICATED))) {
            ldapMonoConnection = LdapFactory.getSingleConnectionAuthenticated(config, true);
          } else {
            ldapMonoConnection = LdapFactory.getSingleConnection(config, true);
          }
        } catch (LDAPException e1) {
          throw new RuntimeException(e1);
        }
        searchResult = ldapMonoConnection.search(searchRequest);
      } else {
        throw new RuntimeException(e);
      }
    }
    PageResult<ResultType> pageResult = new PageResult<>();
    pageResult.setResults(
        searchResult.getSearchEntries().stream()
            .map(e -> mapper.mapFromAttributes(e.getAttributes()))
            .collect(Collectors.toList()));
    LdapUtils.setResponseControls(pageResult, searchResult);
    pageResult.setNextStart(pageableResult.getFirst() + pageResult.getPageSize());
    return pageResult;
  }

  private PostalAddress getAddress(String addressId) {
    SearchResultEntry addressResult = getEntryByDn(getAddressDN(addressId));
    return addressResult != null
        ? addressLdapMapper.getAddressFromSearchEntry(addressResult)
        : null;
  }

  @Override
  public Optional<User> getUserByMail(String mail) {
    logger.debug("Searching user with mail {}", mail);
    User searchedUser = new User();
    searchedUser.setMail(mail);
    PageResult<User> users =
        searchUsers(searchedUser, new PageableResult(2, 0, null), SearchType.OR.name());
    User user = null;
    if (users.getResults().size() == 1) {
      user = users.getResults().get(0);
      if (user.getAddress() != null && user.getAddress().getId() != null) {
        PostalAddress address = getAddress(user.getAddress().getId());
        if (address != null) {
          address.setId(user.getAddress().getId());
          user.setAddress(address);
        }
      }
      if (user.getOrganization() != null) {
        user.setOrganization(getOrganization(user.getOrganization().getIdentifiant()).orElse(null));
      }
    } else if (users.getResults().size() > 1) {
      throw new MultipleUserWithSameMailException(mail);
    }
    return Optional.ofNullable(user);
  }

  @Override
  public Optional<Group> getManagerGroup(String applicationName) {
    try {
      SearchResultEntry entry = getEntryByDn(getGroupManagerSource(applicationName));
      return (entry != null)
          ? Optional.of(groupLdapMapper.mapFromAttributes(entry.getAttributes()))
          : Optional.empty();
    } catch (Exception e) {
      throw new RuntimeException("Fail to get group in ldap", e);
    }
  }

  private Optional<Organization> getOrganization(String id, boolean isSubOrganization) {
    SearchResultEntry entry = getEntryByDn(getOrganizationDN(id));
    Organization org =
        (entry != null) ? organizationLdapMapper.mapFromAttributes(entry.getAttributes()) : null;
    if (org != null && org.getAddress() != null && org.getAddress().getId() != null) {
      PostalAddress address = getAddress(org.getAddress().getId());
      if (address != null) {
        address.setId(org.getAddress().getId());
        org.setAddress(address);
      }
    }
    if (org != null && !isSubOrganization && org.getOrganization() != null) {
      org.setOrganization(
          getOrganization(org.getOrganization().getIdentifiant(), true).orElse(null));
    }
    return Optional.ofNullable(org);
  }
}
