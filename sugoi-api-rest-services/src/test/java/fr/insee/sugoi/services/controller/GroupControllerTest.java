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
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.service.GroupService;
import fr.insee.sugoi.model.Group;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
    classes = GroupController.class,
    properties = "spring.config.location=classpath:/controller/application.properties")
@AutoConfigureMockMvc
@EnableWebMvc
public class GroupControllerTest {

  @Autowired MockMvc mockMvc;

  @MockBean private GroupService groupService;

  ObjectMapper objectMapper = new ObjectMapper();
  Group group1, group2, group2Updated;
  PageResult<Group> pageResult;

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
  }

  // Test read requests on good query

  @Test
  @WithMockUser
  public void retrieveAllGroups() {
    try {

      Mockito.when(
              groupService.findByProperties(
                  Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any()))
          .thenReturn(pageResult);

      RequestBuilder requestBuilder =
          MockMvcRequestBuilders.get("/realms/domaine1/groups")
              .param("application", "monApplication")
              .accept(MediaType.APPLICATION_JSON);
      MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();
      TypeReference<PageResult<Group>> mapType = new TypeReference<PageResult<Group>>() {};
      PageResult<Group> appRes = objectMapper.readValue(response.getContentAsString(), mapType);

      assertThat(
          "First element should be Group1", appRes.getResults().get(0).getName(), is("Group1"));
      assertThat(
          "Second element should be Group2", appRes.getResults().get(1).getName(), is("Group2"));
      assertThat("Response code should be 200", response.getStatus(), is(200));

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Disabled
  @Test
  @WithMockUser
  public void shouldRetrieveSomeGroups() {}

  @Test
  @WithMockUser
  public void shouldGetGroupByID() {
    try {

      Mockito.when(groupService.findById("domaine1", "monApplication", "Group1"))
          .thenReturn(group1);

      RequestBuilder requestBuilder =
          MockMvcRequestBuilders.get("/realms/domaine1/groups/Group1")
              .param("application", "monApplication")
              .accept(MediaType.APPLICATION_JSON);

      MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();
      Group res = objectMapper.readValue(response.getContentAsString(), Group.class);

      verify(groupService).findById("domaine1", "monApplication", "Group1");
      assertThat("Group returned should be Group1", res.getName(), is("Group1"));

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  // Test write requests

  @Test
  @WithMockUser
  public void deleteShouldCallDeleteService() {
    try {

      Mockito.when(groupService.findById("domaine1", "monApplication", "supprimemoi"))
          .thenReturn(group1);

      RequestBuilder requestBuilder =
          MockMvcRequestBuilders.delete("/realms/domaine1/groups/supprimemoi")
              .accept(MediaType.APPLICATION_JSON)
              .param("application", "monApplication")
              .with(csrf());

      mockMvc.perform(requestBuilder).andReturn();
      verify(groupService).delete("domaine1", "monApplication", "supprimemoi");

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  @WithMockUser
  public void updateShouldCallUpdateServiceAndReturnNewApp() {
    try {

      Mockito.when(groupService.findById("domaine1", "monApplication", "Group2"))
          .thenReturn(group2)
          .thenReturn(group2Updated);

      RequestBuilder requestBuilder =
          MockMvcRequestBuilders.put("/realms/domaine1/groups/Group2")
              .param("application", "monApplication")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(group2Updated))
              .accept(MediaType.APPLICATION_JSON)
              .with(csrf());

      MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();

      verify(groupService).update(Mockito.anyString(), Mockito.anyString(), Mockito.any());
      assertThat(
          "Should get updated group",
          objectMapper.readValue(response.getContentAsString(), Group.class).getDescription(),
          is("groupe pas fou"));

      assertThat(
          "Should get location",
          response.getHeader("Location"),
          is("http://localhost/realms/domaine1/groups/Group2"));

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  @WithMockUser
  public void postShouldCallPostServiceAndReturnNewApp() {

    try {
      Mockito.when(groupService.findById("domaine1", "monApplication", "Group1"))
          .thenReturn(null)
          .thenReturn(group1);

      RequestBuilder requestBuilder =
          MockMvcRequestBuilders.post("/realms/domaine1/groups")
              .param("application", "monApplication")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(group1))
              .accept(MediaType.APPLICATION_JSON)
              .with(csrf());

      MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();
      verify(groupService).create(Mockito.anyString(), Mockito.anyString(), Mockito.any());
      assertThat(
          "Should get new group",
          objectMapper.readValue(response.getContentAsString(), Group.class).getName(),
          is("Group1"));

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  // Test location headers
  @Test
  @WithMockUser
  public void getNextLocationInSearchResponse() {
    try {

      pageResult.setHasMoreResult(true);

      Mockito.when(
              groupService.findByProperties(
                  Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any()))
          .thenReturn(pageResult);
      RequestBuilder requestBuilder =
          MockMvcRequestBuilders.get("/realms/domaine1/groups?size=2")
              .param("application", "monApplication")
              .accept(MediaType.APPLICATION_JSON);

      assertThat(
          "Location header gives next page",
          mockMvc.perform(requestBuilder).andReturn().getResponse().getHeader("Location"),
          is("http://localhost/realms/domaine1/groups?size=2&offset=2"));

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  @WithMockUser
  public void getObjectLocationInGroupCreationResponse() {
    try {

      Mockito.when(groupService.findById("domaine1", "monApplication", "Group1")).thenReturn(null);
      Mockito.when(groupService.create(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
          .thenReturn(group1);

      RequestBuilder requestBuilder =
          MockMvcRequestBuilders.post("/realms/domaine1/groups")
              .param("application", "monApplication")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(group1))
              .accept(MediaType.APPLICATION_JSON)
              .with(csrf());

      assertThat(
          "Location header gives get uri",
          mockMvc.perform(requestBuilder).andReturn().getResponse().getHeader("Location"),
          is("http://localhost/realms/domaine1/groups/Group1"));

    } catch (Exception e1) {
      e1.printStackTrace();
      fail();
    }
  }

  // Test response codes on error
  @Test
  public void get401OnCreateGroupWhenNotAuhtenticated() {
    try {

      RequestBuilder requestBuilder =
          MockMvcRequestBuilders.post("/realms/domaine1/groups")
              .param("application", "monApplication")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(group1))
              .accept(MediaType.APPLICATION_JSON)
              .with(csrf());

      assertThat(
          "Should respond 401",
          mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
          is(401));

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void get401OnDeleteGroupWhenNotAuhtenticated() {
    try {

      RequestBuilder requestBuilder =
          MockMvcRequestBuilders.delete("/realms/domaine1/groups/supprimemoi")
              .param("application", "monApplication")
              .accept(MediaType.APPLICATION_JSON)
              .with(csrf());

      assertThat(
          "Should respond 401",
          mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
          is(401));

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void get401OnUpdateGroupWhenNotAuhtenticated() {
    try {

      RequestBuilder requestBuilder =
          MockMvcRequestBuilders.put("/realms/domaine1/groups/Group1")
              .param("application", "monApplication")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(group1))
              .accept(MediaType.APPLICATION_JSON)
              .with(csrf());

      assertThat(
          "Should respond 401",
          mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
          is(401));

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  @WithMockUser
  public void get409WhenCreatingAlreadyExistingGroup() {
    try {

      Mockito.when(groupService.findById("domaine1", "monApplication", "Group1"))
          .thenReturn(group1);

      RequestBuilder requestBuilder =
          MockMvcRequestBuilders.post("/realms/domaine1/groups")
              .param("application", "monApplication")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(group1))
              .accept(MediaType.APPLICATION_JSON)
              .with(csrf());

      assertThat(
          "Should respond 409",
          mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
          is(409));

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  @WithMockUser
  public void get404WhenNoGroupIsFoundWhenGetById() {
    try {

      Mockito.when(groupService.findById("domaine1", "monApplication", "dontexist"))
          .thenReturn(null);

      RequestBuilder requestBuilder =
          MockMvcRequestBuilders.get("/realms/domaine1/groups/dontexist")
              .param("application", "monApplication")
              .accept(MediaType.APPLICATION_JSON);

      assertThat(
          "Should respond 404",
          mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
          is(404));

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  @WithMockUser
  public void get400WhenNoGroupIdDoesntMatchBody() {
    try {

      RequestBuilder requestBuilder =
          MockMvcRequestBuilders.put("/realms/domaine1/groups/dontexist")
              .param("application", "monApplication")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(group1))
              .accept(MediaType.APPLICATION_JSON)
              .with(csrf());

      assertThat(
          "Should respond 404",
          mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
          is(400));

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  @WithMockUser
  public void get404WhenNoGroupIsFoundWhenUpdate() {
    try {

      Mockito.when(groupService.findById("domaine1", "monApplication", "Group1")).thenReturn(null);

      RequestBuilder requestBuilder =
          MockMvcRequestBuilders.put("/realms/domaine1/groups/Group1")
              .param("application", "monApplication")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(group1))
              .accept(MediaType.APPLICATION_JSON)
              .with(csrf());

      assertThat(
          "Should respond 404",
          mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
          is(404));

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  @WithMockUser
  public void get404WhenNoGroupIsFoundWhenDelete() {
    try {

      Mockito.when(groupService.findById("domaine1", "monApplication", "Group1")).thenReturn(null);

      RequestBuilder requestBuilder =
          MockMvcRequestBuilders.delete("/realms/domaine1/groups/dontexist")
              .param("application", "monApplication")
              .accept(MediaType.APPLICATION_JSON)
              .with(csrf());

      assertThat(
          "Should respond 404",
          mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
          is(404));

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }
}
