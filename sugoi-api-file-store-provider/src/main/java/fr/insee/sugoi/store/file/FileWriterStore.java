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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.paging.PasswordChangeRequest;
import fr.insee.sugoi.model.paging.SendMode;
import fr.insee.sugoi.store.file.configuration.FileKeysConfig;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

public class FileWriterStore implements WriterStore {

  @Autowired ResourceLoader resourceLoader;

  private Map<String, String> config;
  private ObjectMapper mapper = new ObjectMapper();
  FileReaderStore fileReaderStore;

  public FileWriterStore(Map<String, String> generateConfig) {
    this.config = generateConfig;
    this.fileReaderStore = new FileReaderStore(generateConfig);
  }

  @Override
  public void deleteUser(String id) {
    fileReaderStore.setResourceLoader(resourceLoader);
    User user = fileReaderStore.getUser(id);
    user.getGroups().forEach(group -> deleteUserFromGroup(group.getAppName(), group.getName(), id));
    deleteResourceFile(config.get(FileKeysConfig.USER_SOURCE), id);
  }

  @Override
  public User createUser(User user) {
    try {
      createResourceFile(
          config.get(FileKeysConfig.USER_SOURCE),
          user.getUsername(),
          mapper.writeValueAsString(user));
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error mapping user" + user.getUsername(), e);
    }
    return user;
  }

  @Override
  public User updateUser(User updatedUser) {
    try {
      updateResourceFile(
          config.get(FileKeysConfig.USER_SOURCE),
          updatedUser.getUsername(),
          mapper.writeValueAsString(updatedUser));
      return updatedUser;
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error mapping user" + updatedUser.getUsername(), e);
    }
  }

  @Override
  public void deleteGroup(String appName, String groupName) {
    fileReaderStore.setResourceLoader(resourceLoader);
    Application application = fileReaderStore.getApplication(appName);
    if (application != null) {
      application.getGroups().removeIf(group -> group.getName().equalsIgnoreCase(groupName));
      updateApplication(application);
    } else {
      throw new RuntimeException("Application " + appName + " doesn't exist");
    }
  }

  @Override
  public Group createGroup(String appName, Group appGroup) {
    fileReaderStore.setResourceLoader(resourceLoader);
    Application application = fileReaderStore.getApplication(appName);
    if (application != null) {
      if (application.getGroups() == null) {
        application.setGroups(new ArrayList<>());
      }
      if (!application.getGroups().stream()
          .anyMatch(group -> group.getName().equalsIgnoreCase(appGroup.getName()))) {
        appGroup.setAppName(appName);
        application.getGroups().add(appGroup);
        updateApplication(application);
        return appGroup;
      } else {
        throw new RuntimeException("Group " + appGroup.getName() + " already exists in " + appName);
      }
    } else {
      throw new RuntimeException("Application " + appName + " doesn't exist");
    }
  }

  @Override
  public Group updateGroup(String appName, Group updatedGroup) {
    fileReaderStore.setResourceLoader(resourceLoader);
    Application application = fileReaderStore.getApplication(appName);
    if (application != null) {
      if (application.getGroups() != null
          && application.getGroups().stream()
              .anyMatch(
                  filterGroup -> filterGroup.getName().equalsIgnoreCase(updatedGroup.getName()))) {

        application
            .getGroups()
            .removeIf(
                filterGroup -> filterGroup.getName().equalsIgnoreCase(updatedGroup.getName()));
        application.getGroups().add(updatedGroup);
        updateApplication(application);

        return updatedGroup;
      } else {
        throw new RuntimeException(
            "Group " + updatedGroup.getName() + " doesn't exist in " + appName);
      }
    } else {
      throw new RuntimeException("Application " + appName + " doesn't exist");
    }
  }

  @Override
  public void deleteOrganization(String name) {
    if (config.get(FileKeysConfig.ORGANIZATION_SOURCE) != null) {
      deleteResourceFile(config.get(FileKeysConfig.ORGANIZATION_SOURCE), name);
    } else {
      throw new UnsupportedOperationException(
          "organizations feature not configured for this storage");
    }
  }

