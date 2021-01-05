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

import fr.insee.sugoi.model.Realm;
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

  public Map<String, String> generateConfig(Realm realm, UserStorage userStorage) {
    Map<String, String> config = new HashMap<>();
    if (userStorage.getUserSource() != null) {
      config.put("userSource", userStorage.getUserSource());
    }
    if (userStorage.getOrganizationSource() != null) {
      config.put("organisationSource", userStorage.getOrganizationSource());
    }
    if (realm.getAppSource() != null) {
      config.put("applicationSource", realm.getAppSource());
    }
    return config;
  }
}
