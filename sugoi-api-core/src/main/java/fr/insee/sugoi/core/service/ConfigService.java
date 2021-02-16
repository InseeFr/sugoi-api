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

import fr.insee.sugoi.core.exceptions.RealmNotFoundException;
import fr.insee.sugoi.model.Realm;
import java.util.List;

/** Managing realm configuration */
public interface ConfigService {

  /**
   * Finds a realm by its name by using the RealmProvider Bean The result is cached.
   *
   * @throws RealmNotFoundException in case no realm is found
   * @param name
   * @return the Realm
   */
  Realm getRealm(String name);

  /**
   * Finds all realms using the RelmProvider bean. The resulting list is cached.
   *
   * @return a list of all realms
   */
  List<Realm> getRealms();

  void deleteRealm(String realmName);

  void updateRealm(Realm realm);

  void createRealm(Realm realm);
}
