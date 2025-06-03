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
package fr.insee.sugoi.commons.services.configuration;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import fr.insee.sugoi.commons.services.configuration.basic.CustomLdapAuthoritiesPopulator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConfigurationProperties("fr.insee.sugoi.security")
@EnableMethodSecurity
public class SecurityConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);

  @Autowired ApplicationContext applicationContext;

  /** Enable basic authentication */
  private boolean basicAuthenticationEnabled = false;

  /**
   * Enable bearer authentication
   *
   * <p>A Spring Security oAuth configuration is mandatory if enabled
   *
   * <p>For instance you should add the spring.security.oauth2.resourceserver.jwt.jwk-set-uri
   * property by the public key location of your oAuth server
   */
  private boolean bearerAuthenticationEnabled = false;

  /** Ldap url where are stored accounts for managment */
  private String ldapAccountManagmentUrl;
  /** Base DN where are stored ldap accounts for managment */
  private String ldapAccountManagmentUserBase;
  /** Group DN where are stored permissions for ldap accounts for managment */
  private String ldapAccountManagmentGroupeBase;
  /** Search in subtree * */
  private boolean ldapAccountManagmentGroupSubtree;

  private String oidcClaimUsername = "sub";
  /** Path to the role field in token. For instance realm_access.role */
  private String oidcClaimRole = "realm_access.roles";

  @Value("${fr.insee.sugoi.security.default-roles-for-users:}")
  private List<String> defaultRolesForUsers;

  private String ldapAccountManagementBindDn = null;

  private String ldapAccountManagementPassword;

  private boolean ldapAccountManagementPooled = false;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // api, so csrf is disabled
    http.csrf(csrf -> csrf.disable());

    // allow basic authentication
    if (basicAuthenticationEnabled) {
      http.httpBasic(Customizer.withDefaults());
    }
    // allow jwt bearer authentication
    if (bearerAuthenticationEnabled) {
      http.oauth2ResourceServer(
          oauth2 ->
              oauth2.jwt(
                  jwtConfigurer -> {
                    jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter());
                  }));
    }

    String adminRegex =
        applicationContext
            .getEnvironment()
            .getProperty("fr.insee.sugoi.api.regexp.role.admin", "ROLE_ADMIN");
    String[] monitorRoles = (String[]) ArrayUtils.add(adminRegex.split(","), "ROLE_MONITOR");

    http.authorizeHttpRequests(
        configurer -> {
          configurer.requestMatchers(antMatcher(HttpMethod.OPTIONS)).permitAll();
          configurer.requestMatchers(antMatcher("/")).permitAll();
          configurer.requestMatchers(antMatcher("/actuator/health/**")).permitAll();
          configurer.requestMatchers(antMatcher("/swagger-ui/**")).permitAll();
          configurer.requestMatchers(antMatcher("/v3/api-docs/**")).permitAll();
          configurer.requestMatchers(antMatcher(HttpMethod.GET, "/realms")).permitAll();
          configurer.requestMatchers(antMatcher(HttpMethod.GET, "/v2/realms")).permitAll();
          configurer.requestMatchers(antMatcher("/actuator/**")).hasAnyAuthority(monitorRoles);
          configurer.anyRequest().authenticated();
        });
    return http.build();
  }

  // Customization to get Keycloak Role

  JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setPrincipalClaimName(oidcClaimUsername);
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
    return jwtAuthenticationConverter;
  }

  Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
    return new Converter<Jwt, Collection<GrantedAuthority>>() {
      @Override
      @SuppressWarnings({"unchecked"})
      public Collection<GrantedAuthority> convert(Jwt source) {

        String[] claimPath = oidcClaimRole.split("\\.");
        Map<String, Object> claims = source.getClaims();
        try {

          for (int i = 0; i < claimPath.length - 1; i++) {
            claims = (Map<String, Object>) claims.get(claimPath[i]);
          }

          List<String> roles =
              (List<String>)
                  claims.getOrDefault(claimPath[claimPath.length - 1], new ArrayList<>());
          roles.addAll(defaultRolesForUsers);
          return roles.stream()
              .map(
                  s ->
                      new GrantedAuthority() {
                        @Override
                        public String getAuthority() {
                          return "ROLE_" + s;
                        }

                        @Override
                        public String toString() {
                          return getAuthority();
                        }
                      })
              .collect(Collectors.toList());
        } catch (ClassCastException e) {
          // role path not correctly found, assume that no role for this user
          return new ArrayList<>();
        }
      }
    };
  }

  @Bean
  @ConditionalOnProperty(
      value = "fr.insee.sugoi.security.ldap-account-managment-enabled",
      havingValue = "true")
  LdapContextSource contextSourceFactoryBean() {
    LdapContextSource ldapContextSource = new LdapContextSource();
    ldapContextSource.setUrl(ldapAccountManagmentUrl);
    ldapContextSource.setUserDn(ldapAccountManagementBindDn);
    ldapContextSource.setPassword(ldapAccountManagementPassword);
    ldapContextSource.setPooled(ldapAccountManagementPooled);
    return ldapContextSource;
  }

  @Bean
  @ConditionalOnProperty(
      value = "fr.insee.sugoi.security.ldap-account-managment-enabled",
      havingValue = "true")
  LdapAuthoritiesPopulator authorities(BaseLdapPathContextSource contextSource) {
    DefaultLdapAuthoritiesPopulator authorities =
        new CustomLdapAuthoritiesPopulator(
            contextSource, ldapAccountManagmentGroupeBase, defaultRolesForUsers);
    authorities.setGroupSearchFilter("(uniquemember={0})");
    authorities.setSearchSubtree(ldapAccountManagmentGroupSubtree);
    return authorities;
  }

  @Bean
  @ConditionalOnProperty(
      value = "fr.insee.sugoi.security.ldap-account-managment-enabled",
      havingValue = "true")
  AuthenticationManager ldapAuthenticationManager(
      BaseLdapPathContextSource contextSource, LdapAuthoritiesPopulator ldapAuthoritiesPopulator) {
    LdapBindAuthenticationManagerFactory factory =
        new LdapBindAuthenticationManagerFactory(contextSource);
    factory.setUserSearchBase(ldapAccountManagmentUserBase);
    factory.setUserSearchFilter("(uid={0})");
    factory.setLdapAuthoritiesPopulator(ldapAuthoritiesPopulator);
    return factory.createAuthenticationManager();
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

  public String getLdapAccountManagmentUrl() {
    return ldapAccountManagmentUrl;
  }

  public void setLdapAccountManagmentUrl(String ldapAccountManagmentUrl) {
    this.ldapAccountManagmentUrl = ldapAccountManagmentUrl;
  }

  public String getLdapAccountManagmentUserBase() {
    return ldapAccountManagmentUserBase;
  }

  public void setLdapAccountManagmentUserBase(String ldapAccountManagmentUserBase) {
    this.ldapAccountManagmentUserBase = ldapAccountManagmentUserBase;
  }

  public String getLdapAccountManagmentGroupeBase() {
    return ldapAccountManagmentGroupeBase;
  }

  public void setLdapAccountManagmentGroupeBase(String ldapAccountManagmentGroupeBase) {
    this.ldapAccountManagmentGroupeBase = ldapAccountManagmentGroupeBase;
  }

  public boolean getLdapAccountManagmentGroupSubtree() {
    return ldapAccountManagmentGroupSubtree;
  }

  public void setLdapAccountManagmentGroupSubtree(boolean ldapAccountManagmentGroupSubtree) {
    this.ldapAccountManagmentGroupSubtree = ldapAccountManagmentGroupSubtree;
  }

  public String getOidcClaimUsername() {
    return this.oidcClaimUsername;
  }

  public void setOidcClaimUsername(String oidcClaimUsername) {
    this.oidcClaimUsername = oidcClaimUsername;
  }

  public String getOidcClaimRole() {
    return oidcClaimRole;
  }

  public void setOidcClaimRole(String oidcClaimRole) {
    this.oidcClaimRole = oidcClaimRole;
  }

  public String getLdapAccountManagementBindDn() {
    return ldapAccountManagementBindDn;
  }

  public void setLdapAccountManagementBindDn(String ldapAccountManagementBindDn) {
    this.ldapAccountManagementBindDn = ldapAccountManagementBindDn;
  }

  public String getLdapAccountManagementPassword() {
    return ldapAccountManagementPassword;
  }

  public void setLdapAccountManagementPassword(String ldapAccountManagementPassword) {
    this.ldapAccountManagementPassword = ldapAccountManagementPassword;
  }

  public boolean isLdapAccountManagementPooled() {
    return ldapAccountManagementPooled;
  }

  public void setLdapAccountManagementPooled(boolean ldapAccountManagementPooled) {
    this.ldapAccountManagementPooled = ldapAccountManagementPooled;
  }
}
