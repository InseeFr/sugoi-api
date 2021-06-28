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
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.paging.PageResult;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Then;

public class OrganizationGlue {

  private Scenario scenario;

  private StepData stepData;

  public OrganizationGlue(StepData stepData) {
    this.stepData = stepData;
  }

  @Before
  public void before(Scenario scenario) {
    this.scenario = scenario;
  }

  @Then("the client expect to receive a list of organizations")
  @SuppressWarnings("unchecked")
  public void expect_to_receive_a_list_of_organizations() {
    Boolean isOrga = false;
    ObjectMapper mapper = new ObjectMapper();
    try {
      PageResult<Organization> organizations =
          mapper.readValue(stepData.getLatestResponse().getBody(), PageResult.class);
      stepData.setOrganizations(organizations.getResults());
      isOrga = true;
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    assertThat("Data receive is a list of organizations", isOrga, is(true));
  }

  @Then("the client expect to receive an organization")
  public void expect_to_receive_a_organization() {
    Boolean isOrga = false;
    ObjectMapper mapper = new ObjectMapper();
    Organization organization;
    try {
      organization = mapper.readValue(stepData.getLatestResponse().getBody(), Organization.class);
      stepData.setOrganization(organization);
      isOrga = true;
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    assertThat("Data receive is an organization", isOrga, is(true));
  }

  @Then("the client expect the identifiant of organization to be {}")
  public void expect_organizationname_of_organization_to_be(String organizationname) {
    assertThat(stepData.getUser().getUsername(), is(organizationname));
  }

  @Then("the client want to see the organization list")
  public void show_list() {
    scenario.log(stepData.getOrganizations().toString());
    System.out.println(stepData.getOrganizations());
  }
}
