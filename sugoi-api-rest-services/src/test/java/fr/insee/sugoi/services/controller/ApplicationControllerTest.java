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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.service.ApplicationService;
import fr.insee.sugoi.model.Application;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootTest(
    classes = ApplicationController.class,
    properties = "spring.config.location=classpath:/controller/application.properties")
@AutoConfigureMockMvc
@EnableWebMvc
public class ApplicationControllerTest {

  @Autowired MockMvc mockMvc;

  @MockBean private ApplicationService applicationService;

  ObjectMapper objectMapper = new ObjectMapper();
  Application application1, application2;
  PageResult<Application> pageResult;

  @BeforeEach
  public void setUp() {
    application1 = new Application();
    application1.setName("SuperAppli");
    application1.setOwner("Amoi");
    application2 = new Application();
    application2.setName("SuperAppli2");
    application2.setOwner("Amoi2");
    List<Application> applications = new ArrayList<>();
    applications.add(application1);
    applications.add(application2);
    pageResult = new PageResult<Application>();
    pageResult.setResults(applications);
  }

  @Test
  @WithMockUser
  public void retrieveAllApplicationsWithoutStorage() {

    Mockito.when(
            applicationService.findByProperties(
                Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(pageResult);

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/toto/applications").accept(MediaType.APPLICATION_JSON);
    try {
      String jsonResult =
          mockMvc.perform(requestBuilder).andReturn().getResponse().getContentAsString();
      TypeReference<PageResult<Application>> mapType =
          new TypeReference<PageResult<Application>>() {};
      PageResult<Application> appRes = objectMapper.readValue(jsonResult, mapType);
      assertThat(
          "First element should be SuperAppli",
          appRes.getResults().get(0).getName(),
          is("SuperAppli"));
      assertThat(
          "SuperAppli should have owner Amoi", appRes.getResults().get(0).getOwner(), is("Amoi"));
      assertThat(
          "Second element should be SuperAppli2",
          appRes.getResults().get(1).getName(),
          is("SuperAppli2"));
      assertThat(
          "SuperAppli2 should have owner Amoi2",
          appRes.getResults().get(1).getOwner(),
          is("Amoi2"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      fail();
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  @WithMockUser
  public void getNextLocationOfSearchTest() {
    pageResult.setHasMoreResult(true);

    Mockito.when(
            applicationService.findByProperties(
                Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(pageResult);
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/toto/applications?size=2").accept(MediaType.APPLICATION_JSON);

    try {
      assertThat(
          "Location header gives next page",
          mockMvc.perform(requestBuilder).andReturn().getResponse().getHeader("Location"),
          is("http://localhost/toto/applications?size=2&offset=2"));
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  @WithMockUser
  public void getRetrieveObjectLocationWhenApplicationCreation() {
    Mockito.when(
            applicationService.findById(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
        .thenReturn(null);
    Mockito.when(applicationService.create(Mockito.anyString(), Mockito.any(), Mockito.anyString()))
        .thenReturn(application1);
    RequestBuilder requestBuilder;
    try {
      requestBuilder =
          MockMvcRequestBuilders.post("/toto/applications")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(application1))
              .accept(MediaType.APPLICATION_JSON)
              .with(csrf());
      assertThat(
          "Location header gives get uri",
          mockMvc.perform(requestBuilder).andReturn().getResponse().getHeader("Location"),
          is("http://localhost/toto/applications/SuperAppli"));

    } catch (Exception e1) {
      e1.printStackTrace();
    }
  }
}
