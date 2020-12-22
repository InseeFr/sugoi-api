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
package fr.insee.sugoi.store.file;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.UserStorage;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(
    classes = {FileStoreBeans.class},
    properties = "fr.insee.sugoi.store.file.folder=./src/test/resources/sugoi-file-tests")
public class FileReaderStoreTest {
  public Map<String, String> config = new HashMap<>();

  @MockBean private Realm realm;
  @MockBean private UserStorage us;

  @Autowired private FileReaderStore reader;

  @Test
  public void testGetUser() {
    User user = reader.getUser("test1");
    assertThat("user must be found", user, not(nullValue()));
    assertThat("user mail should be test@insee.fr", user.getMail(), is("test@insee.fr"));
  }

  @Test
  public void testSearchUser() {
    PageResult<User> users =
        reader.searchUsers(null, null, null, null, null, null, null, null, null, null, null, null);

    assertThat("users must be found", users, not(nullValue()));
    assertThat("users size should be 2", users.getResults().size(), is(2));
  }
}
