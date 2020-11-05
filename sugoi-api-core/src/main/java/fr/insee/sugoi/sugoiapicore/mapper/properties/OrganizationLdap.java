package fr.insee.sugoi.sugoiapicore.mapper.properties;

import fr.insee.sugoi.sugoiapicore.mapper.properties.utils.AttributeLdapName;
import fr.insee.sugoi.sugoiapicore.mapper.properties.utils.MapToAttribute;
import fr.insee.sugoi.sugoiapicore.mapper.properties.utils.MapToMapElement;

public class OrganizationLdap {

    @AttributeLdapName("cn")
    @MapToAttribute("identifiant")
    public String cn;

    @AttributeLdapName("description")
    @MapToMapElement(name = "attributes", key = "description")
    public String description;

    @AttributeLdapName("inseeAdressePostaleDn")
    @MapToMapElement(name = "attributes", key = "adressDn")
    public String adresseDn;

    @AttributeLdapName("inseeClefChiffrement")
    @MapToAttribute("gpgkey")
    public byte[] clef;

    @AttributeLdapName("mail")
    @MapToMapElement(name = "attributes", key = "mail")
    public String mail;

}
