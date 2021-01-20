package fr.insee.sugoi.app.cucumber.glue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fr.insee.sugoi.app.cucumber.utils.StepData;
import fr.insee.sugoi.app.cucumber.utils.WebRequest;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class GenericGlue {

    private StepData stepData;

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

    @When("the client perform {} request with body on url {} body:")
    public void the_client_perform_request_on_url(String MethodType, String url, String body) throws Throwable {
        WebRequest webRequest = new WebRequest();
        Map<String, String> headers = new HashMap<>();
        switch (MethodType) {
            case "GET":
                stepData.setLatestResponse(webRequest.executeGet(url, headers, body));
                break;
            case "POST":
                stepData.setLatestResponse(webRequest.executePost(url, headers, body));
                break;
            case "DELETE":
                stepData.setLatestResponse(webRequest.executeDelete(url, headers, body));
                break;
            case "PUT":
                stepData.setLatestResponse(webRequest.executeUpdate(url, headers, body));
                break;
        }
    }

    @When("the client perform {} request on url {}")
    public void the_client_perform_request_on_url(String MethodType, String url) throws Throwable {
        WebRequest webRequest = new WebRequest();
        Map<String, String> headers = new HashMap<>();
        switch (MethodType) {
            case "GET":
                stepData.setLatestResponse(webRequest.executeGet(url, headers, null));
                break;
            case "POST":
                stepData.setLatestResponse(webRequest.executePost(url, headers, null));
                break;
            case "DELETE":
                stepData.setLatestResponse(webRequest.executeDelete(url, headers, null));
                break;
            case "PUT":
                stepData.setLatestResponse(webRequest.executeUpdate(url, headers, null));
                break;
        }
    }

    @Then("the client receives status code {}")
    public void the_client_receive_status_code(int statusCode) throws IOException {
        assertThat(stepData.getLatestResponse().getClientHttpResponse().getStatusCode().value(), is(statusCode));
    }

    @And("the client receives body:")
    public void the_client_receives__body(String bodyResponse) throws Throwable {
        assertThat(stepData.getLatestResponse().getBody(), is(bodyResponse));
    }
}
