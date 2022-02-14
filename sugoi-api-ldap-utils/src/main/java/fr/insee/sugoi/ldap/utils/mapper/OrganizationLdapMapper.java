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
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.RealmConfigKeys;
import fr.insee.sugoi.model.technics.StoreMapping;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class OrganizationLdapMapper extends LdapMapper<Organization> {

  public OrganizationLdapMapper(Map<RealmConfigKeys, String> config, List<StoreMapping> mappings) {
    this.config = config;
    if (config.get(LdapConfigKeys.ORGANIZATION_OBJECT_CLASSES) != null) {
      objectClasses =
          Arrays.asList(config.get(LdapConfigKeys.ORGANIZATION_OBJECT_CLASSES).split(","));
    }
    this.mappings = mappings;
  }

  @Override
  public Organization mapFromAttributes(Collection<Attribute> attributes) {
    return GenericLdapMapper.mapLdapAttributesToObject(
        attributes, Organization.class, config, mappings);
  }
}
