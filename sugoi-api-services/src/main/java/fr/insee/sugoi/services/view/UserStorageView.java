package fr.insee.sugoi.services.view;

public class UserStorageView {
    private String name;
    private String userBranch;
    private String organizationBranch;

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

}
