package fr.insee.sugoi.model;

import java.util.HashMap;
import java.util.Map;

public class Organization {
    private String identifiant;
    private byte[] gpgkey;
    private Organization organization;

    private Map<String, String> address = new HashMap<>();
    private Map<String, Object> metadatas = new HashMap<>();
    private Map<String, Object> attributes = new HashMap<>();


    public byte[] getGpgkey() {
        return this.gpgkey;
    }

    public void setGpgkey(byte[] gpgkey) {
        this.gpgkey = gpgkey;
    }

    public Map<String, String> getAddress() {
        return this.address;
    }

    public void addAddress(String name, String value) {
        this.address.put(name, value);
    }

    public void setAddress(Map<String, String> address) {
        this.address = address;
    }

    public Map<String, Object> getMetadatas() {
        return this.metadatas;
    }

    public void addMetadatas(String name, Object value) {
        this.metadatas.put(name, value);
    }

    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public void addAttributes(String name, Object value) {
        this.attributes.put(name, value);
    }

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getIdentifiant() {
        return this.identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

}
