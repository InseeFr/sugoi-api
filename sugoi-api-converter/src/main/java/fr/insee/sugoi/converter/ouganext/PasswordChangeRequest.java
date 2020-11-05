package fr.insee.sugoi.converter.ouganext;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Java class for PasswordChangeRequestType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="PasswordChangeRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AdresseMessagerie" type="{http://xml.insee.fr/schema/annuaire}emailType" minOccurs="0"/>
 *         &lt;element name="AncienMotDePasse" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NouveauMotDePasse" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="InfoFormattageEnvoi" type="{http://xml.insee.fr/schema/annuaire}InfoFormattageType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@JsonPropertyOrder({ "adresseMessagerie", "adressePostale", "ancienMotDePasse", "nouveauMotDePasse",
    "infoFormattageEnvoi" })
@JacksonXmlRootElement(localName = "PasswordChangeRequest", namespace = Namespace.ANNUAIRE)
public class PasswordChangeRequest {

  @JacksonXmlProperty(localName = "AdresseMessagerie", namespace = Namespace.ANNUAIRE)
  protected String adresseMessagerie;
  @JacksonXmlProperty(localName = "AdressePostale", namespace = Namespace.INSEE)
  private Adresse adressePostale;
  @JacksonXmlProperty(localName = "AncienMotDePasse", namespace = Namespace.ANNUAIRE)
  protected String ancienMotDePasse;
  @JacksonXmlProperty(localName = "NouveauMotDePasse", namespace = Namespace.ANNUAIRE)
  protected String nouveauMotDePasse;
  @JacksonXmlProperty(localName = "InfoFormattageEnvoi", namespace = Namespace.ANNUAIRE)
  protected InfoFormattage infoFormattageEnvoi;

  public String getAdresseMessagerie() {
    return adresseMessagerie;
  }

  public void setAdresseMessagerie(String value) {
    this.adresseMessagerie = value;
  }

  public Adresse getAdressePostale() {
    return adressePostale;
  }

  public void setAdressePostale(Adresse adressePostale) {
    this.adressePostale = adressePostale;
  }

  public String getAncienMotDePasse() {
    return ancienMotDePasse;
  }

  public void setAncienMotDePasse(String value) {
    this.ancienMotDePasse = value;
  }

  public String getNouveauMotDePasse() {
    return nouveauMotDePasse;
  }

  public void setNouveauMotDePasse(String value) {
    this.nouveauMotDePasse = value;
  }

  public InfoFormattage getInfoFormattageEnvoi() {
    return infoFormattageEnvoi;
  }

  public void setInfoFormattageEnvoi(InfoFormattage value) {
    this.infoFormattageEnvoi = value;
  }

}
