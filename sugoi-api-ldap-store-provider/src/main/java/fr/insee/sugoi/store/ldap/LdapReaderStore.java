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
import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.core.store.ReaderStore;
import fr.insee.sugoi.ldap.utils.LdapFactory;
import fr.insee.sugoi.ldap.utils.LdapFilter;
import fr.insee.sugoi.ldap.utils.LdapUtils;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.ldap.utils.mapper.AddressLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.ApplicationLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.GroupLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.LdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.OrganizationLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.UserLdapMapper;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.MappingType;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.PostalAddress;
import fr.insee.sugoi.model.RealmConfigKeys;
import fr.insee.sugoi.model.SugoiObject;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.exceptions.MultipleUserWithSameMailException;
import fr.insee.sugoi.model.exceptions.StoreException;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import fr.insee.sugoi.model.paging.SearchType;
import fr.insee.sugoi.model.technics.StoreMapping;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

public class LdapReaderStore extends LdapStore implements ReaderStore {

  public LdapReaderStore(
      Map<RealmConfigKeys, String> config, Map<MappingType, List<StoreMapping>> mappings) {
    logger.debug("Configuring LdapReaderStore with config : {}", config);
    try {
      if (Boolean.TRUE.equals(
          Boolean.valueOf(config.get(LdapConfigKeys.READ_CONNECTION_AUTHENTICATED)))) {
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
      throw new StoreException("Failed to create LDAPReaderStore", e);
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
    // Add empty habilitations and groups if not already added
    if (user != null && user.getHabilitations() == null) {
      user.setHabilitations(new ArrayList<>());
    }
    if (user != null && user.getGroups() == null) {
      user.setGroups(new ArrayList<>());
    }
    return Optional.ofNullable(user);
  }

  /**
   * Retrieve the organization ldap resource then complete it by retrieving the address ldap
   * resource and the sub organization ldap resource
   */
  @Override
  public Optional<Organization> getOrganization(String id) {
    if (StringUtils.isNotBlank(config.get(GlobalKeysConfig.ORGANIZATION_SOURCE))) {
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
          config.get(GlobalKeysConfig.USER_SOURCE),
          SearchScope.SUBORDINATE_SUBTREE,
          getFilterFromObject(userFilter, userLdapMapper, typeRecherche),
          pageable,
          userLdapMapper);
    } catch (LDAPSearchException e) {
      throw new StoreException("Fail to execute user search", e);
    }
  }

  @Override
  public PageResult<User> fuzzySearchUsers(
      User userFilter, PageableResult pageable, String typeRecherche) {
    String initialCommonName = (String) userFilter.getAttributes().get("common_name");
    if (initialCommonName == null) {
      return searchUsers(userFilter, pageable, typeRecherche);
    } else {
      try {
        userFilter
            .getAttributes()
            .put(
                "common_name",
                initialCommonName
                    .replaceAll(
                        "[ÀÁÂÃÄAÅÇCÈÉÊËEÌÍIÎÏÐÒÓÔOÕÖÙUÚÛÜÝYŸàáâãäåçèéêëìíîïðòóôõöùúûüýÿaeiouc \\-']",
                        "*")
                    .replaceAll("\\*+", "*"));
        int originalPageSize = pageable.getSize();
        pageable.setSize(50000);
        PageResult<User> results =
            searchOnLdap(
                config.get(GlobalKeysConfig.USER_SOURCE),
                SearchScope.SUBORDINATE_SUBTREE,
                getFilterFromObject(userFilter, userLdapMapper, typeRecherche, false),
                pageable,
                userLdapMapper);
        String normalizedCommonName = removeSpecialChars(initialCommonName);
        List<User> filteredUsers =
            results.getResults().stream()
                .filter(
                    u ->
                        removeSpecialChars((String) u.getAttributes().get("common_name"))
                            .toUpperCase()
                            .contains(normalizedCommonName.toUpperCase()))
                .limit(originalPageSize)
                .collect(Collectors.toList());
        results.setResults(filteredUsers);
        return results;
      } catch (LDAPException e) {
        throw new StoreException("Fail to execute user search", e);
      }
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
              .filter(Optional::isPresent)
              .map(Optional::get)
              .collect(Collectors.toList()));
    } else {
      page.setResults(new ArrayList<>());
    }
    return page;
  }

