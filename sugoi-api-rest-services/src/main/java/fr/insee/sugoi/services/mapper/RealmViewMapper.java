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
package fr.insee.sugoi.services.mapper;

import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.services.view.RealmView;
import fr.insee.sugoi.services.view.UserStorageView;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RealmViewMapper {

  public RealmView mapperHandly(Realm realm) {
    final RealmView realmView = new RealmView();
    realmView.setName(realm.getName());
    realmView.setUrl(realm.getUrl());
    realmView.setAppSource(realm.getAppSource());
    List<UserStorageView> userStorageViews = new ArrayList<>();
    for (UserStorage userStorage : realm.getUserStorages()) {
      UserStorageView userStorageView = new UserStorageView();
      userStorageView.setName(userStorage.getName());
      userStorageView.setOrganizationSource(userStorage.getOrganizationSource());
      userStorageView.setUserSource(userStorage.getUserSource());
      userStorageViews.add(userStorageView);
    }
    realmView.setUserStorages(userStorageViews);
    return realmView;
  }
}
