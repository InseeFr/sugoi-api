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
package fr.insee.sugoi.ldap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.model.MappingType;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.model.technics.StoreMapping;
import fr.insee.sugoi.store.ldap.LdapStoreBeans;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

@SpringBootTest(classes = LdapStoreBeans.class)
public class LdapStoreBeansWithDefaultTest {

  @Value("${fr.insee.sugoi.ldap.default.organizationsource:}")
  private String organizationSource;

  @Value("${fr.insee.sugoi.ldap.default.appsource:}")
  private String appSource;

  @Value("${fr.insee.sugoi.ldap.default.usersource:}")
  private String userSource;

  @Value("${fr.insee.sugoi.ldap.default.addresssource:}")
  private String addressSource;

  @Value("${fr.insee.sugoi.ldap.default.groupsourcepattern:}")
  private String groupSourcePattern;

  @Value("${fr.insee.sugoi.ldap.default.groupfilterpattern:}")
  private String groupFilterPattern;

  @Bean
  public UserStorage userStorage() {
    UserStorage us = new UserStorage();
    us.setOrganizationSource(organizationSource);
    us.setUserSource(userSource);
    us.setAddressSource(addressSource);
    us.setName("default");
    us.getProperties().put(LdapConfigKeys.GROUP_FILTER_PATTERN, List.of(groupFilterPattern));
    us.getProperties().put(LdapConfigKeys.GROUP_SOURCE_PATTERN, List.of(groupSourcePattern));
    return us;
  }

  @Bean(name = "Realm")
  public Realm realm() {
    Realm realm = new Realm();
    realm.setName("domaine1");
    realm.setUrl("localhost");
    realm.setPort("390");
    realm.setAppSource(appSource);
    return realm;
  }

  @Autowired LdapStoreBeans ldapStoreBeans;

  @Test
  public void testDefaultUserMapping() {
    Map<MappingType, List<StoreMapping>> mappings =
        ldapStoreBeans.getCompleteMapping(realm(), userStorage());
    assertNotNull(mappings.get(MappingType.USERMAPPING));
    assertTrue(!mappings.get(MappingType.USERMAPPING).isEmpty());
  }

  @Test
  public void testDefaultOrganizationMapping() {
    Map<MappingType, List<StoreMapping>> mappings =
        ldapStoreBeans.getCompleteMapping(realm(), userStorage());
    assertNotNull(mappings.get(MappingType.ORGANIZATIONMAPPING));
    assertTrue(!mappings.get(MappingType.ORGANIZATIONMAPPING).isEmpty());
  }

  @Test
  public void testDefaultGroupMapping() {
    Map<MappingType, List<StoreMapping>> mappings =
        ldapStoreBeans.getCompleteMapping(realm(), userStorage());
    assertNotNull(mappings.get(MappingType.GROUPMAPPING));
    assertTrue(!mappings.get(MappingType.GROUPMAPPING).isEmpty());
  }
}
