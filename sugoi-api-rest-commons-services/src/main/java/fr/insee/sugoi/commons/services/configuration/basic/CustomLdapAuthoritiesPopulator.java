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
package fr.insee.sugoi.commons.services.configuration.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

public class CustomLdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator {

  List<String> defaultRoles = new ArrayList<>();

  public CustomLdapAuthoritiesPopulator(
      ContextSource contextSource, String groupSearchBase, List<String> defaultRoles) {
    super(contextSource, groupSearchBase);
    this.defaultRoles = defaultRoles;
  }

  @Override
  protected Set<GrantedAuthority> getAdditionalRoles(DirContextOperations user, String username) {
    return defaultRoles.stream()
        .map((role) -> new SimpleGrantedAuthority("ROLE_" + role))
        .collect(Collectors.toSet());
  }
}
