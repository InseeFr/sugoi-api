package fr.insee.sugoi.model;

import java.util.List;

public class Realm {
    private String name;
    private String url;
    private String appBranch;
    private List<Application> applications;
    private List<UserStorage> userStorages;

    public Realm() {

    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAppBranch() {
        return this.appBranch;
    }

    public void setAppBranch(String appBranch) {
        this.appBranch = appBranch;
    }

    public List<Application> getApplications() {
        return this.applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public List<UserStorage> getUserStorages() {
        return this.userStorages;
    }

    public void setUserStorages(List<UserStorage> userStorages) {
        this.userStorages = userStorages;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
