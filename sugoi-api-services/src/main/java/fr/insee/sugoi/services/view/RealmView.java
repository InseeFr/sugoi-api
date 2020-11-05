package fr.insee.sugoi.services.view;

import java.util.List;

public class RealmView {

    private String name;
    private String url;
    private String appBranch;
    private List<UserStorageView> userStorages;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAppBranch() {
        return this.appBranch;
    }

    public void setAppBranch(String appBranch) {
        this.appBranch = appBranch;
    }

    public List<UserStorageView> getUserStorages() {
        return this.userStorages;
    }

    public void setUserStorages(List<UserStorageView> userStorages) {
        this.userStorages = userStorages;
    }

}