  @Override
  public Organization createOrganization(Organization organization) {
    if (config.get(FileKeysConfig.ORGANIZATION_SOURCE) != null) {
      try {
        createResourceFile(
            config.get(FileKeysConfig.ORGANIZATION_SOURCE),
            organization.getIdentifiant(),
            mapper.writeValueAsString(organization));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(
            "Error mapping organization " + organization.getIdentifiant(), e);
      }
      return organization;
    } else {
      throw new UnsupportedOperationException(
          "organizations feature not configured for this storage");
    }
  }

  @Override
  public Organization updateOrganization(Organization updatedOrganization) {
    if (config.get(FileKeysConfig.ORGANIZATION_SOURCE) != null) {
      try {
        updateResourceFile(
            config.get(FileKeysConfig.ORGANIZATION_SOURCE),
            updatedOrganization.getIdentifiant(),
            mapper.writeValueAsString(updatedOrganization));
        return updatedOrganization;
      } catch (JsonProcessingException e) {
        throw new RuntimeException(
            "Error mapping organization" + updatedOrganization.getIdentifiant(), e);
      }
    } else {
      throw new UnsupportedOperationException(
          "organizations feature not configured for this storage");
    }
  }

  /** Group membership information is removed from the user object and from the group object */
  @Override
  public void deleteUserFromGroup(String appName, String groupName, String userId) {
    fileReaderStore.setResourceLoader(resourceLoader);
    Application application = fileReaderStore.getApplication(appName);
    if (application != null) {
      Group group =
          application.getGroups() != null
              ? application.getGroups().stream()
                  .filter(filterGroup -> filterGroup.getName().equalsIgnoreCase(groupName))
                  .findFirst()
                  .orElse(null)
              : null;
      if (group != null) {
        User user = fileReaderStore.getUser(userId);
        user.getGroups()
            .removeIf(
                groupFilter ->
                    groupFilter.getAppName().equalsIgnoreCase(appName)
                        && groupFilter.getName().equalsIgnoreCase(groupName));
        updateUser(user);
        if (group.getUsers() != null) {
          try {
            group
                .getUsers()
                .removeIf(userFilter -> userFilter.getUsername().equalsIgnoreCase(userId));
            updateResourceFile(
                config.get(FileKeysConfig.APP_SOURCE),
                application.getName(),
                mapper.writeValueAsString(application));
          } catch (JsonProcessingException e) {
            throw new RuntimeException("Error mapping application " + application.getName(), e);
          }
        }
      } else {
        throw new RuntimeException("Group " + groupName + " doesn't exist in " + appName);
      }
    } else {
      throw new RuntimeException("Application " + appName + " doesn't exist");
    }
  }

  /**
   * Group membership information is added on the user object and on the group object, user's groups
   * are simplified groups to avoid information loop
   */
  @Override
  public void addUserToGroup(String appName, String groupName, String userId) {
    fileReaderStore.setResourceLoader(resourceLoader);
    Application application = fileReaderStore.getApplication(appName);
    if (application != null) {
      Group group =
          application.getGroups() != null
              ? application.getGroups().stream()
                  .filter(filterGroup -> filterGroup.getName().equalsIgnoreCase(groupName))
                  .findFirst()
                  .orElse(null)
              : null;
      if (group != null) {
        User user = fileReaderStore.getUser(userId);
        if (user != null) {
          try {
            if (user.getGroups() == null) {
              user.setGroups(new ArrayList<>());
            }
            user.getGroups().add(new Group(appName, groupName));
            updateUser(user);
            if (group.getUsers() == null) {
              group.setUsers(new ArrayList<>());
            }
            group.getUsers().add(user);
            updateResourceFile(
                config.get(FileKeysConfig.APP_SOURCE),
                application.getName(),
                mapper.writeValueAsString(application));
          } catch (JsonProcessingException e) {
            throw new RuntimeException("Error mapping application " + application.getName(), e);
          }
        } else {
          throw new RuntimeException("User " + userId + " not found");
        }
      } else {
        throw new RuntimeException("Group " + groupName + " doesn't exist in " + appName);
      }
    } else {
      throw new RuntimeException("Application " + appName + " doesn't exist");
    }
  }

