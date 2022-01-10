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
package fr.insee.sugoi.store.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.core.exceptions.ApplicationNotFoundException;
import fr.insee.sugoi.core.exceptions.MultipleUserWithSameMailException;
import fr.insee.sugoi.core.exceptions.OrganizationNotFoundException;
import fr.insee.sugoi.core.store.ReaderStore;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import fr.insee.sugoi.store.file.configuration.FileKeysConfig;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class FileReaderStore implements ReaderStore {

  private static final Logger logger = LoggerFactory.getLogger(FileReaderStore.class);

  @Autowired ResourceLoader resourceLoader;
  private Map<String, String> config;
  private ObjectMapper mapper = new ObjectMapper();

  public FileReaderStore(Map<String, String> generateConfig) {
    this.config = generateConfig;
  }

  @Override
  public Optional<User> getUser(String id) {
    Resource realmsResource =
        resourceLoader.getResource(config.get(FileKeysConfig.USER_SOURCE) + id);
    if (realmsResource.exists()) {
      User user = loadResourceContent(realmsResource, User.class);
      // suborganization is loaded as an independant resource
      if (user.getOrganization() != null && user.getOrganization().getIdentifiant() != null) {
        String nestedOrgaId = user.getOrganization().getIdentifiant();
        user.setOrganization(getOrganization(nestedOrgaId).orElse(null));
      }
      return Optional.of(user);
    } else {
      return Optional.empty();
    }
  }

  @Override
  public PageResult<User> searchUsers(
      User searchUser, PageableResult pageable, String searchOperator) {
    PageResult<User> pageResult = new PageResult<>();
    pageResult.setResults(
        Arrays.stream(getFilenamesFromSource(config.get(FileKeysConfig.USER_SOURCE)))
            .map(resource -> loadResourceContent(resource, User.class))
            .filter(user -> checkIfMatches(user, searchUser))
            .collect(Collectors.toList()));
    return pageResult;
  }

  @Override
  public Optional<Organization> getOrganization(String id) {
    if (config.get(FileKeysConfig.ORGANIZATION_SOURCE) != null) {
      Resource realmsResource =
          resourceLoader.getResource(config.get(FileKeysConfig.ORGANIZATION_SOURCE) + id);
      if (realmsResource.exists()) {
        Organization organization = loadResourceContent(realmsResource, Organization.class);
        // suborganization is loaded as an independant resource
        if (organization.getOrganization() != null
            && organization.getOrganization().getIdentifiant() != null) {
          try {
            String subOrganizationId = organization.getOrganization().getIdentifiant();
            organization.setOrganization(
                getOrganization(subOrganizationId)
                    .orElseThrow(() -> new OrganizationNotFoundException(subOrganizationId)));
          } catch (RuntimeException e) {
            logger.error("Unable to retrieve organization's suborganization", e);
          }
        }
        return Optional.of(organization);
      } else {
        return Optional.empty();
      }
    } else {
      throw new UnsupportedOperationException(
          "Organizations feature not configured for this storage");
    }
  }

  @Override
  public PageResult<User> getUsersInGroup(String appName, String groupName) {
    PageResult<User> pageResult = new PageResult<>();
    Optional<Group> optionalGroup = getGroup(appName, groupName);
    if (optionalGroup.isPresent() && optionalGroup.get().getUsers() != null) {
      pageResult.setResults(
          optionalGroup.get().getUsers().stream()
              .map(simplifiedUser -> getUser(simplifiedUser.getUsername()))
              .filter(optionalUser -> optionalUser.isPresent())
              .map(optionalUser -> optionalUser.get())
              .collect(Collectors.toList()));
    } else {
      pageResult.setResults(List.of());
    }
    return pageResult;
  }

  @Override
  public PageResult<Organization> searchOrganizations(
      Organization organizationFilter, PageableResult pageable, String searchOperator) {
    if (config.get(FileKeysConfig.ORGANIZATION_SOURCE) != null) {
      PageResult<Organization> pageResult = new PageResult<>();
      pageResult.setResults(
          Arrays.stream(getFilenamesFromSource(config.get(FileKeysConfig.ORGANIZATION_SOURCE)))
              .map(resource -> loadResourceContent(resource, Organization.class))
              .filter(org -> checkIfMatches(org, organizationFilter))
              .collect(Collectors.toList()));
      return pageResult;
    } else {
      throw new UnsupportedOperationException(
          "Organizations feature not configured for this storage");
    }
  }

  @Override
  public Optional<Group> getGroup(String appName, String groupName) {
    Application application =
        getApplication(appName).orElseThrow(() -> new ApplicationNotFoundException(appName));
    return application.getGroups() != null
        ? application.getGroups().stream()
            .filter(group -> group.getName().equalsIgnoreCase(groupName))
            .findFirst()
        : Optional.empty();
  }

  @Override
  public PageResult<Group> searchGroups(
      String appName, Group groupFilter, PageableResult pageable, String searchOperator) {
    if (config.get(FileKeysConfig.APP_SOURCE) != null) {
      PageResult<Group> pageResult = new PageResult<>();
      pageResult.setResults(
          Arrays.stream(getFilenamesFromSource(config.get(FileKeysConfig.APP_SOURCE)))
              .map(resource -> loadResourceContent(resource, Application.class).getGroups())
              .filter(groups -> groups != null)
              .flatMap(groups -> groups.stream())
              .filter(group -> checkIfMatches(group, groupFilter))
              .collect(Collectors.toList()));
      return pageResult;
    } else {
      throw new UnsupportedOperationException("Applications feature not configured for this realm");
    }
  }

  @Override
  public boolean validateCredentials(User user, String credential) {
    throw new NotImplementedException();
  }

  @Override
  public Optional<Application> getApplication(String applicationName) {
    if (config.get(FileKeysConfig.APP_SOURCE) != null) {
      Resource realmsResource =
          resourceLoader.getResource(config.get(FileKeysConfig.APP_SOURCE) + applicationName);
      if (realmsResource.exists()) {
        return Optional.of(loadResourceContent(realmsResource, Application.class));
      } else {
        return Optional.empty();
      }
    } else {
      throw new UnsupportedOperationException("Applications feature not configured for this realm");
    }
  }

  @Override
  public PageResult<Application> searchApplications(
      Application applicationFilter, PageableResult pageable, String searchOperator) {
    if (config.get(FileKeysConfig.APP_SOURCE) != null) {
      PageResult<Application> pageResult = new PageResult<>();
      pageResult.setResults(
          Arrays.stream(getFilenamesFromSource(config.get(FileKeysConfig.APP_SOURCE)))
              .map(resource -> loadResourceContent(resource, Application.class))
              .filter(app -> checkIfMatches(app, applicationFilter))
              .collect(Collectors.toList()));
      return pageResult;
    } else {
      throw new UnsupportedOperationException("Applications feature not configured for this realm");
    }
  }

  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @SuppressWarnings("unchecked")
  private <TestedClazz> boolean checkIfMatches(TestedClazz toTest, TestedClazz filter) {
    for (Field field : (toTest.getClass()).getDeclaredFields()) {
      field.setAccessible(true);
      try {
        if (field.get(filter) != null && !field.get(filter).equals(field.get(toTest))) {
          Object object = field.get(filter);
          if (object.getClass() == HashMap.class) {
            for (String key : ((HashMap<String, String>) object).keySet()) {
              if (!((HashMap<String, String>) object)
                  .get(key)
                  .equalsIgnoreCase(((HashMap<String, String>) field.get(toTest)).get(key))) {
                return false;
              }
            }
          } else if (object.getClass() == ArrayList.class) {
            for (Object value : (ArrayList<Object>) object) {
              if (!((ArrayList<Object>) field.get(toTest))
                  .stream().anyMatch(t -> checkIfMatches(t, value))) {
                return false;
              }
            }
          } else {
            return false;
          }
        }
      } catch (IllegalArgumentException | IllegalAccessException e) {
        throw new RuntimeException("Failed to compare resources", e);
      }
    }
    return true;
  }

  private Resource[] getFilenamesFromSource(String source) {
    try {
      PathMatchingResourcePatternResolver pathMatcher = new PathMatchingResourcePatternResolver();
      return pathMatcher.getResources(source + "/*");
    } catch (IOException e) {
      throw new RuntimeException("Error reading filenames in path " + source, e);
    }
  }

  private <ReturnClazz> ReturnClazz loadResourceContent(
      Resource resource, Class<ReturnClazz> returnClazz) {
    try {
      return mapper.readValue(resource.getInputStream(), returnClazz);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load resource", e);
    }
  }

  @Override
  public Optional<User> getUserByMail(String mail) {
    logger.debug("Searching user with mail {}", mail);
    User searchedUser = new User();
    searchedUser.setMail(mail);
    PageResult<User> users = searchUsers(searchedUser, null, null);
    if (users.getResults().size() > 1) {
      throw new MultipleUserWithSameMailException(mail);
    } else {
      return users.getResults().stream().findFirst();
    }
  }

  @Override
  public Optional<Group> getManagerGroup(String applicationName) {
    throw new NotImplementedException();
  }
}
