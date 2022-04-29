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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.commons.services.configuration.SecurityConfiguration;
import fr.insee.sugoi.commons.services.controller.technics.SugoiAdviceController;
import fr.insee.sugoi.core.exceptions.ApplicationAlreadyExistException;
import fr.insee.sugoi.core.exceptions.ApplicationNotFoundException;
import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.model.ProviderResponse.ProviderResponseStatus;
import fr.insee.sugoi.core.service.ApplicationService;
import fr.insee.sugoi.core.service.impl.PermissionServiceImpl;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import fr.insee.sugoi.services.decider.AuthorizeMethodDecider;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    classes = {
      ApplicationController.class,
      SugoiAdviceController.class,
      AuthorizeMethodDecider.class,
      PermissionServiceImpl.class,
      SecurityConfiguration.class
    },
    properties = "spring.config.location=classpath:/controller/application.properties")
@AutoConfigureMockMvc
@EnableWebMvc
public class ApplicationControllerTest {

  @Autowired MockMvc mockMvc;

  @MockBean private ApplicationService applicationService;

  ObjectMapper objectMapper = new ObjectMapper();

  class ApplicationMatcher implements ArgumentMatcher<Application> {

    private String name;

    public ApplicationMatcher(String name) {
      this.name = name;
    }

    @Override
    public boolean matches(Application application) {
      return application.getName() != null && application.getName().equals(name);
    }
  }

  class PageableResultMatcher implements ArgumentMatcher<PageableResult> {

    private int size;

    public PageableResultMatcher(int size) {
      this.size = size;
    }

    @Override
    public boolean matches(PageableResult pageableResult) {
      return pageableResult.getSize() == size;
    }
  }

  @BeforeEach
  public void setup() {

    Application application1 = new Application();
    application1.setName("SuperAppli");
    application1.setOwner("Amoi");

    Mockito.when(applicationService.findById("domaine1", "SuperAppli")).thenReturn(application1);
    Mockito.when(applicationService.findById("domaine1", "dontexist"))
        .thenThrow(ApplicationNotFoundException.class);

    Application application2 = new Application();
    application2.setName("SuperAppli2");
    application2.setOwner("Amoi2");

    Application application3 = new Application();
    application3.setName("Application3");

    List<Application> applications = List.of(application1, application2, application3);
    PageResult<Application> pageResult = new PageResult<Application>();
    pageResult.setResults(applications);

    Mockito.when(
            applicationService.findByProperties(
                Mockito.eq("domaine1"),
                Mockito.any(Application.class),
                Mockito.any(PageableResult.class)))
        .thenReturn(pageResult);

    PageResult<Application> pageResultMoreResults = new PageResult<Application>();
    pageResultMoreResults.setSearchToken("mySearchToken");
    pageResultMoreResults.setResults(applications);
    pageResultMoreResults.setHasMoreResult(true);

    Mockito.when(
            applicationService.findByProperties(
                Mockito.eq("domaine1"),
                Mockito.any(Application.class),
                Mockito.argThat(new PageableResultMatcher(2))))
        .thenReturn(pageResultMoreResults);

    PageResult<Application> onlyAppli2PageResult = new PageResult<>();
    onlyAppli2PageResult.setResults(List.of(application2));

    Mockito.when(
            applicationService.findByProperties(
                Mockito.eq("domaine1"),
                Mockito.argThat(new ApplicationMatcher("Appli2")),
                Mockito.any(PageableResult.class)))
        .thenReturn(onlyAppli2PageResult);

    Application newAppli = new Application();
    newAppli.setName("NewAppli");

    Mockito.doReturn(
            new ProviderResponse(
                "NewAppli", "requestId", ProviderResponseStatus.OK, newAppli, null))
        .when(applicationService)
        .create(
            Mockito.eq("domaine1"),
            Mockito.argThat(new ApplicationMatcher("NewAppli")),
            Mockito.any(ProviderRequest.class));

    Mockito.when(
            applicationService.create(
                Mockito.anyString(),
                Mockito.argThat(new ApplicationMatcher("ExistingApp")),
                Mockito.any()))
        .thenThrow(
            new ApplicationAlreadyExistException(
                "Application ExistingApp already exist in realm domaine1"));

    Mockito.doReturn(new ProviderResponse("", "requestId", ProviderResponseStatus.OK, null, null))
        .when(applicationService)
        .delete(
            Mockito.eq("domaine1"), Mockito.eq("supprimemoi"), Mockito.any(ProviderRequest.class));

    Mockito.doThrow(new ApplicationNotFoundException("domaine1", "dontexist"))
        .when(applicationService)
        .delete(Mockito.anyString(), Mockito.anyString(), Mockito.any());

    Application applicationToUpdate = new Application();
    applicationToUpdate.setName("ToUpdate");
    applicationToUpdate.setOwner("OldOwner");
    Application applicationUpdated = new Application();
    applicationUpdated.setName("ToUpdate");
    applicationUpdated.setOwner("NewOwner");

    Mockito.when(applicationService.findById("domaine1", "ToUpdate"))
        .thenReturn(applicationToUpdate)
        .thenReturn(applicationUpdated);
    Mockito.doReturn(
            new ProviderResponse(
                "", "requestId", ProviderResponseStatus.OK, applicationUpdated, null))
        .when(applicationService)
        .update(
            Mockito.eq("domaine1"),
            Mockito.argThat(new ApplicationMatcher("ToUpdate")),
            Mockito.any(ProviderRequest.class));

    Mockito.doThrow(new ApplicationNotFoundException("domaine1", "dontexist"))
        .when(applicationService)
        .update(
            Mockito.eq("domaine1"),
            Mockito.argThat(new ApplicationMatcher("dontexist")),
            Mockito.any());
  }

