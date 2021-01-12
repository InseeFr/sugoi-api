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

import java.util.List;

public class Realm {
  private String name;
  private String url;
  private String appSource;
  private List<UserStorage> userStorages;

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<UserStorage> getUserStorages() {
    return this.userStorages;
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

  public String getAppSource() {
    return appSource;
  }

  public void setAppSource(String appSource) {
    this.appSource = appSource;
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
}
