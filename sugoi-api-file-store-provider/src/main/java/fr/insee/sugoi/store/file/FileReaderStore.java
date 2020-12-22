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

import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.store.ReaderStore;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileReaderStore extends AbstractFileStore implements ReaderStore {

  private static Logger logger = LoggerFactory.getLogger(FileReaderStore.class);

  public FileReaderStore(Map<String, String> config) {
    super(config);
  }

  @Override
  public User getUser(String id) {
    Path userPath = Paths.get(storeFolder, relativeFolderUsers).resolve(pathEncode(id) + EXTENSION);
    if (userPath.toFile().exists() && userPath.toFile().canRead()) {
      try {
        return mapper.readValue(userPath.toFile(), User.class);
      } catch (IOException e) {
        logger.error("Error when getting user " + id, e);
      }
    }
    return null;
  }

  @Override
  public PageResult<User> searchUsers(
      String identifiant,
      String nomCommun,
      String description,
      String organisationId,
      String mail,
      PageableResult pageable,
      String typeRecherche,
      List<String> habilitations,
      String application,
      String role,
      String rolePropriete,
      String certificat) {
    File userFolder = Paths.get(storeFolder, relativeFolderUsers).toFile();
    List<User> users =
        Arrays.asList(userFolder.listFiles(f -> f.getName().endsWith(EXTENSION))).stream()
            .map(f -> getFromFile(f, User.class))
            .filter(u -> mail != null ? u.getMail().equalsIgnoreCase(mail) : true)
            .filter(u -> identifiant != null ? u.getUsername().equalsIgnoreCase(identifiant) : true)
            .limit(500)
            .collect(Collectors.toList());

    PageResult<User> pr = new PageResult<>();
    pr.setResults(users);
    return pr;
  }

  @Override
  public Organization getOrganization(String id) {
    Path userPath =
        Paths.get(storeFolder, relativeFolderOrganizations).resolve(pathEncode(id) + EXTENSION);
    if (userPath.toFile().exists() && userPath.toFile().canRead()) {
      try {
        return mapper.readValue(userPath.toFile(), Organization.class);
      } catch (IOException e) {
        logger.error("Error when getting Organization " + id, e);
      }
    }
    return null;
  }

  @Override
  public PageResult<User> getUsersInGroup(String applicationName, String groupName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public PageResult<Organization> searchOrganizations(
      Map<String, String> searchProperties, PageableResult pageable, String searchOperator) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Group getGroup(String applicationName, String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public PageResult<Group> searchGroups(
      String applicationName,
      Map<String, String> searchProperties,
      PageableResult pageable,
      String searchOperator) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean validateCredentials(User user, String credential) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Application getApplication(String applicationName) {
    Path userPath =
        Paths.get(storeFolder, relativeFolderApplications)
            .resolve(pathEncode(applicationName) + EXTENSION);
    if (userPath.toFile().exists() && userPath.toFile().canRead()) {
      try {
        return mapper.readValue(userPath.toFile(), Application.class);
      } catch (IOException e) {
        logger.error("Error when getting Application " + applicationName, e);
      }
    }
    return null;
  }

  @Override
  public PageResult<Application> searchApplications(
      Map<String, String> searchProperties, PageableResult pageable, String searchOperator) {
    throw new UnsupportedOperationException();
  }
}
