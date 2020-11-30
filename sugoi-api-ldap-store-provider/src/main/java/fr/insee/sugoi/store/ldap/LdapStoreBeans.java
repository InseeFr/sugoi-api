package fr.insee.sugoi.store.ldap;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class LdapStoreBeans {

  @Bean
  @Lazy
  public LdapReaderStore ldapReaderStore(Map<String, String> config) {
    return new LdapReaderStore(config);
  }

  @Bean
  @Lazy
  public LdapWriterStore ldapWriterStore(Map<String, String> config) {
    return new LdapWriterStore(config);
  }
}
