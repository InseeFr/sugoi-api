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
package fr.insee.sugoi.core.store.impl;

import fr.insee.sugoi.core.exceptions.InvalidUserStorage;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.store.ReaderStore;
import fr.insee.sugoi.core.store.Store;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.core.store.StoreStorage;
import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StoreProviderImpl implements StoreProvider {

  @Autowired private RealmProvider realmProvider;

  @Autowired private StoreStorage storeStorage;

  protected static final Logger logger = LogManager.getLogger(StoreProviderImpl.class);

  @Override
  public Store getStoreForUserStorage(String realmName, String userStorageName) {
    Realm r = realmProvider.load(realmName);
    UserStorage us = realmProvider.loadUserStorageByUserStorageName(realmName, userStorageName);
    return storeStorage.getStore(r, us);
  }

  @Override
  public ReaderStore getReaderStore(String realm, String storage) {
    if (storage != null) {
      return this.getStoreForUserStorage(
              realm,
              (storage != null)
                  ? storage
                  : realmProvider.load(realm).getUserStorages().get(0).getName())
          .getReader();
    }
    logger.info("User storage can not be null");
    throw new InvalidUserStorage("User storage can not be null");
  }

  @Override
  public WriterStore getWriterStore(String realm, String storage) {
    if (storage != null) {
      return this.getStoreForUserStorage(realm, storage).getWriter();
    }
    logger.error("User storage can not be null");
    throw new InvalidUserStorage("User storage can not be null");
  }

  @Override
  public List<ReaderStore> getReaderStores(String realm) {
    Realm r = realmProvider.load(realm);
    return r.getUserStorages().stream()
        .map(us -> storeStorage.getStore(r, us).getReader())
        .collect(Collectors.toList());
  }

  @Override
  public WriterStore getWriterStore(String realm) {
    return this.getStoreForUserStorage(
            realm, realmProvider.load(realm).getUserStorages().get(0).getName())
        .getWriter();
  }

  @Override
  public ReaderStore getReaderStore(String realm) {
    return this.getStoreForUserStorage(
            realm, realmProvider.load(realm).getUserStorages().get(0).getName())
        .getReader();
  }
}