  @Override
  public void reinitPassword(
      User user, String generatedPassword, PasswordChangeRequest pcr, List<SendMode> sendMode) {
    throw new UnsupportedOperationException("Password actions are not supported on file storage");
  }

  @Override
  public void initPassword(
      User user, String password, PasswordChangeRequest pcr, List<SendMode> sendMode) {
    throw new UnsupportedOperationException("Password actions are not supported on file storage");
  }

  @Override
  public void changePasswordResetStatus(User user, boolean isReset) {
    throw new UnsupportedOperationException("Password actions are not supported on file storage");
  }

  @Override
  public Application createApplication(Application application) {
    if (config.get(FileKeysConfig.APP_SOURCE) != null) {
      try {
        application.getGroups().forEach(group -> group.setUsers(null));
        createResourceFile(
            config.get(FileKeysConfig.APP_SOURCE),
            application.getName(),
            mapper.writeValueAsString(application));
        return application;
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Error mapping application " + application.getName(), e);
      }
    } else {
      throw new UnsupportedOperationException("Applications feature not configured for this realm");
    }
  }

  @Override
  public Application updateApplication(Application updatedApplication) {
    if (config.get(FileKeysConfig.APP_SOURCE) != null) {
      try {
        fileReaderStore.setResourceLoader(resourceLoader);
        for (Group updatedGroup : updatedApplication.getGroups()) {
          Group existingGroup =
              fileReaderStore.getGroup(updatedApplication.getName(), updatedGroup.getName());
          if (existingGroup != null) {
            updatedGroup.setUsers(existingGroup.getUsers());
          }
        }
        updateResourceFile(
            config.get(FileKeysConfig.APP_SOURCE),
            updatedApplication.getName(),
            mapper.writeValueAsString(updatedApplication));
        return updatedApplication;
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Error mapping application" + updatedApplication.getName(), e);
      }
    } else {
      throw new UnsupportedOperationException("Applications feature not configured for this realm");
    }
  }

  @Override
  public void deleteApplication(String applicationName) {
    if (config.get(FileKeysConfig.APP_SOURCE) != null) {
      deleteResourceFile(config.get(FileKeysConfig.APP_SOURCE), applicationName);
    } else {
      throw new UnsupportedOperationException("Applications feature not configured for this realm");
    }
  }

  @Override
  public void changePassword(
      User user, String oldPassword, String newPassword, PasswordChangeRequest pcr) {
    throw new NotImplementedException();
  }

  private void createResourceFile(String source, String resourceName, String resourceValue) {
    try {
      File file = resourceLoader.getResource(source + resourceName).getFile();
      file.createNewFile();
      updateResourceFile(source, resourceName, resourceValue);
    } catch (IOException e) {
      throw new RuntimeException("Error creating new resource file", e);
    }
  }

  private void deleteResourceFile(String source, String resourceName) {
    try {
      File file = resourceLoader.getResource(source + resourceName).getFile();
      file.delete();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void updateResourceFile(String source, String resourceName, String resourceValue) {
    try {
      File file = resourceLoader.getResource(source + resourceName).getFile();
      FileWriter fWriter = new FileWriter(file);
      fWriter.write(resourceValue);
      fWriter.close();
    } catch (IOException e) {
      throw new RuntimeException("Error writing in file", e);
    }
  }

  @Override
  public void addAppManagedAttribute(String userId, String attributeKey, String attribute) {
    throw new NotImplementedException();
  }

  @Override
  public void deleteAppManagedAttribute(String userId, String attributeKey, String attribute) {
    throw new NotImplementedException();
  }
}
