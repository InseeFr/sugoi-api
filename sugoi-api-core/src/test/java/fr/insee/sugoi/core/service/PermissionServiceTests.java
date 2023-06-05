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
package fr.insee.sugoi.core.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.eq;

import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.core.model.SugoiUser;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.impl.PermissionServiceImpl;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.exceptions.ApplicationNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(
    classes = PermissionServiceImpl.class,
    properties = "spring.config.location=classpath:/permissions/test-regexp-permissions.properties")
public class PermissionServiceTests {

  @MockBean private RealmProvider realmProvider;
  @MockBean private ApplicationService applicationService;
  @MockBean private GroupService groupService;
  @Autowired PermissionService permissions;

  private Realm realm;
  private Application selfManagedGroupsApplication = new Application();
  private Application notSelfManagedGroupsApplication = new Application();

  @BeforeEach
  public void setup() {
    realm = new Realm();
    realm.setName("test");
    realm
            .getProperties()
            .put(GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_PATTERNS_LIST, List.of("(.*)_$(application)"));
    realm
            .getProperties()
            .put(GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_KEYS_LIST, List.of("my-attribute-key"));

    selfManagedGroupsApplication.setName("selfmanaged");
    selfManagedGroupsApplication.setIsSelfManagedGroupsApp(true);
    Group selfManagedGroup = new Group("selfManagedGroup");
    selfManagedGroup.setUsers(List.of(new User("user2")));
    selfManagedGroup.setIsSelfManaged(true);
    Mockito.when(
            groupService.findById(
                Mockito.eq("test"), Mockito.any(), Mockito.matches("selfManagedGroup.*")))
        .thenReturn(selfManagedGroup);
    Group notSelfManagedGroup = new Group("notSelfManagedGroup");
    notSelfManagedGroup.setUsers(List.of(new User("user3")));
    notSelfManagedGroup.setIsSelfManaged(false);
    Mockito.when(
            groupService.findById(
                Mockito.eq("test"), Mockito.any(), Mockito.matches("notSelfManagedGroup.*")))
        .thenReturn(notSelfManagedGroup);
    Group isMember = new Group("ismember");
    Group isNotMember = new Group("isnotmember");
    selfManagedGroupsApplication.setGroups(
        List.of(isMember, isNotMember, selfManagedGroup, notSelfManagedGroup));
    Mockito.when(
            groupService.findById(Mockito.eq("test"), Mockito.any(), Mockito.matches("ismember.*")))
        .thenReturn(isMember);
    Mockito.when(
            groupService.findById(
                Mockito.eq("test"), Mockito.any(), Mockito.matches("isnotmember.*")))
        .thenReturn(isNotMember);
    notSelfManagedGroupsApplication.setName("notselfmanaged");
    notSelfManagedGroupsApplication.setIsSelfManagedGroupsApp(false);
    Group group = new Group("group");
    group.setUsers(List.of(new User("user")));
    Mockito.when(groupService.findById(Mockito.eq("test"), Mockito.any(), Mockito.eq("group")))
        .thenReturn(group);
    notSelfManagedGroupsApplication.setGroups(
        List.of(group, selfManagedGroup, notSelfManagedGroup));
    Mockito.when(applicationService.findById("test", "selfmanaged"))
        .thenReturn(selfManagedGroupsApplication);
    Mockito.when(applicationService.findById("test", "notselfmanaged"))
        .thenReturn(notSelfManagedGroupsApplication);
    Mockito.when(
            applicationService.findById(
                eq("test"), and(not(eq("notselfmanaged")), not(eq("selfmanaged")))))
        .thenThrow(ApplicationNotFoundException.class);
  }

  @Test
  public void testAdminRegexp() {
    SugoiUser sugoiUser = new SugoiUser("admin", List.of("role_Admin_Sugoi"));
    assertThat("User is admin", permissions.isAdmin(sugoiUser), is(true));
  }

  @Test
  public void testReaderRegexp() {
    SugoiUser sugoiUser = new SugoiUser("reader_realm1", List.of("role_Reader_realm1_sugoi"));

    assertThat("User can read realm1", permissions.isReader(sugoiUser, "realm1", ""), is(true));
    assertThat(
        "User cannot write realm1", permissions.isWriter(sugoiUser, "realm1", ""), is(false));
    assertThat("User cannot read realm2", permissions.isReader(sugoiUser, "realm2", ""), is(false));
  }

  @Test
  public void testPasswordValidatorRegexp() {
    SugoiUser sugoiUser =
        new SugoiUser("password_validator_realm1", List.of("role_PASSWORD_VALIDATOR_realm1_us1"));

    assertThat(
        "User can be password validator on realm1",
        permissions.isPasswordValidator(sugoiUser, "realm1", "us1"),
        is(true));
    assertThat(
        "User cannot be password validator on realm2",
        permissions.isReader(sugoiUser, "realm2", "us1"),
        is(false));
  }

