package fr.insee.sugoi.model;

public class Habilitation {
    private String application;
    private String role;
    private String property;

    public Habilitation() {

    }

    public Habilitation(String application, String role, String property) {
        this.application = application;
        this.role = role;
        this.property = property;
    }

    public String getApplication() {
        return this.application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProperty() {
        return this.property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

}
