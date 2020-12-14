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

import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.model.User;
import java.util.List;

public interface UserService {

  User searchUser(String domaine, String idep);

  PageResult<User> searchUsers(
      String identifiant,
      String nomCommun,
      String description,
      String organisationId,
      String domaineGestion,
      String mail,
      String cookie,
      int size,
      int offset,
      String typeRecherche,
      List<String> habilitations,
      String application,
      String role,
      String rolePropriete,
      String certificat);

  User create(String realm, String storage, User user);

  User delete(String realm, String id);
}