  @Test
  public void testWriterRegexp() {
    SugoiUser sugoiUser = new SugoiUser("writer_realm1", List.of("role_Writer_realm1_sugoi"));
    assertThat("User can read realm1", permissions.isReader(sugoiUser, "realm1", ""), is(true));
    assertThat("User can write realm1", permissions.isWriter(sugoiUser, "realm1", ""), is(true));
    assertThat("User cannot read realm2", permissions.isReader(sugoiUser, "realm2", ""), is(false));
  }

  @Test
  public void testUserStorageNull() {
    try {
      SugoiUser sugoiUser = new SugoiUser("writer_realm1", List.of("role_Writer_realm1_sugoi"));
      assertThat(
          "No nullPointerException", permissions.isReader(sugoiUser, "realm1", null), is(true));
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void testAdminRealm() {
    SugoiUser sugoiUser = new SugoiUser("admin_realm1", List.of("ROLE_SUGOI_realm1_ADMIN"));
    assertThat(
        "user is admin realm for realm1",
        permissions.isAdminRealm(sugoiUser, "realm1", null),
        is(true));
  }

  @Test
  public void testAdminRealm2() {
    SugoiUser sugoiUser = new SugoiUser("admin_realm1", List.of("ROLE_SUGOI_realm1_ADMIN"));
    assertThat(
        "user is not admin realm for realm2",
        permissions.isAdminRealm(sugoiUser, "realm2", null),
        is(false));
  }

  @Test
  public void testAppManager() {
    SugoiUser sugoiUser =
        new SugoiUser("appmanager_realm1_appli1", List.of("role_Asi_realm1_appli1"));
    assertThat(
        "user is app manager for appli1 in realm1",
        permissions.isApplicationManager(sugoiUser, "realm1", "appli1"),
        is(true));
    assertThat(
        "user is reader in realm1", permissions.isReader(sugoiUser, "realm1", null), is(true));
  }

  @Test
  public void testAnyAppManager() {
    SugoiUser sugoiUser =
        new SugoiUser(
            "appmanager_realm1", List.of("role_Asi_realm1_appli1", "role_reader_realm1_sugoi"));
    assertThat(
        "user is app manager for at least one application",
        permissions.isAtLeastOneApplicationManager(sugoiUser, "realm1"),
        is(true));
  }

  @Test
  public void testAppManagerWithoutRealmInRoleName() {
    SugoiUser sugoiUser = new SugoiUser("appmanager_appli1", List.of("role_Asi_realm1_appli1"));
    assertThat(
        "user is app manager for appli1 in realm1",
        permissions.isApplicationManager(sugoiUser, "realm1", "appli1"),
        is(true));
    assertThat(
        "user is reader in realm1", permissions.isReader(sugoiUser, "realm1", null), is(true));
    assertThat(
        "user is app manager for appli1 in realm2",
        permissions.isApplicationManager(sugoiUser, "realm2", "appli1"),
        is(false));
  }

  @Test
  public void testAppManagerWithoutRealmInRoleName2() {
    SugoiUser sugoiUser = new SugoiUser("appmanager_appli1", List.of("role_Asi_realm1_appli1"));
    assertThat(
        "user is reader in realm2", permissions.isReader(sugoiUser, "realm2", null), is(false));
  }

  @Test
  public void testAdminWildcard() {
    SugoiUser sugoiUser =
        new SugoiUser("admin_wildcard", List.of("role_nimportequoi_adminwildcard"));
    try {
      assertThat("user is admin sugoi", permissions.isAdmin(sugoiUser), is(true));
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void testgetAllowedAttributePattern() {
    try {
      Mockito.when(realmProvider.load(Mockito.any())).thenReturn(Optional.of(realm));
      SugoiUser sugoiUser =
          new SugoiUser("admin_wildcard", List.of("role_ASI_realm1_app1", "role_ASI_REALM_app1"));
      List<String> allowedAttributesPattern =
          permissions.getAllowedAttributePattern(
              sugoiUser,
              "toto",
              "tata",
              realm
                  .getProperties()
                  .get(GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_KEYS_LIST)
                  .get(0)
                  .split(",")[0]);
      assertThat("allowed Attributes Pattern", allowedAttributesPattern.size(), is(2));
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  @DisplayName(
      "Given a self-managed-groups application, "
          + "with a user being in one group but not in the other, "
          + "the user should be able to manage the groups they are in "
          + "but not the group they don't belong to")
  void changingMembersWithSelfManageAppTest() {
    SugoiUser sugoiUser = new SugoiUser("user", List.of("role_ismember_selfmanaged"));
    assertThat(
        "The user should be able to add a member in the group it belongs to",
        true,
        is(
            permissions.isMemberOfSelfManagedGroup(
                sugoiUser, "test", "selfmanaged", "ismember_selfmanaged")));
    assertThat(
        "The user should not be able to add a member in the group it doesn't belong to",
        false,
        is(
            permissions.isMemberOfSelfManagedGroup(
                sugoiUser, "test", "selfmanaged", "isnotmember_selfmanaged")));
    assertThat(
        "If a group does not exist, the user is not member",
        false,
        is(
            permissions.isMemberOfSelfManagedGroup(
                sugoiUser, "test", "selfmanaged", "notexisting_selfmanaged")));
    assertThat(
        "If an application does not exist, the user is not member",
        false,
        is(
            permissions.isMemberOfSelfManagedGroup(
                sugoiUser, "test", "notexisting", "isnotmember_notexisting")));
  }

  @Test
  @DisplayName(
      "Given an application without the self-managed-group property, "
          + "with a user being in one group but not in the other, "
          + "the user should not be able to manage any groups")
  void changingMembersWithoutSelfManageAppTest() {
    SugoiUser sugoiUser = new SugoiUser("user", List.of("role_group_notselfmanaged"));
    assertThat(
        "The user should not be able to add a member in any group",
        false,
        is(
            permissions.isMemberOfSelfManagedGroup(
                sugoiUser, "test", "notselfmanaged", "group_notselfmanaged")));
  }

  @Test
  @DisplayName(
      "Given an application without the self-managed-group property, "
          + "with a user being in one self managed group and another not , "
          + "the user should be able to manage only the self managed group")
  void changingMembersWithoutSelfManageAppInSelfManagedGroupButWithSelfManagedGroupTest() {
    SugoiUser sugoiUser =
        new SugoiUser(
            "user",
            List.of("role_selfManagedGroup_notselfmanaged", "role_ismember_notselfmanaged"));
    assertThat(
        "The user should be able to add a member in his group but not any",
        true,
        is(
            permissions.isMemberOfSelfManagedGroup(
                sugoiUser, "test", "notselfmanaged", "selfManagedGroup_notselfmanaged")));
    assertThat(
        "The user should not be able to add a member in another default existing group",
        false,
        is(
            permissions.isMemberOfSelfManagedGroup(
                sugoiUser, "test", "notselfmanaged", "ismember_notselfmanaged")));
  }

  @Test
  @DisplayName(
      "Given an application with the self-managed-group property, "
          + "with a user being in a not self managed group and a self managed group, "
          + "the user should not be able to manage this group but others")
  void changingMemberInASelfManagedAppButNotInASelfManagedGroup() {
    SugoiUser sugoiUser =
        new SugoiUser(
            "user",
            List.of(
                "role_notSelfManagedGroup_selfmanaged",
                "role_selfManagedGroup_selfmanaged",
                "role_ismember_selfmanaged"));
    assertThat(
        "The user should not be able to add a member in his group",
        false,
        is(
            permissions.isMemberOfSelfManagedGroup(
                sugoiUser, "test", "selfmanaged", "notSelfManagedGroup_selfmanaged")));
    assertThat(
        "The user should not be able to add a member in his group",
        true,
        is(
            permissions.isMemberOfSelfManagedGroup(
                sugoiUser, "test", "selfmanaged", "selfManagedGroup_selfmanaged")));
    assertThat(
        "The user should be able to add a member in another existing group",
        true,
        is(
            permissions.isMemberOfSelfManagedGroup(
                sugoiUser, "test", "selfmanaged", "ismember_selfmanaged")));
  }

  @Test
  @DisplayName(
      "Given an application without the self-managed-group property, "
          + "with a user being in a not self-managed group, "
          + "the user should not be able to manage any groups")
  void changingMembersWithoutSelfManageAppButWithSelfManagedGroupTest() {
    SugoiUser sugoiUser = new SugoiUser("user", List.of("role_notSelfManagedGroup_notselfmanaged"));
    assertThat(
        "The user should not be able to add a member in any group",
        false,
        is(
            permissions.isMemberOfSelfManagedGroup(
                sugoiUser, "test", "notselfmanaged", "notSelfManagedGroup_notselfmanaged")));
  }

  @Test
  @DisplayName(
      "Given a user being in a group managing an other group"
          + "the user should be able to manage this group and only this group")
  void groupManagerTest() {
    SugoiUser sugoiUser =
        new SugoiUser("user", List.of("role_admin_appli", "role_ASI_manageme_appli", "", "role_"));
    assertThat(
        "The user should be manager of manageme",
        permissions.isGroupManager(sugoiUser, "test", "appli", "manageme"),
        is(true));
    assertThat(
        "The user should not be manager of admin",
        permissions.isGroupManager(sugoiUser, "test", "appli", "admin"),
        is(false));
    assertThat(
        "The user should not be manager of the application",
        permissions.isApplicationManager(sugoiUser, "test", "appli"),
        is(false));
  }
}
