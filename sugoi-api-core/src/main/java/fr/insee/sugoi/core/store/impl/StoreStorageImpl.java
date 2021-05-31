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

import fr.insee.sugoi.core.store.ReaderStore;
import fr.insee.sugoi.core.store.Store;
import fr.insee.sugoi.core.store.StoreStorage;
import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "fr.insee.sugoi.store")
public class StoreStorageImpl implements StoreStorage {

  private static final Logger logger = LogManager.getLogger(StoreStorageImpl.class);

  private static final Map<String, Store> connections = new HashMap<>();

  @Autowired private ApplicationContext applicationContext;

  @Override
  public Store getStore(Realm realm, UserStorage userStorage) {
    String writerType = realm.getWriterType();
    String readerType = realm.getReaderType();
    if (readerType == null || writerType == null) {
      throw new RuntimeException("Realm must have a defaultReaderType and a defaultWriterType");
    }
    String name = realm.getName() + "_" + userStorage.getName();
    if (!connections.containsKey(name)) {
      logger.info("Chargement de la configuration {}", name);
      WriterStore writerStore =
          (WriterStore) applicationContext.getBean(writerType, realm, userStorage);
      ReaderStore readerStore =
          (ReaderStore) applicationContext.getBean(readerType, realm, userStorage);
      logger.info(
          "Création de la configuration de type ({},{}) pour {}",
          readerStore.getClass().getSimpleName(),
          writerStore.getClass().getSimpleName(),
          name);
      connections.put(name, new Store(readerStore, writerStore));
    }

    return connections.get(name);
  }
}
