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
package fr.insee.sugoi.services.services;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest(
    classes = PermissionService.class,
    properties = "spring.config.location=classpath:/permissions/test-regexp-permissions.properties")
public class PermissionServiceTests {

  @Autowired PermissionService permissions;

  @Test
  @WithMockUser(username = "admin", roles = "Admin_Sugoi")
  public void testAdminRegexp() {
    assertThat("User is admin", permissions.isAdmin(), is(true));
  }

  @Test
  @WithMockUser(username = "reader_realm1", roles = "Reader_realm1_sugoi")
  public void testReaderRegexp() {
    assertThat("User can read realm1", permissions.isReader("realm1", ""), is(true));
    assertThat("User cannot write realm1", permissions.isWriter("realm1", ""), is(false));
    assertThat("User cannot read realm2", permissions.isReader("realm2", ""), is(false));
  }

  @Test
  @WithMockUser(username = "writer_realm1", roles = "Writer_realm1_sugoi")
  public void testWriterRegexp() {
    assertThat("User can read realm1", permissions.isReader("realm1", ""), is(true));
    assertThat("User can write realm1", permissions.isWriter("realm1", ""), is(true));
    assertThat("User cannot read realm2", permissions.isReader("realm2", ""), is(false));
  }

  @Test
  @WithMockUser(username = "writer_realm1", roles = "Writer_realm1_sugoi")
  public void testUserStorageNull() {
    try {
      assertThat("No nullPointerException", permissions.isReader("realm1", null), is(true));
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  @WithMockUser(username = "appmanager_realm1_appli1", roles = "Asi_realm1_appli1")
  public void testAppManager() {
    try {
      assertThat(
          "user is app manager for appli1 in realm1",
          permissions.isApplicationManager("realm1", null, "appli1"),
          is(true));
      assertThat("user is reader in realm1", permissions.isReader("realm1", null), is(true));

    } catch (Exception e) {
      fail();
    }
  }

  @Test
  @WithMockUser(username = "appmanager_appli1", roles = "Asi_appli1")
  public void testAppManagerWithoutRealmInRoleName() {
    try {
      assertThat(
          "user is app manager for appli1 in realm1",
          permissions.isApplicationManager("realm1", null, "appli1"),
          is(true));
      assertThat("user is reader in realm1", permissions.isReader("realm1", null), is(true));
      assertThat(
          "user is app manager for appli1 in realm2",
          permissions.isApplicationManager("realm2", null, "appli1"),
          is(true));
      assertThat("user is reader in realm2", permissions.isReader("realm2", null), is(true));

    } catch (Exception e) {
      fail();
    }
  }

  @Test
  @WithMockUser(username = "admin_wildcard", roles = "nimportequoi_admin")
  public void testAdminWildcard() {
    try {
      assertThat("user is admin sugoi", permissions.isAdmin(), is(true));
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  @WithMockUser(roles = {"ASI_realm1_app1", "ASI_app2"})
  public void testgetAllowedAttributePattern() {
    try {
      List<String> allowedAttributesPattern =
          permissions.getAllowedAttributePattern("toto", "tata", "(.*)_$(application)");
      assertThat("allowed Attributes Pattern", allowedAttributesPattern.size(), is(2));
    } catch (Exception e) {
      fail();
    }
  }
}
