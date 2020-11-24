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
package fr.insee.sugoi.core.configuration.impl;

import fr.insee.sugoi.core.configuration.StoreStorage;
import fr.insee.sugoi.core.store.ReaderStore;
import fr.insee.sugoi.core.store.Store;
import fr.insee.sugoi.core.store.WriterStore;
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

  private String defaultWriter;

  private String defaultReader;

  @Override
  public Store getStore(String readerType, String writerType, Map<String, String> config) {
    if (writerType == null) {
      writerType = defaultWriter;
    }
    if (readerType == null) {
      readerType = defaultReader;
    }

    if (!connections.containsKey(config.get("name"))) {
      logger.info("Chargement de la configuration {}", config.get("name"));
      WriterStore writerStore = (WriterStore) applicationContext.getBean(writerType, config);
      ReaderStore readerStore = (ReaderStore) applicationContext.getBean(readerType, config);
      logger.info(
          "Création de la configuration de type ({},{}) pour {}",
          readerStore.getClass().getSimpleName(),
          writerStore.getClass().getSimpleName(),
          config.get("name"));
      connections.put(config.get("name"), new Store(readerStore, writerStore));
    }

    return connections.get(config.get("name"));
  }

  public String getDefaultWriter() {
    return defaultWriter;
  }

  public void setDefaultWriter(String defaultWriter) {
    this.defaultWriter = defaultWriter;
  }

  public String getDefaultReader() {
    return defaultReader;
  }

  public void setDefaultReader(String defaultReader) {
    this.defaultReader = defaultReader;
  }
}
