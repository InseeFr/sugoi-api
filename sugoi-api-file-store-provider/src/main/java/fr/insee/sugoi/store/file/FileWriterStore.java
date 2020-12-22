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

import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWriterStore extends AbstractFileStore implements WriterStore {

  private static Logger logger = LoggerFactory.getLogger(FileWriterStore.class);

  public FileWriterStore(Map<String, String> config) {
    super(config);
  }

  @Override
  public void deleteUser(String id) {
    Path userPath = Paths.get(storeFolder, relativeFolderUsers).resolve(pathEncode(id) + EXTENSION);
    try {
      Files.deleteIfExists(userPath);
    } catch (IOException e) {
      logger.error("Unable to delete " + userPath.toFile().getName(), e);
    }
  }

  @Override
  public User createUser(User user) {
    File userFile =
        Paths.get(storeFolder, relativeFolderUsers)
            .resolve(pathEncode(user.getUsername()) + EXTENSION)
            .toFile();
    try {
      mapper.writeValue(userFile, user);
    } catch (IOException e) {
      logger.error("Unable to create " + user, e);
    }
    return user;
  }

  @Override
  public User updateUser(User updatedUser) {
    File userFile =
        Paths.get(storeFolder, relativeFolderUsers)
            .resolve(pathEncode(updatedUser.getUsername()) + EXTENSION)
            .toFile();
    try {
      mapper.writeValue(userFile, updatedUser);
    } catch (IOException e) {
      logger.error("Unable to create " + updatedUser, e);
    }
    return updatedUser;
  }

  @Override
  public void deleteGroup(String applicationName, String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Group createGroup(String applicationName, Group group) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Group updateGroup(String applicationName, Group updatedGroup) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteOrganization(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Organization createOrganization(Organization organization) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Organization updateOrganization(Organization updatedOrganization) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteUserFromGroup(String applicationName, String groupName, String userId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addUserToGroup(String applicationName, String groupName, String userId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void reinitPassword(User user) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void initPassword(User user, String password) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void changePasswordResetStatus(User user, boolean isReset) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Application createApplication(Application application) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Application updateApplication(Application updatedApplication) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteApplication(String applicationName) {
    throw new UnsupportedOperationException();
  }
}
