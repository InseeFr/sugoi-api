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

import java.util.List;

public interface StoreProvider {

  public Store getStoreForUserStorage(String realmName, String userStorageName);

  public ReaderStore getReaderStore(String realm, String storage);

  public List<ReaderStore> getReaderStores(String realm);

  public WriterStore getWriterStore(String realm, String storage);

  /**
   * Get a random writer store for a realm, be aware use only if userStorage is not used for the
   * writing operation
   *
   * @param realm
   * @return
   */
  public WriterStore getWriterStore(String realm);

  /**
   * Get a random writer store for a realm, be aware use only if userStorage is not used for the
   * reading operation
   *
   * @param realm
   * @return
   */
  public ReaderStore getReaderStore(String realm);
}
