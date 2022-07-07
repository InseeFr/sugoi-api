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

import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.RealmConfigKeys;
import fr.insee.sugoi.model.UserStorage;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class FileStoreBeans {

  @Bean("FileReaderStore")
  @Lazy
  public FileReaderStore fileReaderStore(Realm realm, UserStorage userStorage) {
    return new FileReaderStore(generateConfig(realm, userStorage));
  }

  @Bean("FileWriterStore")
  @Lazy
  public FileWriterStore fileWriterStore(Realm realm, UserStorage userStorage) {
    return new FileWriterStore(generateConfig(realm, userStorage));
  }

  public Map<RealmConfigKeys, String> generateConfig(Realm realm, UserStorage userStorage) {
    Map<RealmConfigKeys, String> config = new HashMap<>();
    config.put(GlobalKeysConfig.APP_SOURCE, realm.getAppSource());
    config.put(GlobalKeysConfig.USER_SOURCE, userStorage.getUserSource());
    config.put(GlobalKeysConfig.ORGANIZATION_SOURCE, userStorage.getOrganizationSource());
    return config;
  }
}
