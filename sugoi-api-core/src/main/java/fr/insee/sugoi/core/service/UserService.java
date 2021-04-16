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
package fr.insee.sugoi.core.service;

import fr.insee.sugoi.core.exceptions.UserAlreadyExistException;
import fr.insee.sugoi.core.exceptions.UserNotCreatedException;
import fr.insee.sugoi.core.exceptions.UserNotFoundException;
import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.model.SearchType;
import fr.insee.sugoi.model.User;
import java.util.Optional;

public interface UserService {

  /**
   * Create an user if not already exists in the realm (check on the username).
   *
   * @param realm
   * @param storage
   * @param user
   * @return the created user
   * @throws UserAlreadyExistException if an user with the same username already exist in the realm
   * @throws UserNotCreatedException if user is not found after create
   */
  User create(String realm, String storage, User user);

  /**
   * Update the user if the user already exists in the realm
   *
   * @param realm
   * @param storage
   * @param user
   * @throws UserNotFoundException if user is not found in the realm
   */
  void update(String realm, String storage, User user);

  /**
   * Delete an existing user (if the user already exists in the realm)
   *
   * @param realm
   * @param storage
   * @param id
   * @throws UserNotFoundException if user is not found in the realm
   */
  void delete(String realm, String storage, String id);

  /**
   * Find a user by it's username in a realm
   *
   * @param realm
   * @param storageName
   * @param idep
   * @return an optional of user
   */
  Optional<User> findById(String realm, String storageName, String idep);

  /**
   * Find users by criterias in a realm
   *
   * @param realm
   * @param storageName
   * @param userProperties
   * @param pageable
   * @param typeRecherche
   * @return a list of users
   */
  PageResult<User> findByProperties(
      String realm,
      String storageName,
      User userProperties,
      PageableResult pageable,
      SearchType typeRecherche);
}
