package fr.insee.sugoi.services.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.services.view.RealmView;
import fr.insee.sugoi.services.view.UserStorageView;

@Component
public class RealmViewMapper {

    public RealmView mapperHandly(Realm realm) {
        final RealmView realmView = new RealmView();
        realmView.setName(realm.getName());
        realmView.setUrl(realm.getUrl());
        realmView.setAppBranch(realm.getAppBranch());
        List<UserStorageView> userStorageViews = new ArrayList<>();
        for (UserStorage userStorage : realm.getUserStorages()) {
            UserStorageView userStorageView = new UserStorageView();
            userStorageView.setName(userStorage.getName());
            userStorageView.setOrganizationBranch(userStorage.getOrganizationBranch());
            userStorageView.setUserBranch(userStorage.getUserBranch());
            userStorageViews.add(userStorageView);
        }
        realmView.setUserStorages(userStorageViews);
        return realmView;
    }

}
