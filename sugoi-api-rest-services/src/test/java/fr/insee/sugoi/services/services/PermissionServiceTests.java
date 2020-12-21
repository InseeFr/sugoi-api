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
    assertThat("User can read realm1", permissions.isAtLeastReader("realm1", ""), is(true));
    assertThat("User cannot write realm1", permissions.isAtLeastWriter("realm1", ""), is(false));
    assertThat("User cannot read realm2", permissions.isAtLeastReader("realm2", ""), is(false));
  }

  @Test
  @WithMockUser(username = "writer_realm1", roles = "Writer_realm1_sugoi")
  public void testWriterRegexp() {
    assertThat("User can read realm1", permissions.isAtLeastReader("realm1", ""), is(true));
    assertThat("User can write realm1", permissions.isAtLeastWriter("realm1", ""), is(true));
    assertThat("User cannot read realm2", permissions.isAtLeastReader("realm2", ""), is(false));
  }
}
