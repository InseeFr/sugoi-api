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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.commons.services.controller.technics.SugoiAdviceController;
import fr.insee.sugoi.core.service.CredentialsService;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.exceptions.UserNotFoundException;
import fr.insee.sugoi.services.view.PasswordView;
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
    classes = {CredentialsController.class, SugoiAdviceController.class},
    properties = "spring.config.location=classpath:/controller/application.properties")
@AutoConfigureMockMvc
@EnableWebMvc
public class CredentialsControllerTest {

  @Autowired MockMvc mockMvc;

  @MockBean private CredentialsService credentialService;

  @MockBean private UserService userService;

  ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setup() {}

  @Test
  @WithMockUser(roles = "Admin_Sugoi")
  public void testWrongCredential() throws Exception {

    Mockito.when(
            credentialService.validateCredential(
                Mockito.eq("domaine1"),
                Mockito.eq("userStorage"),
                Mockito.eq("user"),
                Mockito.eq("myPassword")))
        .thenReturn(true);

    PasswordView passwordView = new PasswordView();
    passwordView.setPassword("false");

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post(
                "/realms/domaine1/storages/userStorage/users/user/validate-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(passwordView))
            .with(csrf());
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();

    assertThat("Response status should be 401", response.getStatus(), is(401));
  }

  @Test
  @WithMockUser(roles = "Admin_Sugoi")
  public void testRightCredential() throws Exception {
    Mockito.when(
            credentialService.validateCredential(
                Mockito.eq("domaine1"),
                Mockito.eq("userStorage"),
                Mockito.eq("user"),
                Mockito.eq("myPassword")))
        .thenReturn(true);

    PasswordView passwordView = new PasswordView();
    passwordView.setPassword("myPassword");

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post(
                "/realms/domaine1/storages/userStorage/users/user/validate-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(passwordView))
            .with(csrf());
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();

    assertThat("Response status should be 200", response.getStatus(), is(200));
  }

  @Test
  @WithMockUser(roles = "Admin_Sugoi")
  public void testNoPasswordParam() throws Exception {

    Mockito.when(
            credentialService.validateCredential(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
        .thenReturn(true);

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post(
                "/realms/domaine1/storages/userStorage/users/user/validate-password")
            .contentType(MediaType.APPLICATION_JSON)
            .with(csrf());
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();

    assertThat("Response status should be 400", response.getStatus(), is(400));
  }

  @Test
  @WithMockUser(roles = "Admin_Sugoi")
  public void testValidatePasswordNoUserStorage() throws Exception {

    Mockito.when(
            credentialService.validateCredential(
                Mockito.eq("domaine1"),
                Mockito.eq("foundUserstorage"),
                Mockito.eq("user"),
                Mockito.eq("myPassword")))
        .thenReturn(true);

    User user = new User("user");
    user.getMetadatas().put("userStorage", "foundUserstorage");

    Mockito.when(userService.findById(Mockito.eq("domaine1"), Mockito.isNull(), Mockito.eq("user")))
        .thenReturn(user);

    PasswordView passwordView = new PasswordView();
    passwordView.setPassword("myPassword");

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post("/realms/domaine1/users/user/validate-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(passwordView))
            .with(csrf());
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();
    assertThat("Response status should be 200", response.getStatus(), is(200));
  }
  ;

  @Test
  @WithMockUser(roles = "Admin_Sugoi")
  public void testInvalidatePasswordNoUserStorage() throws Exception {

    Mockito.when(
            credentialService.validateCredential(
                Mockito.eq("domaine1"),
                Mockito.eq("foundUserstorage"),
                Mockito.eq("user"),
                Mockito.eq("myPassword")))
        .thenReturn(true);

    User user = new User("user");
    user.getMetadatas().put("userStorage", "foundUserstorage");

    Mockito.when(userService.findById(Mockito.eq("domaine1"), Mockito.isNull(), Mockito.eq("user")))
        .thenReturn(user);

    PasswordView passwordView = new PasswordView();
    passwordView.setPassword("badPassword");

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post("/realms/domaine1/users/user/validate-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(passwordView))
            .with(csrf());
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();

    assertThat("Response status should be 401", response.getStatus(), is(401));
  }
  ;

  @Test
  @WithMockUser(roles = "Admin_Sugoi")
  public void testValidatePasswordOfNoUser() throws Exception {

    Mockito.when(
            credentialService.validateCredential(
                Mockito.eq("domaine1"),
                Mockito.eq("foundUserstorage"),
                Mockito.eq("user"),
                Mockito.eq("myPassword")))
        .thenReturn(true);

    Mockito.when(userService.findById(Mockito.eq("domaine1"), Mockito.isNull(), Mockito.eq("user")))
        .thenThrow(new UserNotFoundException("domaine1", "user"));

    PasswordView passwordView = new PasswordView();
    passwordView.setPassword("myPassword");

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post("/realms/domaine1/users/user/validate-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(passwordView))
            .with(csrf());
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();

    assertThat("Response status should be 404", response.getStatus(), is(404));
  }
}
