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

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.app.cucumber.utils.ResponseResults;
import fr.insee.sugoi.app.cucumber.utils.StepData;
import fr.insee.sugoi.app.cucumber.utils.WebRequest;
import fr.insee.sugoi.model.Realm;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.net.util.Base64;

public class GenericGlue {

  private Scenario scenario;

  @Before
  public void before(Scenario scenario) {
    this.scenario = scenario;
  }

  private StepData stepData;

  String basicUsername = null;
  String basicPassword = null;

  public GenericGlue(StepData stepData) {
    this.stepData = stepData;
  }

  @Given("the client is using {}")
  public void select_default_plateform(String name) throws Exception {
    switch (name) {
      case "tomcat1":
        stepData.setDefaultTomcatUrl("/tomcat1");
        break;

      case "tomcat2":
        stepData.setDefaultTomcatUrl("/tomcat2");
        break;

      default:
        throw new Exception("Client can use only tomcat1 or tomcat2");
    }
  }

  @Given("the client authentified with username {} and password {}")
  public void select_role(String username, String password) throws Exception {
    basicUsername = username;
    basicPassword = password;
  }

  @When("the client perform {} request with body on url {} body:")
  public void the_client_perform_request_on_url(String MethodType, String url, String body)
      throws Throwable {
    WebRequest webRequest = new WebRequest();
    Map<String, String> headers = new HashMap<>();
    if (basicPassword != null && basicUsername != null) {
      String auth = basicUsername + ":" + basicPassword;
      byte[] encodedAuth = Base64.encodeBase64(auth.getBytes());
      String authHeader = "Basic " + new String(encodedAuth);
      headers.put("Authorization", authHeader);
    }
    switch (MethodType) {
      case "GET":
        stepData.setLatestResponse(
            webRequest.executeGet(stepData.getDefaultTomcatUrl() + url, headers, body));
        break;
      case "POST":
        stepData.setLatestResponse(
            webRequest.executePost(stepData.getDefaultTomcatUrl() + url, headers, body));
        break;
      case "DELETE":
        stepData.setLatestResponse(
            webRequest.executeDelete(stepData.getDefaultTomcatUrl() + url, headers, body));
        break;
      case "PUT":
        stepData.setLatestResponse(
            webRequest.executeUpdate(stepData.getDefaultTomcatUrl() + url, headers, body));
        break;
    }
  }

  @When("the client perform {} request on url {}")
  public void the_client_perform_request_on_url(String MethodType, String url) throws Throwable {
    WebRequest webRequest = new WebRequest();
    Map<String, String> headers = new HashMap<>();
    if (basicPassword != null && basicUsername != null) {
      String auth = basicUsername + ":" + basicPassword;
      byte[] encodedAuth = Base64.encodeBase64(auth.getBytes());
      String authHeader = "Basic " + new String(encodedAuth);
      headers.put("Authorization", authHeader);
    }
    switch (MethodType) {
      case "GET":
        stepData.setLatestResponse(
            webRequest.executeGet(stepData.getDefaultTomcatUrl() + url, headers, null));
        break;
      case "POST":
        stepData.setLatestResponse(
            webRequest.executePost(stepData.getDefaultTomcatUrl() + url, headers, null));
        break;
      case "DELETE":
        stepData.setLatestResponse(
            webRequest.executeDelete(stepData.getDefaultTomcatUrl() + url, headers, null));
        break;
      case "PUT":
        stepData.setLatestResponse(
            webRequest.executeUpdate(stepData.getDefaultTomcatUrl() + url, headers, null));
        break;
    }
  }

  @Then("the client receives status code {}")
  public void the_client_receive_status_code(int statusCode) throws IOException {
    assertThat(
        stepData.getLatestResponse().getClientHttpResponse().getStatusCode().value(),
        is(statusCode));
  }

  @And("the client receives body:")
  public void the_client_receives__body(String bodyResponse) throws Throwable {
    assertThat(stepData.getLatestResponse().getBody(), is(bodyResponse));
  }

  @And("body received")
  public void body_received() throws Throwable {
    scenario.log(stepData.getLatestResponse().getBody());
  }

  @Given("app is ready")
  public void app_is_ready() throws InterruptedException {
    Boolean isReady = false;
    ObjectMapper mapper = new ObjectMapper();
    WebRequest webRequest = new WebRequest();
    Map<String, String> headers = new HashMap<>();
    String auth = "appli_sugoi:sugoi";
    byte[] encodedAuth = Base64.encodeBase64(auth.getBytes());
    String authHeader = "Basic " + new String(encodedAuth);
    headers.put("Authorization", authHeader);
    while (!isReady) {
      try {
        ResponseResults response =
            webRequest.executeGet(stepData.getDefaultTomcatUrl() + "/realms", headers, null);
        List<Realm> realms = Arrays.asList(mapper.readValue(response.getBody(), Realm[].class));
        if (realms.size() > 0) {
          isReady = true;
        } else {
          System.out.println("App is not ready sleeping...");
          Thread.sleep(20000);
          System.out.println("continue...");
        }
      } catch (IOException e) {
        System.out.println("App is not ready sleeping...");
        Thread.sleep(20000);
        System.out.println("continue...");
      }
    }
  }
}