  // Test on search applications

  @Test
  @WithMockUser(roles = "SUGOI_ADMIN")
  public void searchAllApplications() throws Exception {

    MockHttpServletResponse response = searchAllApplicationOnDomaine1();
    PageResult<Application> appRes = convertResponseToAppPageResult(response);

    assertThat("Response code should be 200", response.getStatus(), is(200));
    assertThat(
        "Retrieving all applications should retrieve SuperAppli",
        appRes.getResults().stream().anyMatch(app -> app.getName().equalsIgnoreCase("SuperAppli")));
    assertThat(
        "The application SuperAppli should have its own data",
        appRes.getResults().stream()
            .filter(app -> app.getName().equalsIgnoreCase("SuperAppli"))
            .findFirst()
            .get()
            .getOwner(),
        is("Amoi"));
    assertThat(
        "Retrieving all applications should retrieve SuperAppli2",
        appRes.getResults().stream().anyMatch(app -> app.getName().equalsIgnoreCase("SuperAppli")));
    assertThat(
        "The application SuperAppli2 should have its own data",
        appRes.getResults().stream()
            .filter(app -> app.getName().equalsIgnoreCase("SuperAppli2"))
            .findFirst()
            .get()
            .getOwner(),
        is("Amoi2"));
  }

  @Test
  @WithMockUser(roles = "SUGOI_ADMIN")
  public void searchApplicationsByNameCriteria() throws Exception {

    PageResult<Application> appRes =
        convertResponseToAppPageResult(searchApplicationOnDomaine1ByName("Appli2"));

    assertThat("Only contains one application SuperAppli2", appRes.getResults().size(), is(1));
    assertThat(
        "Only element should be SuperAppli2",
        appRes.getResults().get(0).getName(),
        is("SuperAppli2"));
  }

  @Test
  @WithMockUser(roles = "SUGOI_ADMIN")
  @DisplayName(
      "Given there is more resuls than the number of result required, "
          + "then the location header should give the next page location")
  public void getNextLocationInSearchResponse() throws Exception {
    assertThat(
        "Location header gives next page",
        searchApplicationOnDomaine1Uncomplete().getHeader("Location"),
        is("http://localhost/realms/domaine1/applications?size=2&searchToken=mySearchToken"));
  }

  // tests on get application

  @Test
  @WithMockUser(roles = "SUGOI_ADMIN")
  public void getApplicationById() throws Exception {

    Application res = convertResponseToApplication(getApplicationOnDomaine1ByName("SuperAppli"));

    verify(applicationService).findById("domaine1", "SuperAppli");
    assertThat("Application returned should be SuperAppli", res.getName(), is("SuperAppli"));
    assertThat("Application returned should be owned by Amoi", res.getOwner(), is("Amoi"));
  }

