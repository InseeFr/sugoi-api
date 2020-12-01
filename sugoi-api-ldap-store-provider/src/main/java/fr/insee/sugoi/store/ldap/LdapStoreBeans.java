package fr.insee.sugoi.store.ldap;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;

@Configuration
public class LdapStoreBeans {

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
    return null;
  }
}