  @Override
  public PageResult<Organization> searchOrganizations(
      Organization organizationFilter, PageableResult pageable, String searchOperator) {
    if (StringUtils.isNotBlank(config.get(GlobalKeysConfig.ORGANIZATION_SOURCE))) {
      try {
        return searchOnLdap(
            config.get(GlobalKeysConfig.ORGANIZATION_SOURCE),
            SearchScope.SUBORDINATE_SUBTREE,
            getFilterFromObject(organizationFilter, organizationLdapMapper, searchOperator),
            pageable,
            organizationLdapMapper);
      } catch (LDAPSearchException e) {
        throw new StoreException("Fail to search organizations in ldap", e);
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
      throw new StoreException("Fail to get group in ldap", e);
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
      throw new StoreException("Fail to search groups in ldap", e);
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
        groups = searchGroups(applicationName, new Group(), null, "AND").getResults();
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
      if (StringUtils.isNotBlank(config.get(GlobalKeysConfig.APP_SOURCE))) {
        return searchOnLdap(
            config.get(GlobalKeysConfig.APP_SOURCE),
            SearchScope.ONE,
            getFilterFromObject(applicationFilter, applicationLdapMapper, searchOperator),
            pageable,
            applicationLdapMapper);
      } else {
        throw new UnsupportedOperationException(
            "Applications feature not configured for this realm");
      }
    } catch (LDAPSearchException e) {
      throw new StoreException("Fail to search applications in ldap", e);
    }
  }

  private <M extends SugoiObject> Filter getFilterFromObject(
      M object, LdapMapper<M> mapper, String searchType) {
    return getFilterFromObject(object, mapper, searchType, true);
  }

  /**
   * Create a filter from an object using a mapper class. Each set field of the object is
   * transformed to a filter.
   *
   * @param <M> the type of object we need to create a filter from
   * @param object the object to create a filter from, only set properties are taken into account
   * @param mapper a mapper used to transform object to filter
   * @return a filter corresponding to the properties of object
   */
  private <M extends SugoiObject> Filter getFilterFromObject(
      M object, LdapMapper<M> mapper, String searchType, boolean encodeCommonNameWildcard) {
    Assert.isTrue(
        searchType.equalsIgnoreCase("AND") || searchType.equalsIgnoreCase("OR"),
        "Search type should be AND or OR.");
    List<Attribute> attributes = mapper.createAttributesForFilter(object);
    List<Filter> attributeListFilter = getAttributesFilters(attributes, encodeCommonNameWildcard);
    List<Filter> objectClassListFilter = getObjectClassFilters(attributes);
    if (!objectClassListFilter.isEmpty() && attributeListFilter.isEmpty()) {
      return LdapFilter.and(objectClassListFilter);
    } else if (objectClassListFilter.isEmpty() && !attributeListFilter.isEmpty()) {
      return searchType.equalsIgnoreCase("OR")
          ? LdapFilter.or(attributeListFilter)
          : LdapFilter.and(attributeListFilter);
    } else {
      return LdapFilter.and(
          Arrays.asList(
              LdapFilter.and(objectClassListFilter),
              searchType.equalsIgnoreCase("OR")
                  ? LdapFilter.or(attributeListFilter)
                  : LdapFilter.and(attributeListFilter)));
    }
  }

  private List<Filter> getObjectClassFilters(List<Attribute> attributes) {
    return attributes.stream()
        .filter(attribute -> attribute.getName().equals("objectClass"))
        .map(attribute -> LdapFilter.createFilter(attribute.getName(), attribute.getValues()))
        .collect(Collectors.toList());
  }

  private List<Filter> getAttributesFilters(
      List<Attribute> attributes, boolean encodeCommonNameWildcard) {
    List<Filter> filters =
        attributes.stream()
            .filter(
                attribute ->
                    !attribute.getName().equals("objectClass")
                        && !attribute.getValue().isEmpty()
                        && !attribute.getName().equals("cn"))
            .map(attribute -> LdapFilter.createFilter(attribute.getName(), attribute.getValues()))
            .collect(Collectors.toList());
    Optional<Attribute> commonNameAttribute =
        attributes.stream().filter(attribute -> attribute.getName().equals("cn")).findFirst();
    if (commonNameAttribute.isPresent()) {
      if (encodeCommonNameWildcard) {
        filters.add(
            LdapFilter.createFilter(
                commonNameAttribute.get().getName(), commonNameAttribute.get().getValues()));
      } else {
        try {
          filters.add(
              Filter.create(
                  "cn="
                      + Filter.encodeValue(commonNameAttribute.get().getValue())
                          .replace("\\2a", "*")));
        } catch (LDAPException e) {
          filters.add(
              LdapFilter.createFilter(
                  commonNameAttribute.get().getName(), commonNameAttribute.get().getValues()));
        }
      }
    }
    return filters;
  }

  private SearchResultEntry getEntryByDn(String dn) {
    try {
      logger.debug("Fetching {}", dn);

      return ldapPoolConnection.getEntry(dn, "+", "*");
    } catch (LDAPException e) {
      throw new StoreException("Failed to execute " + dn, e);
    }
  }

  /**
   * @param <R> the type of the resource searched
   * @param baseDn DN where to make the search
   * @param scope search scope value
   * @param filter filter to apply on the search
   * @param pageableResult
   * @param mapper mapper to convert the attributes found to a ResultType
   * @return
   * @throws LDAPSearchException
   */
  private <R extends SugoiObject> PageResult<R> searchOnLdap(
      String baseDn,
      SearchScope scope,
      Filter filter,
      PageableResult pageableResult,
      LdapMapper<R> mapper)
      throws LDAPSearchException {
    SearchRequest searchRequest = new SearchRequest(baseDn, scope, filter, "*", "+");
    if (pageableResult != null) {
      LdapUtils.setRequestControls(searchRequest, pageableResult, config);
    }
    SearchResult searchResult = null;
    try {
      searchResult = ldapMonoConnection.search(searchRequest);
    } catch (LDAPException e) {
      try {
        logger.info("Retry connection for error code " + e.getResultCode().intValue());
        if (Boolean.TRUE.equals(
            Boolean.valueOf(config.get(LdapConfigKeys.READ_CONNECTION_AUTHENTICATED)))) {
          ldapMonoConnection = LdapFactory.getSingleConnectionAuthenticated(config, true);
        } else {
          ldapMonoConnection = LdapFactory.getSingleConnection(config, true);
        }
      } catch (LDAPException e1) {
        throw new StoreException("Failed to reopen connection", e1);
      }
      searchResult = ldapMonoConnection.search(searchRequest);
    }
    PageResult<R> pageResult = new PageResult<>();
    pageResult.setResults(
        searchResult.getSearchEntries().stream()
            .map(e -> mapper.mapFromAttributes(e.getAttributes()))
            .collect(Collectors.toList()));
    LdapUtils.setResponseControls(pageResult, searchResult);
    int first = pageableResult != null ? pageableResult.getFirst() : 0;
    pageResult.setNextStart(first + pageResult.getPageSize());
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
    List<User> users =
        searchUsers(searchedUser, new PageableResult(2, 0, null), SearchType.OR.name())
            .getResults()
            .stream()
            .filter(u -> u.getMail().equalsIgnoreCase(mail))
            .collect(Collectors.toList());
    User user = null;
    if (users.size() == 1) {
      user = users.get(0);
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
    } else if (users.size() > 1) {
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
      throw new StoreException("Fail to get group in ldap", e);
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

  public String removeSpecialChars(String string) {
    return Normalizer.normalize(string, Normalizer.Form.NFD)
        .replaceAll("[-'\\s]+", "")
        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
  }
}
