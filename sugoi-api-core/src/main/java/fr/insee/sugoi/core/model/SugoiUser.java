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

import java.io.Serializable;
import java.util.List;

public class SugoiUser implements Serializable {

  private String name;

  private List<String> roles;

  public SugoiUser() {
  }

  public SugoiUser(String name, List<String> roles) {
    this.name = name;
    this.roles = roles;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getRoles() {
    return this.roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }
}
