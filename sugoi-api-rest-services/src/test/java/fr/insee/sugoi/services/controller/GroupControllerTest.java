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
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.commons.services.controller.technics.SugoiAdviceController;
import fr.insee.sugoi.commons.services.view.ErrorView;
import fr.insee.sugoi.core.exceptions.GroupAlreadyExistException;
import fr.insee.sugoi.core.exceptions.GroupNotFoundException;
import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.model.ProviderResponse.ProviderResponseStatus;
import fr.insee.sugoi.core.service.GroupService;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
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
    classes = {GroupController.class, SugoiAdviceController.class},
    properties = "spring.config.location=classpath:/controller/application.properties")
@AutoConfigureMockMvc
@EnableWebMvc
public class GroupControllerTest {

  @Autowired MockMvc mockMvc;

  @MockBean private GroupService groupService;

  ObjectMapper objectMapper = new ObjectMapper();
  Group group1, group2, group2Updated;
  PageResult<Group> pageResult;

  class GroupDescriptionMatcher implements ArgumentMatcher<Group> {

    private String description;

    public GroupDescriptionMatcher(String description) {
      this.description = description;
    }

    @Override
    public boolean matches(Group group) {
      return group.getDescription().equals(description);
    }
  }

  @BeforeEach
  public void setup() {
    group1 = new Group();
    group1.setName("Group1");
    group1.setDescription("super groupe");

    group2 = new Group();
    group2.setName("Group2");
    group2.setDescription("groupe moyen");

    group2Updated = new Group();
    group2Updated.setName("Group2");
    group2Updated.setDescription("groupe pas fou");

    List<Group> groups = new ArrayList<>();
    groups.add(group1);
    groups.add(group2);
    pageResult = new PageResult<Group>();
    pageResult.setResults(groups);
    pageResult.setSearchToken("mySearchToken");
  }

  // Test read requests on good query

  @Test
  @WithMockUser
  public void retrieveAllGroups() throws Exception {

    Mockito.when(
            groupService.findByProperties(
                Mockito.eq("domaine1"),
                Mockito.eq("monApplication"),
                Mockito.any(Group.class),
                Mockito.any(PageableResult.class)))
        .thenReturn(pageResult);

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/domaine1/applications/monApplication/groups")
            .accept(MediaType.APPLICATION_JSON);
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();
    TypeReference<PageResult<Group>> mapType = new TypeReference<PageResult<Group>>() {};
    PageResult<Group> appRes = objectMapper.readValue(response.getContentAsString(), mapType);

    assertThat(
        "First element should be Group1", appRes.getResults().get(0).getName(), is("Group1"));
    assertThat(
        "Second element should be Group2", appRes.getResults().get(1).getName(), is("Group2"));
    assertThat("Response code should be 200", response.getStatus(), is(200));
  }

  @Test
  @WithMockUser
  public void shouldRetrieveSomeGroups() throws Exception {

    PageResult<Group> onlySuperPageResult = new PageResult<>();
    onlySuperPageResult.setResults(List.of(group1));

    Mockito.when(
            groupService.findByProperties(
                Mockito.eq("domaine1"),
                Mockito.eq("monApplication"),
                Mockito.argThat(new GroupDescriptionMatcher("super")),
                Mockito.any(PageableResult.class)))
        .thenReturn(onlySuperPageResult);

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/domaine1/applications/monApplication/groups")
            .param("description", "super")
            .accept(MediaType.APPLICATION_JSON);
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();
    TypeReference<PageResult<Group>> mapType = new TypeReference<PageResult<Group>>() {};
    PageResult<Group> appRes = objectMapper.readValue(response.getContentAsString(), mapType);

    assertThat("Should be only super description", appRes.getResults().size(), is(1));
  }

  @Test
  @WithMockUser
  public void shouldGetGroupByID() throws Exception {

    Mockito.when(groupService.findById("domaine1", "monApplication", "Group1")).thenReturn(group1);

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/domaine1/applications/monApplication/groups/Group1")
            .accept(MediaType.APPLICATION_JSON);

    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();
    Group res = objectMapper.readValue(response.getContentAsString(), Group.class);

    verify(groupService).findById("domaine1", "monApplication", "Group1");
    assertThat("Group returned should be Group1", res.getName(), is("Group1"));
  }

