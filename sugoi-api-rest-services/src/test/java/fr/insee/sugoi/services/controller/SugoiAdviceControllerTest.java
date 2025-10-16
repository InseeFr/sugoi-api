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
package fr.insee.sugoi.services.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.commons.services.controller.technics.SugoiAdviceController;
import fr.insee.sugoi.commons.services.view.ErrorView;
import fr.insee.sugoi.core.service.CertificateService;
import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.model.exceptions.UnableToUpdateCertificateException;
import fr.insee.sugoi.model.exceptions.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootTest(
    classes = {UserController.class, SugoiAdviceController.class},
    properties = "spring.config.location=classpath:/controller/application.properties")
@AutoConfigureMockMvc
@EnableWebMvc
public class SugoiAdviceControllerTest {

  @Autowired MockMvc mockMvc;

  @MockitoBean private UserService userService;

  @MockitoBean private CertificateService certificateService;

  @MockitoBean private ConfigService configService;

  ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @WithMockUser
  public void accessDeniedExceptionIsHandled() throws Exception {
    testResponseFromException(
        new AccessDeniedException("custom message"), HttpStatus.FORBIDDEN, "custom message");
  }

  @Test
  // a solution to retrieve this exception as a Sugoi ErrorView can be looked for
  public void unauthorizedExceptionIsHandledBySpring() throws Exception {
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/domaine1/users/Toto")
            .accept(MediaType.APPLICATION_JSON);
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();
    assertThat(response.getStatus(), is(HttpStatus.UNAUTHORIZED.value()));
  }

  @Test
  @WithMockUser
  public void get404IfNoHandlerFound() throws Exception {
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/n/importe/quoi").accept(MediaType.APPLICATION_JSON);
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();
    assertThat(response.getStatus(), is(HttpStatus.NOT_FOUND.value()));
  }

  @Test
  @WithMockUser
  public void springExceptionAreHandledBySpring() throws Exception {
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/domaine1/users/Toto").accept(MediaType.APPLICATION_PDF);
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();
    assertThat(response.getStatus(), is(HttpStatus.NOT_ACCEPTABLE.value()));
  }

  @Test
  @WithMockUser
  public void customSugoiExceptionAreHandledByController() throws Exception {
    testResponseFromException(
        new UserNotFoundException("realm1", "toto"),
        HttpStatus.NOT_FOUND,
        "User toto does not exist in realm realm1");
  }

  @Test
  @WithMockUser
  public void customRuntimeExceptionSugoiExceptionAreHandledByController() throws Exception {
    testResponseFromException(
        new UnableToUpdateCertificateException("custom message"),
        HttpStatus.INTERNAL_SERVER_ERROR,
        "custom message");
  }

  @Test
  @WithMockUser
  public void javaBuildinExceptionAreHandledByControllerWhenDeclared() throws Exception {
    testResponseFromException(
        new UnsupportedOperationException("custom message"),
        HttpStatus.NOT_IMPLEMENTED,
        "custom message");
  }

  @Test
  @WithMockUser
  public void methodNotSupportedExceptionIsHandledBecauseDeclared() throws Exception {
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.patch("/realms/domaine1/users/Toto")
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();
    assertThat("Should get a 405", response.getStatus(), is(405));
  }

  @Test
  @WithMockUser
  public void javaBuildinExceptionAreReturnedAs500WhenNotDeclared() throws Exception {
    testResponseFromException(
        new NullPointerException("custom message"),
        HttpStatus.INTERNAL_SERVER_ERROR,
        "custom message");
  }

  private void testResponseFromException(
      Exception thrownException, HttpStatus expectedStatus, String expectedMessage)
      throws Exception {
    Mockito.when(
            userService.findById(Mockito.anyString(), Mockito.any(), Mockito.any(), anyBoolean()))
        .thenThrow(thrownException);
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/domaine1/users/Toto")
            .accept(MediaType.APPLICATION_JSON);
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();
    assertThat(
        "When throwing "
            + thrownException.getClass().getSimpleName()
            + " then should respond "
            + expectedStatus.value(),
        response.getStatus(),
        is(expectedStatus.value()));
    ErrorView error = objectMapper.readValue(response.getContentAsString(), ErrorView.class);
    assertThat("Message should be accurate", error.getMessage(), is(expectedMessage));
  }
}
