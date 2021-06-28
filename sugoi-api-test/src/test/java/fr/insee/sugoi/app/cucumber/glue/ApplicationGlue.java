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
package fr.insee.sugoi.app.cucumber.glue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.app.cucumber.utils.StepData;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.paging.PageResult;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Then;

public class ApplicationGlue {
  private Scenario scenario;

  private StepData stepData;

  public ApplicationGlue(StepData stepData) {
    this.stepData = stepData;
  }

  @Before
  public void before(Scenario scenario) {
    this.scenario = scenario;
  }

  @Then("the client expect to receive a list of applications")
  @SuppressWarnings("unchecked")
  public void expect_to_receive_a_list_of_applications() {
    Boolean isApplis = false;
    ObjectMapper mapper = new ObjectMapper();
    try {
      PageResult<Application> applications =
          mapper.readValue(stepData.getLatestResponse().getBody(), PageResult.class);
      stepData.setApplications(applications.getResults());
      isApplis = true;
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    assertThat("Data receive is a list of applications", isApplis, is(true));
  }

  @Then("the client expect to receive an application")
  public void expect_to_receive_an_application() {
    Boolean isAppli = false;
    ObjectMapper mapper = new ObjectMapper();
    Application application;
    try {
      application = mapper.readValue(stepData.getLatestResponse().getBody(), Application.class);
      stepData.setApplication(application);
      isAppli = true;
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    assertThat("Data receive is an application", isAppli, is(true));
  }

  @Then("the client expect the name of application to be {}")
  public void expect_name_of_application_to_be(String appName) {
    assertThat(stepData.getApplication().getName(), is(appName));
  }

  @Then("the client want to see the application list")
  public void show_list() {
    scenario.log(stepData.getApplication().toString());
  }
}
