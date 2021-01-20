package fr.insee.sugoi.app.cucumber.utils;

import java.util.List;

import fr.insee.sugoi.model.User;

public class StepData {

    ResponseResults latestResponse = null;
    User user = null;
    List<User> users = null;
    String defaultTomcatUrl = "/tomcat1";

    public StepData() {
    }

    public ResponseResults getLatestResponse() {
        return latestResponse;
    }

    public void setLatestResponse(ResponseResults latestResponse) {
        this.latestResponse = latestResponse;
    }

    public User getUser() {
        return user;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getDefaultTomcatUrl() {
        return defaultTomcatUrl;
    }

    public void setDefaultTomcatUrl(String defaultTomcatUrl) {
        this.defaultTomcatUrl = defaultTomcatUrl;
    }
}