  @Test
  @WithMockUser(roles = "SUGOI_ADMIN")
  public void get404WhenNoApplicationIsFoundWhenGetById() throws Exception {
    assertThat(
        "Should respond 404", getApplicationOnDomaine1ByName("dontexist").getStatus(), is(404));
  }

  // Test create applications

  @Test
  @WithMockUser(roles = "SUGOI_ADMIN")
  public void postShouldCallPostServiceAndReturnNewApp() throws Exception {

    Application application = new Application();
    application.setName("NewAppli");
    MockHttpServletResponse response = createApplicationOnDomaine1(application);
    verify(applicationService)
        .create(
            Mockito.eq("domaine1"),
            Mockito.argThat(new ApplicationMatcher("NewAppli")),
            Mockito.any(ProviderRequest.class));
    assertThat(
        "Should get new application",
        objectMapper.readValue(response.getContentAsString(), Application.class).getName(),
        is("NewAppli"));
  }

  @Test
  @WithMockUser(roles = "SUGOI_ADMIN")
  public void getObjectLocationInApplicationCreationResponse() throws Exception {
    Application application = new Application();
    application.setName("NewAppli");
    assertThat(
        "Location header gives get uri",
        createApplicationOnDomaine1(application).getHeader("Location"),
        is("http://localhost/realms/domaine1/applications/NewAppli"));
  }

  @Test
  @WithMockUser(roles = "SUGOI_ADMIN")
  public void get409WhenCreatingAlreadyExistingApplication() throws Exception {
    Application application = new Application();
    application.setName("ExistingApp");
    assertThat("Should respond 409", createApplicationOnDomaine1(application).getStatus(), is(409));
  }

  // Test delete application

  @Test
  @WithMockUser(roles = "SUGOI_ADMIN")
  public void deleteShouldCallDeleteService() throws Exception {

    deleteApplicationOnDomaine1("supprimemoi");
    verify(applicationService)
        .delete(
            Mockito.eq("domaine1"), Mockito.eq("supprimemoi"), Mockito.any(ProviderRequest.class));
  }

  @Test
  @WithMockUser(roles = "SUGOI_ADMIN")
  public void get404WhenNoApplicationIsFoundWhenDelete() throws Exception {

    assertThat(
        "Should respond 404", deleteApplicationOnDomaine1("supprimemoi").getStatus(), is(404));
  }

  // Test update application

  @Test
  @WithMockUser(roles = "SUGOI_ADMIN")
  public void updateShouldCallUpdateServiceAndReturnNewApp() throws Exception {

    Application toUpdate = new Application();
    toUpdate.setName("ToUpdate");
    toUpdate.setOwner("OldOwner");

    MockHttpServletResponse response = updateApplicationOnDomaine1("ToUpdate", toUpdate);

    verify(applicationService)
        .update(
            Mockito.eq("domaine1"),
            Mockito.argThat(new ApplicationMatcher("ToUpdate")),
            Mockito.any(ProviderRequest.class));
    assertThat(
        "Should get updated application",
        convertResponseToApplication(response).getOwner(),
        is("NewOwner"));

    assertThat(
        "Should get location",
        response.getHeader("Location"),
        is("http://localhost/realms/domaine1/applications/ToUpdate"));
  }

  @Test
  @WithMockUser(roles = "SUGOI_ADMIN")
  public void get400WhenApplicationIdDoesntMatchBody() throws Exception {
    Application toUpdateAppli = new Application();
    toUpdateAppli.setName("toUpdate");
    assertThat(
        "Should respond 400",
        updateApplicationOnDomaine1("Toto", toUpdateAppli).getStatus(),
        is(400));
  }

  @Test
  @WithMockUser(roles = "SUGOI_ADMIN")
  public void get404WhenNoApplicationIsFoundWhenUpdate() throws Exception {
    Application dontexist = new Application();
    dontexist.setName("dontexist");
    assertThat(
        "Should respond 404",
        updateApplicationOnDomaine1("dontexist", dontexist).getStatus(),
        is(404));
  }

  // tests on rights

