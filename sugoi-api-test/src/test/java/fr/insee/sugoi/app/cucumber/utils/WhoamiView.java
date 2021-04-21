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
package fr.insee.sugoi.app.cucumber.utils;

import java.util.List;

public class WhoamiView {
  private String id;
  private List<String> readerRealm;
  private List<String> writerRealm;
  private List<String> appManager;
  private List<String> passwordRealm;
  private boolean isAdmin = false;

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<String> getReaderRealm() {
    return this.readerRealm;
  }

  public void setReaderRealm(List<String> readerRealm) {
    this.readerRealm = readerRealm;
  }

  public List<String> getWriterRealm() {
    return this.writerRealm;
  }

  public void setWriterRealm(List<String> writerRealm) {
    this.writerRealm = writerRealm;
  }

  public List<String> getAppManager() {
    return this.appManager;
  }

  public void setAppManager(List<String> appManager) {
    this.appManager = appManager;
  }

  public List<String> getPasswordRealm() {
    return this.passwordRealm;
  }

  public void setPasswordRealm(List<String> passwordRealm) {
    this.passwordRealm = passwordRealm;
  }

  public boolean isIsAdmin() {
    return this.isAdmin;
  }

  public void setIsAdmin(boolean isAdmin) {
    this.isAdmin = isAdmin;
  }

  public WhoamiView() {}
}
