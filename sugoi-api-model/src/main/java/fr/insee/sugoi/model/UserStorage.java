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

import fr.insee.sugoi.model.technics.StoreMapping;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.ehcache.pool.sizeof.annotations.IgnoreSizeOf;

public class UserStorage implements Serializable {
  private String name;
  private String userSource;
  private String organizationSource;
  private String addressSource;

  @IgnoreSizeOf private Map<String, String> properties = new HashMap<>();

  @IgnoreSizeOf private List<StoreMapping> userMappings;

  @IgnoreSizeOf private List<StoreMapping> organizationMappings;

  public List<StoreMapping> getUserMappings() {
    return userMappings;
  }

  public void setUserMappings(List<StoreMapping> userMappings) {
    this.userMappings = userMappings;
  }

  public List<StoreMapping> getOrganizationMappings() {
    return organizationMappings;
  }

  public void setOrganizationMappings(List<StoreMapping> organizationMappings) {
    this.organizationMappings = organizationMappings;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUserSource() {
    return userSource;
  }

  public void setUserSource(String userSource) {
    this.userSource = userSource;
  }

  public String getOrganizationSource() {
    return organizationSource;
  }

  public void setOrganizationSource(String organizationSource) {
    this.organizationSource = organizationSource;
  }

  public String getAddressSource() {
    return addressSource;
  }

  public void setAddressSource(String addressSource) {
    this.addressSource = addressSource;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public void addProperty(String key, String value) {
    this.properties.put(key, value);
  }
}
