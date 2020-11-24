package fr.insee.sugoi.core.store.impl;

import fr.insee.sugoi.core.configuration.RealmProvider;
import fr.insee.sugoi.core.configuration.StoreStorage;
import fr.insee.sugoi.core.store.Store;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StoreProviderImpl implements StoreProvider {

  @Autowired private RealmProvider realmProvider;
  @Autowired private StoreStorage connectionStorage;

  @Value("${fr.insee.sugoi.ldap.default.username}")
  private String defaultUsername;

  @Value("${fr.insee.sugoi.ldap.default.password}")
  private String defaultPassword;

  @Value("${fr.insee.sugoi.ldap.default.pool}")
  private String defaultPoolSize;

  @Override
  public Store getStoreForUserStorage(String realmName, String userStorageName) {
    Realm r = realmProvider.load(realmName);
    UserStorage us = realmProvider.loadUserStorageByUserStorageName(realmName, userStorageName);
    return connectionStorage.getStore(us.getReaderType(), us.getWriterType(), generateConf(r, us));
  }

  private Map<String, String> generateConf(Realm realm, UserStorage userStorage) {
    Map<String, String> config = new HashMap<>();
    config.put("name", userStorage.getName() != null ? userStorage.getName() : realm.getName());
    config.put("url", realm.getUrl());
    config.put("username", defaultUsername);
    config.put("password", defaultPassword);
    config.put("pool_size", defaultPoolSize);
    config.put("user_source", userStorage.getUserSource());
    config.put("app_source", realm.getAppSource());
    config.put("organization_source", userStorage.getOrganizationSource());
    config.put("realm_name", realm.getName());
    config.put("type", "ldap");
    return config;
  }
}