  // Test write requests

  @Test
  @WithMockUser
  public void deleteShouldCallDeleteService() throws Exception {
    Mockito.doReturn(new ProviderResponse("", "requestId", ProviderResponseStatus.OK, group1, null))
        .when(groupService)
        .delete(
            Mockito.eq("domaine1"),
            Mockito.eq("monApplication"),
            Mockito.eq("supprimemoi"),
            Mockito.any(ProviderRequest.class));

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.delete(
                "/realms/domaine1/applications/monApplication/groups/supprimemoi")
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());

    mockMvc.perform(requestBuilder).andReturn();
    verify(groupService)
        .delete(
            Mockito.eq("domaine1"),
            Mockito.eq("monApplication"),
            Mockito.eq("supprimemoi"),
            Mockito.any(ProviderRequest.class));
  }

  @Test
  @WithMockUser
  public void updateShouldCallUpdateServiceAndReturnNewApp() throws Exception {

    Mockito.when(groupService.findById("domaine1", "monApplication", "Group2"))
        .thenReturn(group2Updated);
    Mockito.doReturn(
            new ProviderResponse(
                "Group2", "requestId", ProviderResponseStatus.OK, group2Updated, null))
        .when(groupService)
        .update(
            Mockito.eq("domaine1"),
            Mockito.eq("monApplication"),
            Mockito.argThat(new GroupDescriptionMatcher("groupe pas fou")),
            Mockito.any(ProviderRequest.class));
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.put("/realms/domaine1/applications/monApplication/groups/Group2")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(group2Updated))
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());

    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();

    verify(groupService)
        .update(
            Mockito.eq("domaine1"),
            Mockito.eq("monApplication"),
            Mockito.argThat(new GroupDescriptionMatcher("groupe pas fou")),
            Mockito.any(ProviderRequest.class));
    assertThat(
        "Should get updated group",
        objectMapper.readValue(response.getContentAsString(), Group.class).getDescription(),
        is("groupe pas fou"));

    assertThat(
        "Should get location",
        response.getHeader("Location"),
        is("http://localhost/realms/domaine1/applications/monApplication/groups/Group2"));
  }

  @Test
  @WithMockUser
  public void postShouldCallPostServiceAndReturnNewApp() throws Exception {

    Mockito.doReturn(new ProviderResponse("", "requestId", ProviderResponseStatus.OK, group1, null))
        .when(groupService)
        .create(
            Mockito.eq("domaine1"),
            Mockito.eq("monApplication"),
            Mockito.argThat(new GroupDescriptionMatcher("super groupe")),
            Mockito.any());
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post("/realms/domaine1/applications/monApplication/groups")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(group1))
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());

    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();
    verify(groupService)
        .create(
            Mockito.eq("domaine1"),
            Mockito.eq("monApplication"),
            Mockito.argThat(new GroupDescriptionMatcher("super groupe")),
            Mockito.any());
    assertThat(
        "Should get new group",
        objectMapper.readValue(response.getContentAsString(), Group.class).getName(),
        is("Group1"));
  }

  // Test location headers
  @Test
  @WithMockUser
  public void getNextLocationInSearchResponse() throws Exception {

    pageResult.setHasMoreResult(true);

    Mockito.when(
            groupService.findByProperties(
                Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any()))
        .thenReturn(pageResult);
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/domaine1/applications/monApplication/groups?size=2")
            .accept(MediaType.APPLICATION_JSON);

    assertThat(
        "Location header gives next page",
        mockMvc.perform(requestBuilder).andReturn().getResponse().getHeader("Location"),
        is(
            "http://localhost/realms/domaine1/applications/monApplication/groups?size=2&searchToken=mySearchToken"));
  }

  @Test
  @WithMockUser
  public void getObjectLocationInGroupCreationResponse() throws Exception {

    Mockito.when(
            groupService.create(
                Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any()))
        .thenReturn(
            new ProviderResponse(group1.getName(), null, ProviderResponseStatus.OK, null, null));

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post("/realms/domaine1/applications/monApplication/groups")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(group1))
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());

    assertThat(
        "Location header gives get uri",
        mockMvc.perform(requestBuilder).andReturn().getResponse().getHeader("Location"),
        is("http://localhost/realms/domaine1/applications/monApplication/groups/Group1"));
  }

  // Test response codes on error
  @Test
  public void get401OnCreateGroupWhenNotAuhtenticated() throws Exception {

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post("/realms/domaine1/applications/monApplication/groups")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(group1))
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());

    assertThat(
        "Should respond 401",
        mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
        is(401));
  }

  @Test
  public void get401OnDeleteGroupWhenNotAuhtenticated() throws Exception {

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.delete(
                "/realms/domaine1/applications/monApplication/groups/supprimemoi")
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());

    assertThat(
        "Should respond 401",
        mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
        is(401));
  }

  @Test
  public void get401OnUpdateGroupWhenNotAuhtenticated() throws Exception {

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.put("/realms/domaine1/application/monApplication/groups/Group1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(group1))
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());

    assertThat(
        "Should respond 401",
        mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
        is(401));
  }

  @Test
  @WithMockUser
  public void get409WhenCreatingAlreadyExistingGroup() throws Exception {

    Mockito.when(
            groupService.create(
                Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any()))
        .thenThrow(new GroupAlreadyExistException(""));

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post("/realms/domaine1/applications/monApplication/groups")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(group1))
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());

    assertThat(
        "Should respond 409",
        mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
        is(409));
  }

  @Test
  @WithMockUser
  public void get404WhenNoGroupIsFoundWhenGetById() throws Exception {

    Mockito.when(groupService.findById("domaine1", "monApplication", "dontexist"))
        .thenThrow(new GroupNotFoundException("domaine1", "monApplication", "dontexist"));

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/domaine1/applications/monApplication/groups/dontexist")
            .accept(MediaType.APPLICATION_JSON);
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();

    assertThat("Should respond 404", response.getStatus(), is(404));
    assertThat(
        "Body should be error view",
        objectMapper.readValue(response.getContentAsString(), ErrorView.class).getMessage(),
        is("Group dontexist of application monApplication does not exist in realm domaine1"));
  }

  @Test
  @WithMockUser
  public void get400WhenGroupIdDoesntMatchBody() throws Exception {

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.put("/realms/domaine1/applications/monApplication/groups/dontexist")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(group1))
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());

    assertThat(
        "Should respond 400",
        mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
        is(400));
  }

  @Test
  @WithMockUser
  public void get404WhenNoGroupIsFoundWhenUpdate() throws Exception {

    Mockito.doThrow(new GroupNotFoundException("domaine1", "monApplication", "Group1"))
        .when(groupService)
        .update(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any());
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.put("/realms/domaine1/applications/monApplication/groups/Group1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(group1))
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();

    assertThat("Should respond 404", response.getStatus(), is(404));
    assertThat(
        "Response should be an ErrorView",
        objectMapper.readValue(response.getContentAsString(), ErrorView.class).getMessage(),
        is("Group Group1 of application monApplication does not exist in realm domaine1"));
  }

  @Test
  @WithMockUser
  public void get404WhenNoGroupIsFoundWhenDelete() throws Exception {

    Mockito.doThrow(new GroupNotFoundException("domaine1", "monApplication", "dontexist"))
        .when(groupService)
        .delete(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any());

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.delete(
                "/realms/domaine1/applications/monApplication/groups/dontexist")
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();

    assertThat("Should respond 404", response.getStatus(), is(404));
    assertThat(
        "Response should be an ErrorView",
        objectMapper.readValue(response.getContentAsString(), ErrorView.class).getMessage(),
        is("Group dontexist of application monApplication does not exist in realm domaine1"));
  }
}
