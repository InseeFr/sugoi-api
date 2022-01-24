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
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.app.cucumber.utils.StepData;
import fr.insee.sugoi.model.Realm;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Then;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class RealmGlue {

  private Scenario scenario;

  private ObjectMapper mapper = new ObjectMapper();

  @Before
  public void before(Scenario scenario) {
    this.scenario = scenario;
  }

  private StepData stepData;

  public RealmGlue(StepData stepData) {
    this.stepData = stepData;
  }

  @Then("the client expect to have realms access")
  public void haveRealmAccess() {
    Boolean haveRealmAccess = false;
    try {
      List<Realm> realms =
          Arrays.asList(mapper.readValue(stepData.getLatestResponse().getBody(), Realm[].class));
      stepData.setRealms(realms);
      if (realms.size() > 0) {
        haveRealmAccess = true;
      }
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    assertThat(haveRealmAccess, is(true));
  }

  @Then("the client expect description to be {}")
  public void expect_description_of_realm_to_be(String description)
      throws JsonMappingException, JsonProcessingException {
    Realm realm = mapper.readValue(stepData.getLatestResponse().getBody(), Realm.class);
    assertThat(realm.getProperties().get("description"), is(description));
  }

  @Then("the client expect uiUserMapping to contain {}")
  public void expect_uiusermapping_to_contain(String uiMappingProperty)
      throws JsonMappingException, JsonProcessingException {
    Realm realm = mapper.readValue(stepData.getLatestResponse().getBody(), Realm.class);
    assertThat(
        "UiField " + uiMappingProperty + " should be contained in realm",
        realm.getUiMapping().get(Realm.UIMappingType.UI_USER_MAPPING).stream()
            .anyMatch(uifield -> uifield.getName().equalsIgnoreCase(uiMappingProperty)));
  }
}
