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
package fr.insee.sugoi.ldap.utils;

import java.util.ArrayList;
import java.util.List;

public enum TypeRecherche {
  ET("et"),
  OU("ou");

  private String typeRechercheLitteral;

  private TypeRecherche(String typeRecherche) {
    this.typeRechercheLitteral = typeRecherche;
  }

  public String getTypeRecherche() {
    return typeRechercheLitteral;
  }

  /**
   * Vérifie si typeRecherche correspond à OU ou ET.
   *
   * @param typeRechercheEntre chaine de caractère à tester
   * @return tru si typeRechercheEntre est assimilable à OU ou ET
   */
  public boolean estTypeRecherche(String typeRechercheEntre) {
    for (String typeRecherche : listerTypeRecherche()) {
      if (typeRechercheEntre.toLowerCase().equals(typeRecherche)) {
        return true;
      }
    }
    return false;
  }

  private static List<String> listerTypeRecherche() {
    List<String> typesRecherche = new ArrayList<String>();
    for (TypeRecherche typeRecherche : TypeRecherche.values()) {
      typesRecherche.add(typeRecherche.getTypeRecherche());
    }
    return typesRecherche;
  }
}
