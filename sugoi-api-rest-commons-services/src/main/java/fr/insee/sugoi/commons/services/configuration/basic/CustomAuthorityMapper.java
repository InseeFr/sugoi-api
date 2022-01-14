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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.util.Assert;

public final class CustomAuthorityMapper implements GrantedAuthoritiesMapper {

  private String prefix = "ROLE_";

  private List<String> defaultRoles = new ArrayList<>();

  public CustomAuthorityMapper(List<String> defaultRolesToAdd) {
    this.defaultRoles = defaultRolesToAdd;
  }

  /**
   * Creates a mapping of the supplied authorities based on the case-conversion and prefix settings.
   * The mapping will be one-to-one unless duplicates are produced during the conversion. If a
   * default authority has been set, this will also be assigned to each mapping.
   *
   * @param authorities the original authorities
   * @return the converted set of authorities
   */
  @Override
  public Set<GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
    HashSet<GrantedAuthority> mapped = new HashSet<>(authorities.size());
    for (GrantedAuthority authority : authorities) {
      mapped.add(mapAuthority(authority.getAuthority()));
    }

    for (String role : this.defaultRoles) {
      mapped.add(mapAuthority(role));
    }

    return mapped;
  }

  private GrantedAuthority mapAuthority(String name) {
    if (this.prefix.length() > 0 && !name.startsWith(this.prefix)) {
      name = this.prefix + name;
    }
    return new SimpleGrantedAuthority(name);
  }

  /**
   * Sets the prefix which should be added to the authority name (if it doesn't already exist)
   *
   * @param prefix the prefix, typically to satisfy the behaviour of an {@code AccessDecisionVoter}.
   */
  public void setPrefix(String prefix) {
    Assert.notNull(prefix, "prefix cannot be null");
    this.prefix = prefix;
  }
}
