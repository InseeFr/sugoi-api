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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
@ConfigurationProperties("fr.insee.sugoi.security")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

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

  /** Enable basic authentication over ldap connection */
  private boolean ldapAccountManagmentEnabled = false;

  /** Ldap url where are stored accounts for managment */
  private String ldapAccountManagmentUrl;
  /** Base DN where are stored ldap accounts for managment */
  private String ldapAccountManagmentUserBase;
  /** Group DN where are stored permissions for ldap accounts for managment */
  private String ldapAccountManagmentGroupeBase;
  /** Search in subtree * */
  private boolean ldapAccountManagmentGroupSubtree;

  private String oidcClaimUsername = "sub";

  /** Enable a monitoring user */
  private boolean monitorUserEnabled = false;

  private String monitorUserName = "monitor";
  private String monitorUserPassword;

  public String getMonitorUserName() {
    return monitorUserName;
  }

  public void setMonitorUserName(String monitorUserName) {
    this.monitorUserName = monitorUserName;
  }

  public String getMonitorUserPassword() {
    return monitorUserPassword;
  }

  public void setMonitorUserPassword(String monitorUserPassword) {
    this.monitorUserPassword = monitorUserPassword;
  }

  public boolean isMonitorUserEnabled() {
    return monitorUserEnabled;
  }

  public void setMonitorUserEnabled(boolean monitorUserEnabled) {
    this.monitorUserEnabled = monitorUserEnabled;
  }

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
      http.oauth2ResourceServer(
          oauth2 ->
              oauth2.jwt(
                  jwtConfigurer -> {
                    jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter());
                  }));
    }

    String adminRegex =
        getApplicationContext()
            .getEnvironment()
            .getProperty("fr.insee.sugoi.api.regexp.role.admin", "ROLE_ADMIN");
    String[] monitorRoles = (String[]) ArrayUtils.add(adminRegex.split(","), "ROLE_MONITOR");

    http.authorizeRequests(
        authz ->
            authz
                .antMatchers("/actuator/health/**")
                .permitAll()
                .antMatchers("/actuator/**")
                .hasAnyAuthority(monitorRoles)
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**")
                .permitAll()
                .antMatchers(HttpMethod.OPTIONS)
                .permitAll()
                .antMatchers(HttpMethod.GET, "/**")
                .permitAll()
                .antMatchers("/**")
                .authenticated()
                .anyRequest()
                .denyAll());
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
      @SuppressWarnings({"unchecked", "serial"})
      public Collection<GrantedAuthority> convert(Jwt source) {
        return ((Map<String, List<String>>) source.getClaim("realm_access"))
            .get("roles").stream()
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
      }
    };
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    if (monitorUserEnabled && monitorUserPassword != null) {
      auth.inMemoryAuthentication()
          .withUser(monitorUserName)
          .password("{noop}" + monitorUserPassword)
          .roles("MONITOR");
    }
    if (ldapAccountManagmentEnabled) {
      auth.ldapAuthentication()
          .userSearchBase(ldapAccountManagmentUserBase)
          .userSearchFilter("(uid={0})")
          .groupSearchBase(ldapAccountManagmentGroupeBase)
          .groupSearchSubtree(ldapAccountManagmentGroupSubtree)
          .contextSource()
          .url(ldapAccountManagmentUrl);
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
}
