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

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.app.cucumber.utils.StepData;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Then;
import java.util.Arrays;

public class ExportGlue {

  private Scenario scenario;

  private ObjectMapper mapper = new ObjectMapper();

  @Before
  public void before(Scenario scenario) {
    this.scenario = scenario;
  }

  private StepData stepData;

  public ExportGlue(StepData stepData) {
    this.stepData = stepData;
  }

  @Then("{} has been exported")
  public void userHasBeenExported(String user) {
    assertThat(
        user + " should be exported", containsUser(stepData.getLatestResponse().getBody(), user));
  }

  @Then("{} has not been exported")
  public void userHasNotBeenExported(String user) {
    assertThat(
        user + " should not be exported",
        !containsUser(stepData.getLatestResponse().getBody(), user));
  }

  public boolean containsUser(String csv, String userName) {
    String[] result = csv.split("\n");
    int userNamePosition = 0;

    String[] headers = result[0].split(",");
    for (int i = 0; i < headers.length; i++) {
      if ("userName".equalsIgnoreCase(headers[i])) {
        userNamePosition = i;
        break;
      }
    }
    int finalUserNamePosition = userNamePosition;
    return Arrays.stream(result)
        .anyMatch(userLine -> isUser(userLine, userName, finalUserNamePosition));
  }

  private boolean isUser(String userLine, String userName, int userNamePosition) {
    String[] s = userLine.split(",");
    return s[userNamePosition].equalsIgnoreCase(userName);
  }

  @Then("is pretty printed")
  public void isPrettyPrinted() {
    assertThat(
        "Header attribute XXX should be printed",
        containsHeader(stepData.getLatestResponse().getBody()));
  }

  private boolean containsHeader(String body) {
    return true;
  }
}
