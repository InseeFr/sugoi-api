package fr.insee.sugoi.services.configuration;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
@ConfigurationProperties("fr.insee.sugoi.security")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  /**
   * Enable basic authentication
   */
  private boolean basicAuthenticationEnabled = true;

  /**
   * Enable bearer authentication
   */
  private boolean bearerAuthenticationEnabled = true;

  /**
   * Enable basic authentication over ldap connection
   */
  private boolean ldapAccountManagmentEnabled = true;

  /**
   * Enable test user user:password for development
   */
  private boolean testUserEnabled = true;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // api, so csrf is disabled
    http.csrf().disable();
    // allow basic authentication
    if (basicAuthenticationEnabled) {
      http.httpBasic();
    }
    // allow jwt bearer authentication
    if (bearerAuthenticationEnabled) {
      http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> {
        jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter());
      }));
    }
    // security constraints
    http.authorizeRequests(authz -> authz.antMatchers(HttpMethod.OPTIONS).permitAll().antMatchers(HttpMethod.GET, "/")
        .permitAll().antMatchers("/**").authenticated().anyRequest().denyAll());
  }

  // Customization to get Keycloak Role

  JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
    return jwtAuthenticationConverter;
  }

  Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
    return new Converter<Jwt, Collection<GrantedAuthority>>() {
      @Override
      @SuppressWarnings({ "unchecked", "serial" })
      public Collection<GrantedAuthority> convert(Jwt source) {
        return ((Map<String, List<String>>) source.getClaim("realm_access")).get("roles").stream()
            .map(s -> new GrantedAuthority() {
              @Override
              public String getAuthority() {
                return "ROLE_" + s;
              }

              @Override
              public String toString() {
                return getAuthority();
              }
            }).collect(Collectors.toList());
      }
    };
  }

  // User de test en basic pour voir un accès concurrent basic/bearer

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    if (ldapAccountManagmentEnabled) {
      // TODO
    }
    if (testUserEnabled) {
      auth.inMemoryAuthentication().withUser("user").password("{noop}password").roles("USER");
    }
  }

  public boolean isBasicAuthenticationEnabled() {
    return basicAuthenticationEnabled;
  }

  public void setBasicAuthenticationEnabled(boolean basicAuthenticationEnabled) {
    this.basicAuthenticationEnabled = basicAuthenticationEnabled;
  }

  public boolean isBearerAuthenticationEnabled() {
    return bearerAuthenticationEnabled;
  }

  public void setBearerAuthenticationEnabled(boolean bearerAuthenticationEnabled) {
    this.bearerAuthenticationEnabled = bearerAuthenticationEnabled;
  }

  public boolean isLdapAccountManagmentEnabled() {
    return ldapAccountManagmentEnabled;
  }

  public void setLdapAccountManagmentEnabled(boolean ldapAccountManagmentEnabled) {
    this.ldapAccountManagmentEnabled = ldapAccountManagmentEnabled;
  }

  public boolean isTestUserEnabled() {
    return testUserEnabled;
  }

  public void setTestUserEnabled(boolean testUserEnabled) {
    this.testUserEnabled = testUserEnabled;
  }

}