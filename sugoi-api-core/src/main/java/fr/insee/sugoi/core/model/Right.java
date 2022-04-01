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
package fr.insee.sugoi.core.model;

public class Right {

  private String realm = "";
  private String userStorage = "";
  private String application = "";

  public String getRealm() {
    return realm;
  }

  public void setRealm(String realm) {
    this.realm = realm;
  }

  public String getUserStorage() {
    return userStorage;
  }

  public void setUserStorage(String userStorage) {
    this.userStorage = userStorage;
  }

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (realm.equals("")) {
      sb.append("*_*");
    } else {
      sb.append(realm);
      if (!userStorage.equals("")) {
        sb.append("_" + userStorage);
      }
    }
    if (!application.equals("")) {
      sb.append("\\" + getApplication());
    }
    return sb.toString().toUpperCase();
  }
}
