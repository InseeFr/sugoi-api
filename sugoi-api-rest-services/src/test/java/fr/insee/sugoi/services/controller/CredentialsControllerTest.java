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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import fr.insee.sugoi.core.service.CredentialsService;
import fr.insee.sugoi.core.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootTest(
    classes = CredentialsController.class,
    properties = "spring.config.location=classpath:/controller/application.properties")
@AutoConfigureMockMvc
@EnableWebMvc
public class CredentialsControllerTest {

  @Autowired MockMvc mockMvc;

  @MockBean private CredentialsService credentialService;

  @MockBean private UserService userService;

  // User user1;

  @BeforeEach
  public void setup() {}

  @Test
  @WithMockUser(roles = "Admin_Sugoi")
  public void testWrongCredential() {
    try {
      Mockito.when(
              credentialService.validateCredential(
                  Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any()))
          .thenReturn(false);

      Mockito.when(
              credentialService.validateCredential(
                  Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any()))
          .thenReturn(false);

      RequestBuilder requestBuilder =
          MockMvcRequestBuilders.post(
                  "/realms/domaine1/storages/userStorage/users/user/validate-password")
              .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
              .param("password", "myPassword")
              .with(csrf());
      MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();

      assertThat("Response status should be 401", response.getStatus(), is(401));

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  @WithMockUser(roles = "Admin_Sugoi")
  public void testRightCredential() {
    try {

      Mockito.when(
              credentialService.validateCredential(
                  Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any()))
          .thenReturn(true);

      RequestBuilder requestBuilder =
          MockMvcRequestBuilders.post(
                  "/realms/domaine1/storages/userStorage/users/user/validate-password")
              .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
              .param("password", "myPassword")
              .with(csrf());
      MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();

      assertThat("Response status should be 200", response.getStatus(), is(200));

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  @WithMockUser(roles = "Admin_Sugoi")
  public void testNoPasswordParam() {
    try {

      Mockito.when(
              credentialService.validateCredential(
                  Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any()))
          .thenReturn(true);

      RequestBuilder requestBuilder =
          MockMvcRequestBuilders.post(
                  "/realms/domaine1/storages/userStorage/users/user/validate-password")
              .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
              .param("passwordd", "myPassword")
              .with(csrf());
      MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();

      assertThat("Response status should be 401", response.getStatus(), is(401));

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }
}
