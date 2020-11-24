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

import java.util.Map;

public class UserStorage {
  private String name;
  private String userSource;
  private String organizationSource;
  private Map<String, String> properties;
  private String readerType;
  private String writerType;

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

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  @Override
  public String toString() {
    return "UserStorage [name="
        + name
        + ", organizationSource="
        + organizationSource
        + ", properties="
        + properties
        + ", userSource="
        + userSource
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
}
