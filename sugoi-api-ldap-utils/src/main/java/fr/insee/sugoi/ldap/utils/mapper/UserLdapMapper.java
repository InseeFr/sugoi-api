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
package fr.insee.sugoi.ldap.utils.mapper;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Modification;
import fr.insee.sugoi.ldap.utils.mapper.properties.UserLdap;
import fr.insee.sugoi.model.User;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class UserLdapMapper implements LdapMapper<User> {

  Map<String, String> config;

  public UserLdapMapper(Map<String, String> config) {
    this.config = config;
  }

  @Override
  public User mapFromAttributes(Collection<Attribute> attributes) {
    return GenericLdapMapper.mapLdapAttributesToObject(attributes, UserLdap.class, User.class);
  }

  @Override
  public List<Attribute> mapToAttributes(User u) {
    return GenericLdapMapper.mapObjectToLdapAttributes(u, UserLdap.class, User.class, config);
  }

  @Override
  public List<Modification> createMods(User updatedUser) {
    return GenericLdapMapper.createMods(updatedUser, UserLdap.class, User.class, config);
  }
}
