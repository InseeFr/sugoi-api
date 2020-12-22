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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractFileStore {

  @Value(value = "${fr.insee.sugoi.store.file.folder:}")
  protected String storeFolder;

  protected String relativeFolderUsers;
  protected String relativeFolderOrganizations;
  protected String relativeFolderApplications;

  protected ObjectMapper mapper = new ObjectMapper();

  protected static final String EXTENSION = ".json";

  private static Logger logger = LoggerFactory.getLogger(AbstractFileStore.class);

  protected Map<String, String> config;

  @PostConstruct
  void init() {
    logger.info("Loading store from  {}", storeFolder);
    relativeFolderUsers = config.getOrDefault("userSource", "users");
    relativeFolderOrganizations = config.getOrDefault("organizationSource", "orgs");
    relativeFolderApplications = config.getOrDefault("applicationSource", "apps");
    Paths.get(storeFolder).resolve(relativeFolderUsers).toFile().mkdirs();
    Paths.get(storeFolder).resolve(relativeFolderOrganizations).toFile().mkdirs();
    Paths.get(storeFolder).resolve(relativeFolderApplications).toFile().mkdirs();
  }

  protected AbstractFileStore(Map<String, String> config) {
    this.config = config;
  }

  protected <T> T getFromFile(File f, Class<T> type) {
    try {
      return mapper.readValue(f, type);
    } catch (IOException e) {
      logger.error(f.getName() + " unparsable", e);
      return null;
    }
  }

  protected String pathEncode(String path) {
    return path.replace("/", "_").replace("\\", "_");
  }

  public String getStoreFolder() {
    return storeFolder;
  }

  public void setStoreFolder(String storeFolder) {
    this.storeFolder = storeFolder;
  }
}
