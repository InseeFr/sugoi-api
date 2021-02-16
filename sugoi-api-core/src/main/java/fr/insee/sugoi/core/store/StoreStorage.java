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
package fr.insee.sugoi.core.store;

import fr.insee.sugoi.core.store.impl.StoreStorageImpl;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;

/**
 * Store configuration from realm Each realm declares a writerType and a readerType (could be null).
 *
 * <p>Default implementation fetch beans from the name of the type
 *
 * @see StoreStorageImpl for details
 */
public interface StoreStorage {

  /**
   * Get the store (writer/reader) for the realm/userStorage pair
   *
   * @param realm
   * @param userStorage
   * @return the store for the realm/userstorage wanted
   */
  public Store getStore(Realm realm, UserStorage userStorage);
}
