package fr.insee.sugoi.model;

import java.util.List;

public class OrganizationMapping {
    private String address;

    private String identifiant;

    private String organization;

    private List<AttributesSugoi> attributes;

    @Override
    public String toString() {
        return "OrganizationMapping{" +
                "address='" + address + '\'' +
                ", identifiant='" + identifiant + '\'' +
                ", organization='" + organization + '\'' +
                ", attributes=" + attributes +
                '}';
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public List<AttributesSugoi> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributesSugoi> attributes) {
        this.attributes = attributes;
    }
}
