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

import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.impl.PermissionServiceImpl;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(
    classes = PermissionServiceImpl.class,
    properties = "spring.config.location=classpath:/permissions/test-regexp-permissions.properties")
public class PermissionServiceTests {

  @MockBean private RealmProvider realmProvider;

  @Autowired PermissionService permissions;

  @Test
  public void testAdminRegexp() {
    assertThat("User is admin", permissions.isAdmin(List.of("role_Admin_Sugoi")));
    assertThat("User on sugoii is not admin", !permissions.isAdmin(List.of("role_Admin_Sugoii")));
  }

  @Test
  public void testReaderRegexp() {
    List<String> readerRoles = List.of("role_Reader_realm1_sugoi");

    assertThat("User can read realm1", permissions.isReader(readerRoles, "realm1", ""), is(true));
    assertThat(
        "User cannot write realm1", permissions.isWriter(readerRoles, "realm1", ""), is(false));
    assertThat(
        "User cannot read realm2", permissions.isReader(readerRoles, "realm2", ""), is(false));
  }

  @Test
  public void testWriterRegexp() {
    List<String> writerRoles = List.of("role_Writer_realm1_sugoi");
    assertThat("User can read realm1", permissions.isReader(writerRoles, "realm1", ""), is(true));
    assertThat("User can write realm1", permissions.isWriter(writerRoles, "realm1", ""), is(true));
    assertThat(
        "User cannot read realm2", permissions.isReader(writerRoles, "realm2", ""), is(false));
  }

  @Test
  public void testUserStorageNull() {
    List<String> writerRoles = List.of("role_Writer_realm1_sugoi");
    assertThat(
        "No nullPointerException", permissions.isReader(writerRoles, "realm1", null), is(true));
  }

  @Test
  public void testAppManager() {
    List<String> appRoles = List.of("role_Asi_realm1_appli1", "role_reader_realm1_sugoi");
    assertThat(
        "user is app manager for appli1 in realm1",
        permissions.isApplicationManager(appRoles, "realm1", "appli1"),
        is(true));
    assertThat(
        "user is reader in realm1", permissions.isReader(appRoles, "realm1", null), is(true));
  }

  @Test
  public void testAppManagerWithoutRealmInRoleName() {
    List<String> withoutRealmRoles = List.of("role_Asi_appli1", "role_reader_realm1_sugoi");
    assertThat(
        "user is app manager for appli1 in realm1",
        permissions.isApplicationManager(withoutRealmRoles, "realm1", "appli1"),
        is(true));
    assertThat(
        "user is reader in realm1",
        permissions.isReader(withoutRealmRoles, "realm1", null),
        is(true));
    assertThat(
        "user is app manager for appli1 in realm2",
        permissions.isApplicationManager(withoutRealmRoles, "realm2", "appli1"),
        is(false));
    assertThat(
        "user is reader in realm2",
        permissions.isReader(withoutRealmRoles, "realm2", null),
        is(false));
  }

  @Test
  public void testAdminWildcard() {
    List<String> adminWildcardRole = List.of("role_nimportequoi_admin");
    assertThat("user is admin sugoi", permissions.isAdmin(adminWildcardRole), is(true));
  }

  @Test
  public void testgetAllowedAttributePattern() {
    List<String> roles = List.of("role_ASI_realm1_app1", "role_ASI_app2");
    assertThat(
        "Toto_app1 should be accepted according to the pattern",
        permissions.isValidAttributeAccordingAttributePattern(
            roles, "realm1", null, "(.*)_$(application)", "toto_app1"));
    assertThat(
        "Toto_app2 should be denied",
        !permissions.isValidAttributeAccordingAttributePattern(
            roles, "realm1", null, "(.*)_$(application)", "toto_app3"));
  }

  @Test
  public void testGetReaderRole() {
    List<String> roles =
        List.of(
            "role_toto_titi",
            "role_Reader_realm2",
            "role_Reader_realm1_sugoi",
            "role_Reader_realm3_sugoi");
    List<String> readableRealm = permissions.getReaderRoles(roles);
    assertThat("There should only be 2 valid reader roles", 2, is(readableRealm.size()));
    assertThat("should contain realm1", readableRealm.contains("REALM1"));
    assertThat("should contain realm3", readableRealm.contains("REALM3"));
  }

  @Test
  public void testGetWriterRole() {
    List<String> roles =
        List.of(
            "role_toto_titi",
            "role_Reader_realm2",
            "role_Writer_realm1_sugoi",
            "role_Reader_realm3_sugoi");
    List<String> writableRealm = permissions.getWriterRoles(roles);
    assertThat("There should only be 1 valid writer role", 1, is(writableRealm.size()));
    assertThat("should contain realm1", writableRealm.contains("REALM1"));
  }

  @Test
  public void testGetAppManagerRole() {
    List<String> roles = List.of("role_ASI_realm1_appli", "role_ASI_appli2");
    List<String> appManagerRealms = permissions.getAppManagerRoles(roles);
    assertThat("There should only be 2 valid appmanager roles", 2, is(appManagerRealms.size()));
    assertThat(
        "should contain realm and appli when application scoped",
        appManagerRealms.contains("REALM1\\APPLI"));
    assertThat("can contain only application", appManagerRealms.contains("*_*\\APPLI2"));
  }

  @Test
  public void testGetPasswordManagerRole() {
    List<String> roles =
        List.of(
            "role_passwordmanager_toto_sugoi",
            "role_passwordmanager_realm1_tata_sugoi",
            "role_Writer_realm2_sugoi");
    List<String> passwordManagerRealms = permissions.getPasswordManagerRoles(roles);
    assertThat(
        "There should only be 2 valid passwordmanager role", 2, is(passwordManagerRealms.size()));
    assertThat("should contain realm1 us tata", passwordManagerRealms.contains("REALM1_TATA"));
    assertThat("should contain toto", passwordManagerRealms.contains("TOTO"));
  }

  @Test
  public void testIsPasswordManager() {
    List<String> roles =
        List.of(
            "role_passwordmanager_toto_sugoi",
            "role_passwordmanager_realm1_tata_sugoi",
            "role_Writer_realm2_sugoi");
    assertThat(
        "Should have the password manager permission with null us",
        permissions.isPasswordManager(roles, "toto", null));
    assertThat(
        "Should have the password manager permission with tata us",
        permissions.isPasswordManager(roles, "realm1", "tata"));
    assertThat(
        "Should not have the password manager permission with null us",
        !permissions.isPasswordManager(roles, "realm1", null));
    assertThat(
        "Should not have the password manager permission with default us",
        !permissions.isPasswordManager(roles, "realm1", "default"));
  }
}
