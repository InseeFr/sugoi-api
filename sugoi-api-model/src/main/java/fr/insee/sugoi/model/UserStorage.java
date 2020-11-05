package fr.insee.sugoi.model;

import java.util.List;

import fr.insee.sugoi.model.Technique.Store;

public class UserStorage {
    private String name;
    private String userBranch;
    private String organizationBranch;
    private List<Organization> organizations;
    private List<User> users;

    private Store store;

    public UserStorage() {

    }

    public List<User> getUsers() {
        return this.users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserBranch() {
        return this.userBranch;
    }

    public void setUserBranch(String userBranch) {
        this.userBranch = userBranch;
    }

    public String getOrganizationBranch() {
        return this.organizationBranch;
    }

    public void setOrganizationBranch(String organizationBranch) {
        this.organizationBranch = organizationBranch;
    }

    public List<Organization> getOrganizations() {
        return this.organizations;
    }

    public void setOrganizations(List<Organization> organizations) {
        this.organizations = organizations;
    }

    public Store getStore() {
        return this.store;
    }

    public void setStore(Store connection) {
        this.store = connection;
    }

}
