package fr.insee.sugoi.store.ldap;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;

@Configuration
public class LdapStoreBeans {

  @Value("${fr.insee.sugoi.ldap.default.username:}")
  private String defaultUsername;

  @Value("${fr.insee.sugoi.ldap.default.password:}")
  private String defaultPassword;

  @Value("${fr.insee.sugoi.ldap.default.pool:}")
  private String defaultPoolSize;

  @Bean
  @Lazy
  public LdapReaderStore ldapReaderStore(Realm realm, UserStorage userStorage) {
    return new LdapReaderStore(generateConfig(realm, userStorage));
  }

  @Bean
  @Lazy
  public LdapWriterStore ldapWriterStore(Realm realm, UserStorage userStorage) {
    return new LdapWriterStore(generateConfig(realm, userStorage));
  }

  public Map<String, String> generateConfig(Realm realm, UserStorage userStorage) {
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
