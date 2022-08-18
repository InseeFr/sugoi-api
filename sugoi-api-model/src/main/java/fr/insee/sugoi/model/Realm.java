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
package fr.insee.sugoi.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.insee.sugoi.model.technics.StoreMapping;
import fr.insee.sugoi.model.technics.UiField;
import java.util.*;
import net.sf.ehcache.pool.sizeof.annotations.IgnoreSizeOf;

public class Realm implements SugoiObject {

  private String name;
  private String url;
  private String port;
  private String appSource;
  private List<UserStorage> userStorages;

  @JsonDeserialize(using = RealmConfigKeysDeserializer.class)
  @IgnoreSizeOf
  private Map<RealmConfigKeys, String> properties = new HashMap<>();

  @IgnoreSizeOf
  private Map<UIMappingType, List<UiField>> uiMapping = new EnumMap<>(UIMappingType.class);

  @IgnoreSizeOf private List<StoreMapping> groupMappings;
  @IgnoreSizeOf private List<StoreMapping> applicationMappings;

  private String readerType;
  private String writerType;

  public List<StoreMapping> getGroupMappings() {
    return groupMappings;
  }

  public void setGroupMappings(List<StoreMapping> groupMappings) {
    this.groupMappings = groupMappings;
  }

  public List<StoreMapping> getApplicationMappings() {
    return applicationMappings;
  }

  public void setApplicationMappings(List<StoreMapping> applicationMappings) {
    this.applicationMappings = applicationMappings;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<UserStorage> getUserStorages() {
    return this.userStorages;
  }

  public enum UIMappingType {
    UI_ORGANIZATION_MAPPING("uiOrganizationMapping"),
    UI_USER_MAPPING("uiUserMapping");

    private final String type;

    UIMappingType(String type) {
      this.type = type;
    }

    @JsonValue
    public String getType() {
      return type;
    }
  }

  public void setUserStorages(List<UserStorage> userStorages) {
    this.userStorages = userStorages;
  }

  public String getUrl() {
    return this.url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getPort() {
    return this.port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getAppSource() {
    return appSource;
  }

  public void setAppSource(String appSource) {
    this.appSource = appSource;
  }

  public void setProperties(Map<RealmConfigKeys, String> properties) {
    this.properties = properties;
  }

  public void addProperty(RealmConfigKeys name, String value) {
    this.properties.put(name, value);
  }

  public Map<RealmConfigKeys, String> getProperties() {
    return properties;
  }

  public Optional<UserStorage> getUserStorageByName(String name) {
    if (userStorages != null) {
      return userStorages.stream().filter(s -> s.getName().equalsIgnoreCase(name)).findFirst();
    } else {
      return Optional.empty();
    }
  }

  @Override
  public String toString() {
    return "Realm [appSource="
        + appSource
        + ", name="
        + name
        + ", url="
        + url
        + ", userStorages="
        + userStorages
        + "]";
  }

  public String getReaderType() {
    return readerType;
  }

  public void setReaderType(String readerType) {
    this.readerType = readerType;
  }

  public String getWriterType() {
    return writerType;
  }

  public void setWriterType(String writerType) {
    this.writerType = writerType;
  }

  public Map<UIMappingType, List<UiField>> getUiMapping() {
    return uiMapping;
  }

  public void setUiMapping(Map<UIMappingType, List<UiField>> uiMapping) {
    this.uiMapping = uiMapping;
  }
}
