package fr.insee.sugoi.app.cucumber.glue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.sugoi.app.cucumber.utils.PageResult;
import fr.insee.sugoi.app.cucumber.utils.StepData;
import fr.insee.sugoi.model.User;
import io.cucumber.java.en.Then;

public class UserGlue {

    private StepData stepData;

    public UserGlue(StepData stepData) {
        this.stepData = stepData;
    }

    @Then("the client expect to receive a list of users")
    public void expect_to_receive_a_list_of_users() {
        Boolean isUsers = false;
        ObjectMapper mapper = new ObjectMapper();
        try {
            PageResult<User> users = mapper.readValue(stepData.getLatestResponse().getBody(), PageResult.class);
            stepData.setUsers(users.getResults());
            isUsers = true;
        } catch (JsonProcessingException e) {
            isUsers = false;
        }
        assertThat("Data receive is a list of user", isUsers, is(true));
    }

    @Then("the client expect to receive a user")
    public void expect_to_receive_a_user() {
        Boolean isUser = false;
        ObjectMapper mapper = new ObjectMapper();
        User user;
        try {
            user = mapper.readValue(stepData.getLatestResponse().getBody(), User.class);
            stepData.setUser(user);
            isUser = true;
        } catch (JsonProcessingException e) {
        }
        assertThat("Data receive is a user", isUser, is(false));
    }

    @Then("the client expect the username of user to be {}")
    public void expect_username_of_user_to_be(String username) {
        assertThat(stepData.getUser().getUsername(), is(username));
    }

    @Then("the client want to see the users list")
    public void show_list() {
        System.out.println(stepData.getUsers());
    }

}
