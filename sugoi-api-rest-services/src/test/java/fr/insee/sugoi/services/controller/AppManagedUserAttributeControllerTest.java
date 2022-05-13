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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.commons.services.controller.technics.SugoiAdviceController;
import fr.insee.sugoi.commons.services.view.ErrorView;
import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.model.ProviderResponse.ProviderResponseStatus;
import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.core.service.impl.PermissionServiceImpl;
import fr.insee.sugoi.model.Realm;
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
    classes = {
      AppManagedUserAttributeController.class,
      SugoiAdviceController.class,
      PermissionServiceImpl.class
    },
    properties = "spring.config.location=classpath:/permissions/test-regexp-permissions.properties")
@AutoConfigureMockMvc
@EnableWebMvc
public class AppManagedUserAttributeControllerTest {
  @Autowired MockMvc mockMvc;

  @MockBean private UserService userService;

  @MockBean private ConfigService configService;

  ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setup() {
    Realm realm = new Realm();
    realm.setName("test");
    realm.addProperty(GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_PATTERNS_LIST, "(.*)_$(application)");
    realm.addProperty(GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_KEYS_LIST, "my-attribute-key");
    Mockito.when(configService.getRealm(Mockito.anyString())).thenReturn(realm);
    Mockito.doReturn(new ProviderResponse("", "requestId", ProviderResponseStatus.OK, null, null))
        .when(userService)
        .addAppManagedAttribute(
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any());
  }

  @Test
  @WithMockUser(roles = "ADMIN_SUGOI")
  public void get200WhenAddCorrectAttributesWhenAdminOrWriter() throws Exception {
    MockHttpServletResponse response =
        createPatchRequestFromUrl(
            "/realms/domaine1/storages/test/users/Toto/my-attribute-key/prop_role_appA");
    assertThat("Response must be 204 ACCEPTED", response.getStatus(), is(204));
  }

  @Test
  @WithMockUser(roles = "ASI_appA")
  public void get200WhenAddCorrectAttributesWhenAppManager() throws Exception {
    MockHttpServletResponse response =
        createPatchRequestFromUrl(
            "/realms/domaine1/storages/test/users/Toto/my-attribute-key/prop_role_appA");
    assertThat("Response must be 204 ACCEPTED", response.getStatus(), is(204));
  }

  @Test
  @WithMockUser(roles = "ASI_appA")
  public void get403WhenTryToAddValidAttributeWithBadPattern() throws Exception {
    MockHttpServletResponse response =
        createPatchRequestFromUrl(
            "/realms/domaine1/storages/test/users/Toto/my-attribute-key/appA_toto");
    assertThat("Response must be 403", response.getStatus(), is(403));
    assertThat(
        "Message should be access denied and give the pattern",
        getMessageFromErrorView(response),
        is(
            "Not allowed to add appmanagedattribute my-attribute-key . Attribute should match pattern : (.*)_$(APPLICATION)"));
  }

  @Test
  @WithMockUser(roles = "ASI_SUGOI")
  public void get403WhenAddIncorrectAttributes() throws Exception {
    MockHttpServletResponse response =
        createPatchRequestFromUrl(
            "/realms/domaine1/storages/test/users/Toto/my-attribute-key2/prop_role_appA");
    assertThat("Response must be 403", response.getStatus(), is(403));
    assertThat(
        "Message should be not an app managed attribute",
        getMessageFromErrorView(response),
        is("Cannot add delete to user: my-attribute-key2 is not an app managed attribute"));
  }

  @Test
  @WithMockUser(roles = "ASI_SUGOI")
  public void get403WhenNoRightIncorrectAttributes() throws Exception {
    MockHttpServletResponse response =
        createPatchRequestFromUrl(
            "/realms/domaine1/storages/test/users/Toto/my-attribute-key/prop_role_appA");
    assertThat("Response must be 403", response.getStatus(), is(403));
    assertThat(
        "Message should be access denied and give the pattern",
        getMessageFromErrorView(response),
        is(
            "Not allowed to add appmanagedattribute my-attribute-key . Attribute should match pattern : (.*)_$(APPLICATION)"));
  }

  private MockHttpServletResponse createPatchRequestFromUrl(String url) throws Exception {
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.patch(url)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());
    return mockMvc.perform(requestBuilder).andReturn().getResponse();
  }

  private String getMessageFromErrorView(MockHttpServletResponse response) throws Exception {
    return objectMapper.readValue(response.getContentAsString(), ErrorView.class).getMessage();
  }
}
