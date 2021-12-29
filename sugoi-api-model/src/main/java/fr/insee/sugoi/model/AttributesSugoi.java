package fr.insee.sugoi.model;

// TODO j'en ai sûrement oublié
public class AttributesSugoi {

    private String description;

    private String mail;

    private String commonName;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "AttributesSugoi{" +
                "description='" + description + '\'' +
                ", mail='" + mail + '\'' +
                ", commonName='" + commonName + '\'' +
                '}';
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }
}
