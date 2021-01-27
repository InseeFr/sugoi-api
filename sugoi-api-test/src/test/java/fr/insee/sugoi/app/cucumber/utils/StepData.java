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

import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import java.util.List;

public class StepData {

  ResponseResults latestResponse = null;
  User user = null;
  List<User> users = null;
  Organization organization = null;
  List<Organization> organizations = null;
  String defaultTomcatUrl = "/tomcat1";
  Realm realm = null;
  List<Realm> realms = null;

  public StepData() {}

  public ResponseResults getLatestResponse() {
    return latestResponse;
  }

  public void setLatestResponse(ResponseResults latestResponse) {
    this.latestResponse = latestResponse;
  }

  public User getUser() {
    return user;
  }

  public List<User> getUsers() {
    return users;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public void setUsers(List<User> users) {
    this.users = users;
  }

  public String getDefaultTomcatUrl() {
    return defaultTomcatUrl;
  }

  public void setDefaultTomcatUrl(String defaultTomcatUrl) {
    this.defaultTomcatUrl = defaultTomcatUrl;
  }

  public Organization getOrganization() {
    return organization;
  }

  public List<Organization> getOrganizations() {
    return organizations;
  }

  public void setOrganization(Organization organization) {
    this.organization = organization;
  }

  public void setOrganizations(List<Organization> organizations) {
    this.organizations = organizations;
  }

  public Realm getRealm() {
    return realm;
  }

  public void setRealm(Realm realm) {
    this.realm = realm;
  }

  public List<Realm> getRealms() {
    return realms;
  }

  public void setRealms(List<Realm> realms) {
    this.realms = realms;
  }
}
