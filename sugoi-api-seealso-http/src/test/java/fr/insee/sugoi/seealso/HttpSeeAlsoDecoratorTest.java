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
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.User;
import java.lang.reflect.InaccessibleObjectException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;

@SpringBootTest(classes = HttpSeeAlsoDecorator.class)
public class HttpSeeAlsoDecoratorTest {

  private ObjectMapper mapper = new ObjectMapper();
  @Mock private WebClient webClient;
  @Mock private RequestHeadersUriSpec<?> requestHeadersUriSpec;
  @Mock private RequestHeadersSpec<?> requestHeadersSpec;
  @Mock private ResponseSpec responseSpec;
  @InjectMocks HttpSeeAlsoDecorator httpSeeAlsoDecorator;

  @BeforeEach
  public void init() {

    try {
      User toto = new User();
      toto.setUsername("toto");
      Group group1 = new Group("app", "group1");
      Group group2 = new Group("app", "group2");
      toto.setGroups(List.of(group1, group2));
      toto.addAttributes("listOfStuff", List.of("something", "else"));

      OngoingStubbing<RequestHeadersUriSpec<?>> stubWebClient = when(webClient.get());
      stubWebClient.thenReturn(requestHeadersUriSpec);
      OngoingStubbing<RequestHeadersSpec<?>> stubRequestHeaderSpec =
          when(requestHeadersUriSpec.uri("https://test.this.url/user/toto"));
      stubRequestHeaderSpec.thenReturn(requestHeadersSpec);
      when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
      when(responseSpec.bodyToMono(String.class))
          .thenReturn(Mono.just(mapper.writeValueAsString(toto)));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testGetStringResourceFromHttpUrl() {
    try {
      Object res =
          httpSeeAlsoDecorator.getResourceFromUrl(
              "https://test.this.url/user/toto", "groups[0].name");
      assertThat("Group name should be group1", res, is("group1"));
    } catch (SecurityException | InaccessibleObjectException e) {
      fail(e);
      e.printStackTrace();
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testGetListResourceFromHttpUrl() {
    Object res =
        httpSeeAlsoDecorator.getResourceFromUrl(
            "https://test.this.url/user/toto", "attributes.listOfStuff");
    assertThat("Should have habilitation something", ((List<String>) res).get(0), is("something"));
    assertThat("Should have habilitation else", ((List<String>) res).get(1), is("else"));
  }
}
