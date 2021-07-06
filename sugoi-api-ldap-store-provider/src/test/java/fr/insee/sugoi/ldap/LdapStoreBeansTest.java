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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.store.ldap.LdapStoreBeans;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
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
    us.addProperty("group_filter_pattern", groupFilterPattern);
    us.addProperty("group_source_pattern", groupSourcePattern);
    Map<String, Map<String, String>> mappings = new HashMap<>();

    Map<String, String> userMapping = new HashMap<>();
    userMapping.put("username", "uid,String,rw");
    userMapping.put("lastName", "sn,String,rw");
    userMapping.put("mail", "mail,String,rw");
    userMapping.put("firstName", "givenname,String,rw");
    userMapping.put("attributes.common_name", "cn,String,rw");
    userMapping.put("attributes.personal_title", "personalTitle,String,rw");
    userMapping.put("attributes.description", "description,String,rw");
    userMapping.put("attributes.phone_number", "telephoneNumber,String,rw");
    userMapping.put("habilitations", "inseeGroupeDefaut,list_habilitation,rw");
    userMapping.put("organization", "inseeOrganisationDN,organization,rw");
    userMapping.put("address", "inseeAdressePostaleDN,address,rw");
    userMapping.put("groups", "memberOf,list_group,ro");
    userMapping.put("attributes.insee_roles_applicatifs", "inseeRoleApplicatif,list_string,rw");
    userMapping.put("attributes.common_name", "cn,String,rw");
    userMapping.put("attributes.additionalMail", "inseeMailCorrespondant,String,rw");

    mappings.put("userMapping", userMapping);

    us.setMappings(mappings);
    return us;
  }

  @Bean(name = "Realm")
  public Realm realm() {
    Realm realm = new Realm();
    realm.setName("domaine1");
    realm.setUrl("localhost");
    realm.setAppSource(appSource);

    Map<String, Map<String, String>> mappings = new HashMap<>();
    Map<String, String> groupMapping = new HashMap<>();
    groupMapping.put("name", "cn,String,rw");
    groupMapping.put("description", "description,String,rw");
    groupMapping.put("users", "uniquemember,list_user,rw");
    mappings.put("groupMapping", groupMapping);
    realm.setMappings(mappings);

    return realm;
  }

  @Autowired ApplicationContext context;
  @Autowired LdapStoreBeans ldapStoreBeans;

  @Test
  public void beanShouldHaveItsOwnMappingTest() {
    Map<String, Map<String, String>> mappings =
        ldapStoreBeans.getCompleteMapping(realm(), userStorage());
    assertThat(
        "Should have the mappings that have been explicitly set in the realm",
        mappings.get("groupMapping").get("name"),
        is("cn,String,rw"));
    assertThat(
        "Should have the mappings that have been explicitly set in the realm",
        mappings.get("groupMapping").get("users"),
        is("uniquemember,list_user,rw"));
    assertThat(
        "Should have the mappings that have been explicitly set in the us",
        mappings.get("userMapping").get("firstName"),
        is("givenname,String,rw"));
    assertThat(
        "Should have the mappings that have been explicitly set in the us",
        mappings.get("userMapping").get("attributes.insee_roles_applicatifs"),
        is("inseeRoleApplicatif,list_string,rw"));
  }

  @Test
  public void beanShouldHaveDefaultMappingTest() {
    Map<String, Map<String, String>> mappings =
        ldapStoreBeans.getCompleteMapping(realm(), userStorage());
    assertThat(
        "Should have the default mappings in the us when not set",
        mappings.get("organizationMapping").get("attributes.mail"),
        is("mail,String,rw"));
    assertThat(
        "Should have the default mappings in the us when not set",
        mappings.get("organizationMapping").get("address"),
        is("inseeAdressePostaleDN,address,rw"));
    assertThat(
        "Should have the default mappings in the realm when not set",
        mappings.get("applicationMapping").get("name"),
        is("ou,String,rw"));
  }
}
