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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.app.cucumber.utils.StepData;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.paging.PageResult;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Then;
import java.util.ArrayList;
import java.util.List;

public class UserGlue {

  private StepData stepData;

  private Scenario scenario;

  @Before
  public void before(Scenario scenario) {
    this.scenario = scenario;
  }

  public UserGlue(StepData stepData) {
    this.stepData = stepData;
  }

  @Then("the client expect to receive a list of users")
  @SuppressWarnings("unchecked")
  public void expect_to_receive_a_list_of_users() {
    Boolean isUsers = false;
    ObjectMapper mapper = new ObjectMapper();
    try {
      PageResult<User> users =
          mapper.readValue(stepData.getLatestResponse().getBody(), PageResult.class);
      stepData.setUsers(users.getResults());
      isUsers = true;
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    assertThat("Data receive is a list of user", isUsers, is(true));
  }

  @Then("The client expect to receive a list of {int} user\\(s)")
  @SuppressWarnings("unchecked")
  public void expect_to_receive_a_list_of_users(Integer size) {
    List<User> usersList = new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper();
    try {
      PageResult<User> users =
          mapper.readValue(stepData.getLatestResponse().getBody(), PageResult.class);
      usersList = users.getResults();
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    assertThat("Data receive is a list of user", usersList.size(), is(size));
  }

  @Then("the client expect to receive an user")
  public void expect_to_receive_a_user() {
    Boolean isUser = false;
    ObjectMapper mapper =
        new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    User user;
    try {
      user = mapper.readValue(stepData.getLatestResponse().getBody(), User.class);
      stepData.setUser(user);
      isUser = true;
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    assertThat("Data receive is a user", isUser, is(true));
  }

  @Then("the client expect the username of user to be {}")
  public void expect_username_of_user_to_be(String username) {
    assertThat(stepData.getUser().getUsername(), is(username));
  }

  @Then("the client want to see the users list")
  public void show_list() {
    scenario.log(stepData.getUsers().toString());
    System.out.println(stepData.getUsers());
  }
}
