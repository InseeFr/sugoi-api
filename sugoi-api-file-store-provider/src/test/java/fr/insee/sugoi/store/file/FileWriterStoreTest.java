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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.UserStorage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(
    classes = {FileStoreBeans.class},
    properties = "fr.insee.sugoi.store.file.folder=./target/sugoi-${random.uuid}")
public class FileWriterStoreTest {
  public Map<String, String> config = new HashMap<>();

  @MockBean private Realm realm;
  @MockBean private UserStorage us;

  @Autowired private FileWriterStore writer;

  private User user1 = new User();

  private static ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  public void init() {
    user1.setUsername("test1");
    user1.setMail("test@insee.fr");
  }

  @Test
  public void testCreateUser() throws JsonParseException, JsonMappingException, IOException {
    writer.createUser(user1);
    File user1File =
        Paths.get(writer.getStoreFolder(), "users").resolve(user1.getUsername() + ".json").toFile();
    assertThat("File should be created", user1File.exists(), is(true));
    assertThat(
        "Files should contain expected json",
        mapper.readValue(user1File, User.class).getMail(),
        is("test@insee.fr"));
  }

  @Test
  public void testDeleteUser() throws JsonParseException, JsonMappingException, IOException {
    writer.createUser(user1);
    File user1File =
        Paths.get(writer.getStoreFolder(), "users").resolve(user1.getUsername() + ".json").toFile();
    writer.deleteUser(user1.getUsername());
    assertThat("File should be deleted", user1File.exists(), is(false));
  }

  @Test
  public void testUpdateUser() throws JsonParseException, JsonMappingException, IOException {
    writer.createUser(user1);
    File user1File =
        Paths.get(writer.getStoreFolder(), "users").resolve(user1.getUsername() + ".json").toFile();
    user1.setMail("modify@insee.fr");
    writer.updateUser(user1);
    assertThat("File should be created", user1File.exists(), is(true));
    assertThat(
        "Files should contain expected json",
        mapper.readValue(user1File, User.class).getMail(),
        is("modify@insee.fr"));
  }
}
