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
import fr.insee.sugoi.model.RealmConfigKeys;
import fr.insee.sugoi.model.technics.StoreMapping;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class LdapMapper<ResultType> {

  protected Map<RealmConfigKeys, String> config;
  protected List<String> objectClasses;
  protected List<StoreMapping> mappings;

  public abstract ResultType mapFromAttributes(Collection<Attribute> attributes);

  public List<Attribute> mapToAttributesForCreation(ResultType object) {
    return GenericLdapMapper.mapObjectToLdapAttributesForCreation(
        object, config, mappings, objectClasses);
  }

  public List<Modification> createMods(ResultType object) {
    return GenericLdapMapper.createMods(object, config, mappings);
  }

  public List<Attribute> createAttributesForFilter(ResultType object) {
    return GenericLdapMapper.mapObjectToLdapAttributesForFilter(
        object, config, mappings, objectClasses);
  }
}
