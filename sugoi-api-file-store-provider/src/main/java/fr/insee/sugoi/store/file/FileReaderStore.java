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
import java.util.stream.Collectors;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class FileReaderStore implements ReaderStore {

  private static final Logger logger = LogManager.getLogger(FileReaderStore.class);

  @Autowired ResourceLoader resourceLoader;
  private Map<String, String> config;
  private ObjectMapper mapper = new ObjectMapper();

  public FileReaderStore(Map<String, String> generateConfig) {
    this.config = generateConfig;
  }

  @Override
  public User getUser(String id) {
    Resource realmsResource =
        resourceLoader.getResource(config.get(FileKeysConfig.USER_SOURCE) + id);
    if (realmsResource.exists()) {
      User user = loadResourceContent(realmsResource, User.class);
      // suborganization is loaded as an independant resource
      if (user.getOrganization() != null && user.getOrganization().getIdentifiant() != null) {
        try {
          user.setOrganization(getOrganization(user.getOrganization().getIdentifiant()));
        } catch (RuntimeException e) {
          logger.error("Unable to retrieve user's organization", e);
        }
      }
      return user;
    }
    return null;
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
  public Organization getOrganization(String id) {
    if (config.get(FileKeysConfig.ORGANIZATION_SOURCE) != null) {
      Resource realmsResource =
          resourceLoader.getResource(config.get(FileKeysConfig.ORGANIZATION_SOURCE) + id);
      if (realmsResource.exists()) {
        Organization organization = loadResourceContent(realmsResource, Organization.class);
        // suborganization is loaded as an independant resource
        if (organization.getOrganization() != null
            && organization.getOrganization().getIdentifiant() != null) {
          try {
            organization.setOrganization(
                getOrganization(organization.getOrganization().getIdentifiant()));
          } catch (RuntimeException e) {
            logger.error("Unable to retrieve organization's suborganization", e);
          }
        }
        return organization;
      }
      return null;
    } else {
      throw new UnsupportedOperationException(
          "Organizations feature not configured for this storage");
    }
  }

  @Override
  public PageResult<User> getUsersInGroup(String appName, String groupName) {
    PageResult<User> pageResult = new PageResult<>();
    Group group = getGroup(appName, groupName);
    if (group != null && group.getUsers() != null) {
      pageResult.setResults(
          group.getUsers().stream()
              .map(user -> getUser(user.getUsername()))
              .filter(user -> user != null)
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
  public Group getGroup(String appName, String groupName) {
    Application application = getApplication(appName);
    if (application != null) {
      return getApplication(appName).getGroups() != null
          ? getApplication(appName).getGroups().stream()
              .filter(group -> group.getName().equalsIgnoreCase(groupName))
              .findFirst()
              .orElse(null)
          : null;
    } else {
      throw new RuntimeException("Application " + appName + " not found");
    }
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
  public Application getApplication(String applicationName) {
    if (config.get(FileKeysConfig.APP_SOURCE) != null) {
      Resource realmsResource =
          resourceLoader.getResource(config.get(FileKeysConfig.APP_SOURCE) + applicationName);
      if (realmsResource.exists()) {
        return loadResourceContent(realmsResource, Application.class);
      }
      return null;
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
}
