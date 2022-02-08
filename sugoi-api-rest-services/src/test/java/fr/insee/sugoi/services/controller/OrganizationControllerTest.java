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
import fr.insee.sugoi.core.exceptions.OrganizationAlreadyExistException;
import fr.insee.sugoi.core.exceptions.OrganizationNotFoundException;
import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.model.ProviderResponse.ProviderResponseStatus;
import fr.insee.sugoi.core.service.OrganizationService;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import fr.insee.sugoi.model.paging.SearchType;
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
    classes = {OrganizationController.class, SugoiAdviceController.class},
    properties = "spring.config.location=classpath:/controller/application.properties")
@AutoConfigureMockMvc
@EnableWebMvc
public class OrganizationControllerTest {

  @Autowired MockMvc mockMvc;

  @MockBean private OrganizationService organizationService;

  ObjectMapper objectMapper = new ObjectMapper();
  Organization organization1, organization2, organization2Updated;
  PageResult<Organization> pageResult;

  class OrganizationMatcher implements ArgumentMatcher<Organization> {

    private String id;

    public OrganizationMatcher(String id) {
      this.id = id;
    }

    @Override
    public boolean matches(Organization organization) {
      return organization.getIdentifiant().equals(id);
    }
  }

  @BeforeEach
  public void setup() {
    organization1 = new Organization();
    organization1.setIdentifiant("BigOrga");

    organization2 = new Organization();
    organization2.setIdentifiant("SimpleOrga");

    organization2Updated = new Organization();
    organization2Updated.setIdentifiant("SimpleOrga");
    organization2Updated.setOrganization(organization1);

    List<Organization> organizations = new ArrayList<>();
    organizations.add(organization1);
    organizations.add(organization2);
    pageResult = new PageResult<Organization>();
    pageResult.setResults(organizations);
    pageResult.setSearchToken("mySearchToken");
  }

  // Test read requests on good query

  @Test
  @WithMockUser
  public void retrieveAllOrganizations() throws Exception {

    Mockito.when(
            organizationService.findByProperties(
                Mockito.eq("domaine1"),
                Mockito.isNull(),
                Mockito.any(Organization.class),
                Mockito.any(PageableResult.class),
                Mockito.any(SearchType.class)))
        .thenReturn(pageResult);

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/domaine1/organizations")
            .accept(MediaType.APPLICATION_JSON);
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();
    TypeReference<PageResult<Organization>> mapType =
        new TypeReference<PageResult<Organization>>() {};
    PageResult<Organization> appRes =
        objectMapper.readValue(response.getContentAsString(), mapType);

    assertThat(
        "First element should be BigOrga",
        appRes.getResults().get(0).getIdentifiant(),
        is("BigOrga"));
    assertThat(
        "Second element should be SimpleOrga",
        appRes.getResults().get(1).getIdentifiant(),
        is("SimpleOrga"));
    assertThat("Response code should be 200", response.getStatus(), is(200));
  }

  @Test
  @WithMockUser
  public void shouldRetrieveSomeOrganizations() throws Exception {

    PageResult<Organization> onlySimpleOrgaPageResult = new PageResult<>();
    onlySimpleOrgaPageResult.setResults(List.of(organization1));

    Mockito.when(
            organizationService.findByProperties(
                Mockito.eq("domaine1"),
                Mockito.isNull(),
                Mockito.argThat(new OrganizationMatcher("Simple")),
                Mockito.any(PageableResult.class),
                Mockito.any(SearchType.class)))
        .thenReturn(onlySimpleOrgaPageResult);

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/domaine1/organizations")
            .param("identifiant", "Simple")
            .accept(MediaType.APPLICATION_JSON);
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();
    TypeReference<PageResult<Organization>> mapType =
        new TypeReference<PageResult<Organization>>() {};
    PageResult<Organization> appRes =
        objectMapper.readValue(response.getContentAsString(), mapType);

    assertThat("Should have one result", appRes.getResults().size(), is(1));
    assertThat("Response code should be 200", response.getStatus(), is(200));
  }

  @Test
  @WithMockUser
  public void shouldGetOrganizationByID() throws Exception {

    Mockito.when(organizationService.findById("domaine1", null, "BigOrga"))
        .thenReturn(organization1);

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/domaine1/organizations/BigOrga")
            .accept(MediaType.APPLICATION_JSON);

    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();
    Organization res = objectMapper.readValue(response.getContentAsString(), Organization.class);

    verify(organizationService).findById("domaine1", null, "BigOrga");
    assertThat("Organization returned should be BigOrga", res.getIdentifiant(), is("BigOrga"));
  }

  // Test write requests

  @Test
  @WithMockUser
  public void deleteShouldCallDeleteService() throws Exception {

    Mockito.when(
            organizationService.delete(
                Mockito.eq("domaine1"),
                Mockito.eq("toto"),
                Mockito.eq("supprimemoi"),
                Mockito.any(ProviderRequest.class)))
        .thenReturn(new ProviderResponse("", "requestId", ProviderResponseStatus.OK, null, null));
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.delete("/realms/domaine1/storages/toto/organizations/supprimemoi")
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());