  @Test
  @WithMockUser(roles = "SUGOI_domaine2_Reader")
  public void badRealmShouldNotBeAuthorized() throws Exception {
    assertThat(
        "should not be authorized",
        getApplicationOnDomaine1ByName("SuperAppli").getStatus(),
        is(403));
  }

  @Test
  @WithMockUser(roles = "SUGOI_domaine1_Reader")
  public void readerOnGoodRealmShouldBeAuthorized() throws Exception {
    assertThat(
        "A reader on the entire realm should read the applications",
        getApplicationOnDomaine1ByName("SuperAppli").getStatus(),
        is(200));
  }

  @Test
  @WithMockUser(roles = "SUGOI_domaine1_$(userStorage)_Reader")
  public void readerOnSubUSShouldNotBeAuthorized() throws Exception {
    assertThat(
        "User having rights to read on a sub userstorage should not be able to read applications",
        getApplicationOnDomaine1ByName("SuperAppli").getStatus(),
        is(403));
  }

  @Test
  @WithMockUser(roles = "SUGOI_domaine1_$(userStorage)_Writer")
  public void writerOnSubUSShouldNotBeAuthorized() throws Exception {
    assertThat(
        "User having rights to write on a sub userstorage should not be able to modify applications",
        getApplicationOnDomaine1ByName("SuperAppli").getStatus(),
        is(403));
  }

  @Test
  @WithMockUser(roles = {"ASI_ELSE", "SUGOI_domaine1_domaine1_Reader"})
  public void appManagerOnBadAppShouldNotBeAuthorized() throws Exception {
    Application newApp = new Application();
    newApp.setName("toto");
    assertThat(
        "User having write on application Else should not have rights on application toto",
        updateApplicationOnDomaine1("toto", newApp).getStatus(),
        is(403));
  }

  private MockHttpServletResponse searchAllApplicationOnDomaine1() throws Exception {
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/domaine1/applications")
            .accept(MediaType.APPLICATION_JSON);
    return mockMvc.perform(requestBuilder).andReturn().getResponse();
  }

  private MockHttpServletResponse searchApplicationOnDomaine1ByName(String name) throws Exception {
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/domaine1/applications")
            .param("name", name)
            .accept(MediaType.APPLICATION_JSON);
    return mockMvc.perform(requestBuilder).andReturn().getResponse();
  }

  private MockHttpServletResponse searchApplicationOnDomaine1Uncomplete() throws Exception {
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/domaine1/applications?size=2")
            .accept(MediaType.APPLICATION_JSON);
    return mockMvc.perform(requestBuilder).andReturn().getResponse();
  }

  private MockHttpServletResponse getApplicationOnDomaine1ByName(String name) throws Exception {
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/domaine1/applications/" + name)
            .accept(MediaType.APPLICATION_JSON);
    return mockMvc.perform(requestBuilder).andReturn().getResponse();
  }

  private MockHttpServletResponse createApplicationOnDomaine1(Application application)
      throws Exception {
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.post("/realms/domaine1/applications")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(application))
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());
    return mockMvc.perform(requestBuilder).andReturn().getResponse();
  }

  private MockHttpServletResponse deleteApplicationOnDomaine1(String applicationName)
      throws Exception {
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.delete("/realms/domaine1/applications/supprimemoi")
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());
    return mockMvc.perform(requestBuilder).andReturn().getResponse();
  }
  ;

  private MockHttpServletResponse updateApplicationOnDomaine1(
      String applicationName, Application application) throws Exception {
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.put("/realms/domaine1/applications/" + applicationName)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(application))
            .accept(MediaType.APPLICATION_JSON)
            .with(csrf());
    return mockMvc.perform(requestBuilder).andReturn().getResponse();
  }

  private PageResult<Application> convertResponseToAppPageResult(MockHttpServletResponse response)
      throws JsonMappingException, JsonProcessingException, UnsupportedEncodingException {
    return objectMapper.readValue(
        response.getContentAsString(), new TypeReference<PageResult<Application>>() {});
  }

  private Application convertResponseToApplication(MockHttpServletResponse response)
      throws Exception {
    return objectMapper.readValue(response.getContentAsString(), Application.class);
  }
}
