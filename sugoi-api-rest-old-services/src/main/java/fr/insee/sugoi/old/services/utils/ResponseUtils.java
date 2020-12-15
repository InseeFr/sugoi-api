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
package fr.insee.sugoi.old.services.utils;

import fr.insee.sugoi.converter.ouganext.Profil;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import java.util.ArrayList;
import java.util.List;

public class ResponseUtils {

  public static List<Profil> convertRealmToProfils(Realm realm) {
    List<Profil> profils = new ArrayList<>();
    for (UserStorage userStorage : realm.getUserStorages()) {
      Profil profil = new Profil();
      profil.setBrancheOrganisation(realm.getAppSource());
      profil.setLdapUrl(realm.getUrl());
      profil.setNomProfil(userStorage.getName());
      profils.add(profil);
    }
    return profils;
  }
}