    mockMvc.perform(requestBuilder).andReturn();
    verify(organizationService)
        .delete(
            Mockito.eq("domaine1"),
            Mockito.eq("toto"),
            Mockito.eq("supprimemoi"),
            Mockito.any(ProviderRequest.class));
  }

  @Test
  @WithMockUser
  public void updateShouldCallUpdateServiceAndReturnNewApp() throws Exception {

    Mockito.when(organizationService.findById("domaine1", null, "SimpleOrga"))
        .thenReturn(organization2)
        .thenReturn(organization2Updated);
    Mockito.doReturn(
            new ProviderResponse(
                "", "requestId", ProviderResponseStatus.OK, organization2Updated, null))
        .when(organizationService)
        .update(
            Mockito.eq("domaine1"),
            Mockito.isNull(),
            Mockito.argThat(new OrganizationMatcher("SimpleOrga")),
            Mockito.any(ProviderRequest.class));
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.put("/realms/domaine1/organizations/SimpleOrga")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(organization2Updated))
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());

    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();

    assertThat(
        "Should get updated organization",
        objectMapper
            .readValue(response.getContentAsString(), Organization.class)
            .getOrganization()
            .getIdentifiant(),
        is("BigOrga"));

    assertThat(
        "Should get location",
        response.getHeader("Location"),
        is("http://localhost/realms/domaine1/organizations/SimpleOrga"));
  }

  @Test
  @WithMockUser
  public void postShouldCallPostServiceAndReturnNewApp() throws Exception {

    Mockito.doReturn(
            new ProviderResponse("", "requestId", ProviderResponseStatus.OK, organization1, null))
        .when(organizationService)
        .create(
            Mockito.eq("domaine1"),
            Mockito.eq("Profil_domaine1_WebServiceLdap"),
            Mockito.argThat(new OrganizationMatcher("BigOrga")),
            Mockito.any(ProviderRequest.class));
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post(
                "/realms/domaine1/storages/Profil_domaine1_WebServiceLdap/organizations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(organization1))
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());

    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();
    assertThat(
        "Should get new organization",
        objectMapper.readValue(response.getContentAsString(), Organization.class).getIdentifiant(),
        is("BigOrga"));
  }

  // Test location headers
  @Test
  @WithMockUser
  public void getNextLocationInSearchResponse() throws Exception {

    pageResult.setHasMoreResult(true);

    Mockito.when(
            organizationService.findByProperties(
                Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(pageResult);
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/domaine1/organizations?size=2")
            .accept(MediaType.APPLICATION_JSON);

    assertThat(
        "Location header gives next page",
        mockMvc.perform(requestBuilder).andReturn().getResponse().getHeader("Location"),
        is("http://localhost/realms/domaine1/organizations?size=2&searchToken=mySearchToken"));
  }

  @Test
  @WithMockUser
  public void getObjectLocationInOrganizationCreationResponse() throws Exception {

    Mockito.when(organizationService.findById(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenThrow(OrganizationNotFoundException.class)
        .thenReturn(organization1);
    Mockito.when(
            organizationService.create(
                Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(
            new ProviderResponse(
                organization1.getIdentifiant(), null, ProviderResponseStatus.OK, null, null));

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post(
                "/realms/domaine1/storages/Profil_domaine1_WebServiceLdap/organizations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(organization1))
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());

    assertThat(
        "Location header gives get uri",
        mockMvc.perform(requestBuilder).andReturn().getResponse().getHeader("Location"),
        is(
            "http://localhost/realms/domaine1/storages/Profil_domaine1_WebServiceLdap/organizations/BigOrga"));
  }

  // Test response codes on error
  @Test
  public void get401OnCreateOrganizationWhenNotAuhtenticated() throws Exception {

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post("/realms/domaine1/organizations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(organization1))
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());

    assertThat(
        "Should respond 401",
        mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
        is(401));
  }

  @Test
  public void get401OnDeleteOrganizationWhenNotAuhtenticated() throws Exception {

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.delete("/realms/domaine1/organizations/supprimemoi")
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());

    assertThat(
        "Should respond 401",
        mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
        is(401));
  }

  @Test
  public void get401OnUpdateOrganizationWhenNotAuhtenticated() throws Exception {

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.put("/realms/domaine1/organizations/BigOrga")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(organization1))
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());

    assertThat(
        "Should respond 401",
        mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
        is(401));
  }

  @Test
  @WithMockUser
  public void get409WhenCreatingAlreadyExistingOrganization() throws Exception {

    Mockito.when(
            organizationService.create(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenThrow(new OrganizationAlreadyExistException(""));

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post(
                "/realms/domaine1/storages/Profil_domaine1_WebServiceLdap/organizations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(organization1))
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());

    assertThat(
        "Should respond 409",
        mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
        is(409));
  }

  @Test
  @WithMockUser
  public void get404WhenNoOrganizationIsFoundWhenGetById() throws Exception {

    Mockito.when(organizationService.findById("domaine1", null, "dontexist"))
        .thenThrow(new OrganizationNotFoundException("domaine1", "dontexist"));

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/domaine1/organizations/dontexist")
            .accept(MediaType.APPLICATION_JSON);

    assertThat(
        "Should respond 404",
        mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
        is(404));
  }

  @Test
  @WithMockUser
  public void get400WhenNoOrganizationIdDoesntMatchBody() throws Exception {

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.put("/realms/domaine1/organizations/dontexist")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(organization1))
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());

    assertThat(
        "Should respond 404",
        mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
        is(400));
  }

  @Test
  @WithMockUser
  public void get404WhenNoOrganizationIsFoundWhenUpdate() throws Exception {

    Mockito.doThrow(new OrganizationNotFoundException(""))
        .when(organizationService)
        .update(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any());
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.put("/realms/domaine1/userstorage/us/organizations/BigOrga")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(organization1))
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());

    assertThat(
        "Should respond 404",
        mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
        is(404));
  }

  @Test
  @WithMockUser
  public void get404WhenNoOrganizationIsFoundWhenDelete() throws Exception {

    Mockito.doThrow(new OrganizationNotFoundException(""))
        .when(organizationService)
        .delete(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any());

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.delete("/realms/domaine1/userstorage/us/organizations/dontexist")
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());

    assertThat(
        "Should respond 404",
        mockMvc.perform(requestBuilder).andReturn().getResponse().getStatus(),
        is(404));
  }
}
