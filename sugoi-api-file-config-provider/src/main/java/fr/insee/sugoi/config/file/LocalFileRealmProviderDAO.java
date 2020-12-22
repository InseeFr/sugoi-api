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
package fr.insee.sugoi.config.file;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.model.Realm;
import java.io.InputStream;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    value = "fr.insee.sugoi.realm.config.type",
    havingValue = "local",
    matchIfMissing = true)
public class LocalFileRealmProviderDAO implements RealmProvider {

  @Value("${fr.insee.sugoi.realm.config.local.path:classpath:/realms.json}")
  private String localFilePath;

  @Autowired ResourceLoader resourceLoader;

  private List<Realm> realms;
  private ObjectMapper mapper = new ObjectMapper();

  @Override
  public Realm load(String realmName) {
    if (realms == null) {
      realms = findAll();
    }
    return realms.stream()
        .filter(r -> r.getName().equalsIgnoreCase(realmName))
        .findFirst()
        .orElse(null);
  }

  @Override
  public List<Realm> findAll() {
    try {
      Resource realmsResource = resourceLoader.getResource(localFilePath);
      if (!realmsResource.exists()) {
        throw new IllegalArgumentException("No resources found in " + localFilePath);
      }
      InputStream is = realmsResource.getInputStream();
      realms = mapper.readValue(is, new TypeReference<List<Realm>>() {});
    } catch (Exception e) {
      throw new IllegalArgumentException("No resources found in " + localFilePath, e);
    }
    return realms;
  }
}
