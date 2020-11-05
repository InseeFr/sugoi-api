package fr.insee.sugoi.model.Technique;

public class UserStorageConfig {

    private String name;
    private String realmName;
    private String connectionUrl;
    private String connectionUsername;
    private String connectionPassword;
    private String userBranch;
    private String organizationBranch;
    private String appBranch;
    private int poolSize;
    private String type;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRealmName() {
        return this.realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    public String getConnectionUrl() {
        return this.connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public String getConnectionUsername() {
        return this.connectionUsername;
    }

    public void setConnectionUsername(String connectionUsername) {
        this.connectionUsername = connectionUsername;
    }

    public String getConnectionPassword() {
        return this.connectionPassword;
    }

    public void setConnectionPassword(String connectionPassword) {
        this.connectionPassword = connectionPassword;
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

    public String getAppBranch() {
        return this.appBranch;
    }

    public void setAppBranch(String appBranch) {
        this.appBranch = appBranch;
    }

    public int getPoolSize() {
        return this.poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }
}
