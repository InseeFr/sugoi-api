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
package fr.insee.sugoi.services.view;

public class UserStorageView {
  private String name;
  private String userSource;
  private String organizationSource;

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUserSource() {
    return this.userSource;
  }

  public void setUserSource(String userSource) {
    this.userSource = userSource;
  }

  public String getOrganizationSource() {
    return this.organizationSource;
  }

  public void setOrganizationSource(String organizationSource) {
    this.organizationSource = organizationSource;
  }
}
