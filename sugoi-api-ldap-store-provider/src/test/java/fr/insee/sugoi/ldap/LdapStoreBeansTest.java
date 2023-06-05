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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.model.MappingType;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.model.fixtures.StoreMappingFixture;
import fr.insee.sugoi.model.technics.ModelType;
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
public class LdapStoreBeansTest {

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

  @Value("#{'${fr.insee.sugoi.ldap.default.user-mapping}'.split('$')}")
  private List<String> defaultUserMapping;

  @Value("#{'${fr.insee.sugoi.ldap.default.organization-mapping}'.split('$')}")
  private List<String> defaultOrganizationMapping;

  @Value("#{'${fr.insee.sugoi.ldap.default.group-mapping}'.split('$')}")
  private List<String> defaultGroupMapping;

  @Value("#{'${fr.insee.sugoi.ldap.default.application-mapping}'.split('$')}")
  private List<String> defaultApplicationMapping;

  @Bean
  public UserStorage userStorage() {
    UserStorage us = new UserStorage();
    us.setOrganizationSource(organizationSource);
    us.setUserSource(userSource);
    us.setAddressSource(addressSource);
    us.setName("default");
    us.getProperties().put(LdapConfigKeys.GROUP_FILTER_PATTERN, List.of(groupFilterPattern));
    us.getProperties().put(LdapConfigKeys.GROUP_SOURCE_PATTERN, List.of(groupSourcePattern));

    us.setUserMappings(StoreMappingFixture.getUserStoreMappings());
    return us;
  }

  @Bean(name = "Realm")
  public Realm realm() {
    Realm realm = new Realm();
    realm.setName("domaine1");
    realm.setUrl("localhost");
    realm.setPort("390");
    realm.setAppSource(appSource);

    realm.setGroupMappings(StoreMappingFixture.getGroupStoreMappings());

    return realm;
  }

  @Autowired LdapStoreBeans ldapStoreBeans;

  @Test
  public void beanShouldHaveItsOwnMappingTest() {
    Map<MappingType, List<StoreMapping>> mappings =
        ldapStoreBeans.getCompleteMapping(realm(), userStorage());
    assertThat(
        "Should have the mappings that have been explicitly set in the realm",
        mappings.get(MappingType.GROUPMAPPING).stream()
            .anyMatch(v -> v.equals(new StoreMapping("name", "cn", ModelType.STRING, true))));
    assertThat(
        "Should have the mappings that have been explicitly set in the realm",
        mappings.get(MappingType.GROUPMAPPING).stream()
            .anyMatch(
                v ->
                    v.equals(
                        new StoreMapping("users", "uniquemember", ModelType.LIST_USER, true))));
    assertThat(
        "Should have the mappings that have been explicitly set in the us",
        mappings.get(MappingType.USERMAPPING).stream()
            .anyMatch(
                v -> v.equals(new StoreMapping("firstName", "givenname", ModelType.STRING, true))));
    assertThat(
        "Should have the mappings that have been explicitly set in the us",
        mappings.get(MappingType.USERMAPPING).stream()
            .anyMatch(
                v ->
                    v.equals(
                        new StoreMapping(
                            "attributes.insee_roles_applicatifs",
                            "inseeRoleApplicatif",
                            ModelType.LIST_STRING,
                            true))));
  }

  @Test
  public void beanShouldHaveDefaultMappingTest() {
    Map<MappingType, List<StoreMapping>> mappings =
        ldapStoreBeans.getCompleteMapping(realm(), userStorage());
    assertThat(
        "Should have the default mappings in the us when not set",
        mappings.get(MappingType.ORGANIZATIONMAPPING).stream()
            .anyMatch(
                v ->
                    v.equals(new StoreMapping("attributes.mail", "mail", ModelType.STRING, true))));

    assertThat(
        "Should have the default mappings in the us when not set",
        mappings.get(MappingType.ORGANIZATIONMAPPING).stream()
            .anyMatch(
                v ->
                    v.equals(
                        new StoreMapping(
                            "address", "inseeAdressePostaleDN", ModelType.ADDRESS, true))));

    assertThat(
        "Should have the default mappings in the realm when not set",
        mappings.get(MappingType.APPLICATIONMAPPING).stream()
            .anyMatch(v -> v.equals(new StoreMapping("name", "ou", ModelType.STRING, true))));
  }

  @Test
  public void beanShouldHaveItsOwnPort() {
    assertThat(
        "Port should be realm configured port 390",
        ldapStoreBeans.generateConfig(realm(), userStorage()).get(LdapConfigKeys.PORT),
        is("390"));
  }
}
