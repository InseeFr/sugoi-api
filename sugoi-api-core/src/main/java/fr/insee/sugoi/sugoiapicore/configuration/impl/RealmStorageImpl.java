package fr.insee.sugoi.sugoiapicore.configuration.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.sugoiapicore.configuration.StoreStorage;
import fr.insee.sugoi.sugoiapicore.utils.Exceptions.RealmNotFoundException;
import fr.insee.sugoi.sugoiapicore.configuration.RealmProviderDAO;
import fr.insee.sugoi.sugoiapicore.configuration.RealmStorage;

@Component
public class RealmStorageImpl implements RealmStorage {

    @Value("${fr.insee.sugoi.ldap.default.username}")
    private String defaultUsername;
    @Value("${fr.insee.sugoi.ldap.default.password}")
    private String defaultPassword;
    @Value("${fr.insee.sugoi.ldap.default.pool}")
    private String defaultPoolSize;

    @Autowired
    private RealmProviderDAO realmProviderDAO;

    @Autowired
    private StoreStorage connectionStorage;

    @Cacheable
    public Realm getRealm(String realmName) {
        try {
            Realm realm = realmProviderDAO.load(realmName);
            for (UserStorage userStorage : realm.getUserStorages()) {
                userStorage.setStore(connectionStorage.getStore(generateConf(realm, userStorage)));
            }
            return realm;
        } catch (RealmNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> generateConf(Realm realm, UserStorage userStorage) {
        Map<String, String> config = new HashMap<>();
        config.put("name", userStorage.getName() != null ? userStorage.getName() : realm.getName());
        config.put("url", realm.getUrl());
        config.put("username", defaultUsername);
        config.put("password", defaultPassword);
        config.put("pool_size", defaultPoolSize);
        config.put("user_branch", userStorage.getUserBranch());
        config.put("app_branch", realm.getAppBranch());
        config.put("organization_branch", userStorage.getOrganizationBranch());
        config.put("realm_name", realm.getName());
        config.put("type", "ldap");
        return config;
    }

    @Override
    public List<Realm> getRealms() {
        List<Realm> realms = realmProviderDAO.findAll();
        return realms;
    }

}
