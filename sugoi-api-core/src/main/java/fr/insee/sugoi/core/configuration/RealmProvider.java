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
package fr.insee.sugoi.core.configuration;

import fr.insee.sugoi.core.utils.Exceptions.RealmNotFoundException;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import java.util.List;

/**
 * This is called to fetch realms configuration.
 *
 * <p>All implementations need to declare a value for the property `fr.insee.sugoi.config.type` like
 * this :
 *
 * <pre>
 * @ConditionalOnProperty(
 *  value = "fr.insee.sugoi.realm.config.type",
 *  havingValue = "<value>",
 *  matchIfMissing = false)
 * </pre>
 */
public interface RealmProvider {

  public Realm load(String realmName) throws RealmNotFoundException;

  public default UserStorage loadUserStorageByUserStorageName(
      String realmName, String userStorageName) throws RealmNotFoundException {
    Realm r = load(realmName);
    if (r != null) {

      for (UserStorage us : r.getUserStorages()) {
        if (us.getName().equalsIgnoreCase(userStorageName)) {
          return us;
        }
      }
    }

    throw new RealmNotFoundException(String.format("No user Storage %s found", userStorageName));
  }

  public default Realm loadRealmByUserStorageName(String userStorageName)
      throws RealmNotFoundException {
    for (Realm r : findAll()) {
      for (UserStorage us : r.getUserStorages()) {
        if (us.getName().equalsIgnoreCase(userStorageName)) {
          return r;
        }
      }
    }
    throw new RealmNotFoundException(
        String.format("No Realm found for user Storage %s", userStorageName));
  }

  public List<Realm> findAll();
}
