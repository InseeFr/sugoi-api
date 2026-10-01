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

import fr.insee.sugoi.commons.services.configuration.basic.CustomLdapAuthoritiesPopulator;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
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
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties("fr.insee.sugoi.security")
@EnableMethodSecurity
@Validated
public class SecurityConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);

  @Autowired ApplicationContext applicationContext;

  /** Enable basic authentication */
  private boolean basicAuthenticationEnabled = false;

  /**
   * Enable bearer authentication
   *
   * <p>An oAuth configuration is mandatory if enabled
   *
   * <p>For common Spring Boot configuration, you should add the
   * spring.security.oauth2.resourceserver.jwt.jwk-set-uri property by the public key location of
   * your oAuth server
   *
   * <p>For multi providers, you should add an array of fr.insee.sugoi.security.issuers with
   * fr.insee.sugoi.security.issuers[0].issuer-uri (must be the value of claim "iss") and
   * fr.insee.sugoi.security.issuers[0].jwk-set-uri
   */
  private boolean bearerAuthenticationEnabled = false;

  @Valid private List<JwtIssuerProperties> issuers = new ArrayList<>();

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
      if (issuers != null && !issuers.isEmpty()) {
        // Multi-source : résolution dynamique selon le claim "iss" du jeton
        http.oauth2ResourceServer(
            oauth2 ->
                oauth2.authenticationManagerResolver(jwtIssuerAuthenticationManagerResolver()));
      } else {
        // Comportement historique : une seule source configurée via
        // spring.security.oauth2.resourceserver.jwt.jwk-set-uri
        http.oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(
                    jwtConfigurer ->
                        jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter())));
      }
    }

    String adminRegex =
        applicationContext
            .getEnvironment()
            .getProperty("fr.insee.sugoi.api.regexp.role.admin", "ROLE_ADMIN");
    String[] monitorRoles = (String[]) ArrayUtils.add(adminRegex.split(","), "ROLE_MONITOR");

    http.authorizeHttpRequests(
        configurer -> {
          configurer.requestMatchers(HttpMethod.OPTIONS).permitAll();
          configurer.requestMatchers("/").permitAll();
          configurer.requestMatchers("/actuator/health/**").permitAll();
          configurer.requestMatchers("/swagger-ui/**").permitAll();
          configurer.requestMatchers("/v3/api-docs/**").permitAll();
          configurer.requestMatchers(HttpMethod.GET, "/realms").permitAll();
          configurer.requestMatchers(HttpMethod.GET, "/v2/realms").permitAll();
          configurer.requestMatchers("/actuator/**").hasAnyAuthority(monitorRoles);
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

  private final Map<String, AuthenticationManager> issuerAuthenticationManagers =
      new ConcurrentHashMap<>();

  @PostConstruct
  public void initIssuerAuthenticationManagers() {
    if (issuers != null) {
      issuers.forEach(
          props ->
              issuerAuthenticationManagers.put(
                  props.getIssuerUri(), buildAuthenticationManager(props)));
    }
  }

  private AuthenticationManager buildAuthenticationManager(JwtIssuerProperties props) {
    JwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(props.getJwkSetUri()).build();
    JwtAuthenticationProvider provider = new JwtAuthenticationProvider(decoder);
    provider.setJwtAuthenticationConverter(jwtAuthenticationConverter());
    return provider::authenticate;
  }

  private AuthenticationManagerResolver<HttpServletRequest>
      jwtIssuerAuthenticationManagerResolver() {
    AuthenticationManagerResolver<String> issuerResolver =
        issuerUri -> {
          AuthenticationManager manager = issuerAuthenticationManagers.get(issuerUri);
          if (manager == null) {
            throw new InvalidBearerTokenException("Invalid issuer : " + issuerUri);
          }
          return manager;
        };
    return new JwtIssuerAuthenticationManagerResolver(issuerResolver);
  }

  public static class JwtIssuerProperties {

    @NotBlank private String issuerUri;
    @NotBlank private String jwkSetUri;

    public String getIssuerUri() {
      return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
      this.issuerUri = issuerUri;
    }

    public String getJwkSetUri() {
      return jwkSetUri;
    }

    public void setJwkSetUri(String jwkSetUri) {
      this.jwkSetUri = jwkSetUri;
    }
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

  public List<JwtIssuerProperties> getIssuers() {
    return issuers;
  }

  public void setIssuers(List<JwtIssuerProperties> issuers) {
    this.issuers = issuers;
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
