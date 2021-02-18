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
package fr.insee.sugoi.converter.ouganext;

import com.fasterxml.jackson.annotation.JsonAlias;
import fr.insee.sugoi.converter.utils.MapFromAttribute;

public class Groupe {

  @JsonAlias({"application", "Application"})
  @MapFromAttribute(attributeName = "appName")
  private String application;

  @JsonAlias({"nom", "Nom"})
  @MapFromAttribute(attributeName = "name")
  private String nom;

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public String getNom() {
    return nom;
  }

  public void setNom(String nom) {
    this.nom = nom;
  }
}
