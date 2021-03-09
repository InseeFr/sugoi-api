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
import fr.insee.sugoi.core.model.PasswordChangeRequest;
import fr.insee.sugoi.core.model.SendMode;
import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
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
    deleteResourceFile(config.get("user_source"), id);
  }

  @Override
  public User createUser(User user) {
    try {
      createResourceFile(
          config.get("user_source"), user.getUsername(), mapper.writeValueAsString(user));
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error mapping user" + user.getUsername(), e);
    }
    return user;
  }

  @Override
  public User updateUser(User updatedUser) {
    try {
      updateResourceFile(
          config.get("user_source"),
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

  /**
   * To avoid unconsistency between users and groups, group is recreated without users and then
   * users are added
   */
  @Override
  public Group updateGroup(String appName, Group updatedGroup) {
    fileReaderStore.setResourceLoader(resourceLoader);
    Application application = fileReaderStore.getApplication(appName);
    if (application != null) {
      if (application.getGroups() != null
          && application.getGroups().stream()
              .anyMatch(
                  filterGroup -> filterGroup.getName().equalsIgnoreCase(updatedGroup.getName()))) {

        // simplify group and keep users
        List<User> usersToAdd =
            updatedGroup.getUsers() != null
                ? new ArrayList<>(updatedGroup.getUsers())
                : new ArrayList<>();
        updatedGroup.setUsers(new ArrayList<>());

        // update group in application
        application
            .getGroups()
            .removeIf(
                filterGroup -> filterGroup.getName().equalsIgnoreCase(updatedGroup.getName()));
        application.getGroups().add(updatedGroup);
        updateApplication(application);

        // add each users in updated group
        usersToAdd.forEach(
            user -> addUserToGroup(appName, updatedGroup.getName(), user.getUsername()));
        updatedGroup.setUsers(usersToAdd);
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
    deleteResourceFile(config.get("organization_source"), name);
  }

  @Override
  public Organization createOrganization(Organization organization) {
    try {
      createResourceFile(
          config.get("organization_source"),
          organization.getIdentifiant(),
          mapper.writeValueAsString(organization));
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error mapping organization " + organization.getIdentifiant(), e);
    }
    return organization;
  }

  @Override
  public Organization updateOrganization(Organization updatedOrganization) {
    try {
      updateResourceFile(
          config.get("organization_source"),
          updatedOrganization.getIdentifiant(),
          mapper.writeValueAsString(updatedOrganization));
      return updatedOrganization;
    } catch (JsonProcessingException e) {
      throw new RuntimeException(
          "Error mapping organization" + updatedOrganization.getIdentifiant(), e);
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
          group
              .getUsers()
              .removeIf(userFilter -> userFilter.getUsername().equalsIgnoreCase(userId));
          updateApplication(application);
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
          if (user.getGroups() == null) {
            user.setGroups(new ArrayList<>());
          }
          user.getGroups().add(new Group(appName, groupName));
          updateUser(user);
          if (group.getUsers() == null) {
            group.setUsers(new ArrayList<>());
          }
          group.getUsers().add(user);
          updateApplication(application);
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
    throw new NotImplementedException();
  }

  @Override
  public void initPassword(
      User user, String password, PasswordChangeRequest pcr, List<SendMode> sendMode) {
    throw new NotImplementedException();
  }

  @Override
  public void changePasswordResetStatus(User user, boolean isReset) {
    throw new NotImplementedException();
  }

  @Override
  public Application createApplication(Application application) {
    try {
      createResourceFile(
          config.get("app_source"), application.getName(), mapper.writeValueAsString(application));
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error mapping application " + application.getName(), e);
    }
    return application;
  }

  @Override
  public Application updateApplication(Application updatedApplication) {
    try {
      updateResourceFile(
          config.get("app_source"),
          updatedApplication.getName(),
          mapper.writeValueAsString(updatedApplication));
      return updatedApplication;
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error mapping application" + updatedApplication.getName(), e);
    }
  }

  @Override
  public void deleteApplication(String applicationName) {
    deleteResourceFile(config.get("app_source"), applicationName);
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
}
