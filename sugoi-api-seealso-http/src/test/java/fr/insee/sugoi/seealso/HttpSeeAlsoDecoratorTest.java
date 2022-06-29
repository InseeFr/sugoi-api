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
package fr.insee.sugoi.seealso;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.core.seealso.SeeAlsoCredentialsConfiguration;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.User;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {HttpSeeAlsoDecorator.class, SeeAlsoCredentialsConfiguration.class})
@TestPropertySource(locations = "classpath:/application.properties")
@EnableConfigurationProperties(value = SeeAlsoCredentialsConfiguration.class)
public class HttpSeeAlsoDecoratorTest {

  private static ObjectMapper mapper = new ObjectMapper();
  @Autowired HttpSeeAlsoDecorator httpSeeAlsoDecorator;
  static MockWebServer mockWebServer;
  static SeeAlsoServerDispatcher dispatcher = new SeeAlsoServerDispatcher();
  static final MockResponse response403 = new MockResponse().setResponseCode(403);
  static final MockResponse response401 = new MockResponse().setResponseCode(401);
  static final MockResponse response500 = new MockResponse().setResponseCode(500);

  static class SeeAlsoServerDispatcher extends Dispatcher {

    @Override
    public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
      try {
        User toto = new User();
        toto.setUsername("toto");
        Group group1 = new Group("group1", "app");
        Group group2 = new Group("group2", "app");
        toto.setGroups(List.of(group1, group2));
        toto.addAttributes("listOfStuff", List.of("something", "else"));
        if (request.getHeader("Authorization") != null) {
          if (request
              .getHeader("Authorization")
              .equals("Basic " + Base64.getEncoder().encodeToString("user:pa;ssword".getBytes()))) {
            return new MockResponse().setResponseCode(200).setBody(mapper.writeValueAsString(toto));
          } else return response403;
        } else return response401;
      } catch (JsonProcessingException e) {
        return response500;
      }
    }
  }

  @BeforeAll
  static void init() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.setDispatcher(new SeeAlsoServerDispatcher());
    mockWebServer.start();
  }

  @Test
  public void testGetStringResourceFromHttpUrl() throws InterruptedException {
    Object res =
        httpSeeAlsoDecorator.getResourceFromUrl(
            "http://localhost:" + mockWebServer.getPort() + "/user/toto", "groups[0].name");
    assertThat("Group name should be group1", res, is("group1"));
  }

  @Test
  public void testGetListResourceFromHttpUrl() {
    Object res =
        httpSeeAlsoDecorator.getResourceFromUrl(
            "http://localhost:" + mockWebServer.getPort() + "/user/toto", "attributes.listOfStuff");
    assertThat("Should have habilitation something", ((List<?>) res).get(0), is("something"));
    assertThat("Should have habilitation else", ((List<?>) res).get(1), is("else"));
  }

  @AfterAll
  public static void end() throws IOException {
    mockWebServer.shutdown();
  }
}
