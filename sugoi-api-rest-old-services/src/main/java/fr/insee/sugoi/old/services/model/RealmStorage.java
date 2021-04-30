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
package fr.insee.sugoi.old.services.model;

public class RealmStorage {
  private String realm;
  private String userStorage;

  public RealmStorage(String realmStorage) {
    String[] res = realmStorage.split("_", 2);
    this.realm = res[0];
    this.userStorage = res[1];
  }

  public RealmStorage(String realm, String storage) {
    this.realm = realm;
    this.userStorage = storage;
  }

  public String getRealm() {
    return realm;
  }

  public String getUserStorage() {
    return userStorage;
  }
}
