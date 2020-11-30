/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fr.insee.sugoi.converter.ouganext;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import fr.insee.sugoi.converter.ouganext.adapters.CertificateDeserializer;
import fr.insee.sugoi.converter.ouganext.adapters.CertificateSerializer;
import fr.insee.sugoi.converter.ouganext.adapters.OrganisationDeserializer;
import fr.insee.sugoi.converter.ouganext.adapters.OrganisationSerializer;
import fr.insee.sugoi.converter.utils.MapFromAttribute;
import fr.insee.sugoi.converter.utils.MapFromHashmapElement;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

/**
 * Java class for ContactType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ContactType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="NomCommun" type="{http://www.w3.org/2001/XMLSchema}token"/>
 *         &lt;element name="Nom" type="{http://www.w3.org/2001/XMLSchema}token"/>
 *         &lt;element name="Prenom" type="{http://www.w3.org/2001/XMLSchema}token"/>
 *         &lt;element name="DomaineDeGestion" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Civilite" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" maxOccurs="1"/>
 *         &lt;element name="IdentifiantMetier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AdresseMessagerie" type="{http://www.w3.org/2005/Atom}emailType" minOccurs="0"/>
 *         &lt;element name="NumeroTelephone" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
 *         &lt;element name="TelephonePortable" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
 *         &lt;element name="MotDePasse" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
 *         &lt;element name="CertificatAuthentification" type="{http://www.w3.org/2001/XMLSchema}byte" minOccurs="0"/>
 *         &lt;element name="CodePin" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
 *         &lt;element name="AdressePostale" type="{http://xml.insee.fr/schema}AdressePostaleType" minOccurs="0"/>
 *         &lt;element name="OrganisationDeRattachementUri" type="{http://www.w3.org/2005/Atom}linkType" minOccurs="0"/>
 *         &lt;element name="Habilitations" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Habilitation" type="{http://xml.insee.fr/schema/anunaire}HabilitationType" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="RepertoireDeDistribution" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Identifiant" type="{http://www.w3.org/2001/XMLSchema}token" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({
  "identifiant",
  "nomCommun",
  "nom",
  "prenom",
  "domaineDeGestion",
  "description",
  "civilite",
  "identifiantMetier",
  "adresseMessagerie",
  "numeroTelephone",
  "telephonePortable",
  "facSimile",
  "hasPassword",
  "certificate",
  "adresse",
  "organisationDeRattachementUri",
  "organisation",
  "repertoireDeDistribution",
  "propriete",
  "inseeRoleApplicatif",
  "codePin",
  "dateCreation",
  "inseeTimbre",
  "inseeOrganisme",
  "inseeAdresseCorrespondantLigne1",
  "inseeAdresseCorrespondantLigne2",
  "inseeAdresseCorrespondantLigne3",
  "postalCode",
  "inseeNomCorrespondant",
  "inseeMailCorrespondant",
  "inseeTelephoneNumberCorrespondant"
})
@JacksonXmlRootElement(localName = "Contact", namespace = Namespace.ANNUAIRE)
public class Contact {

  @JacksonXmlProperty(localName = "Identifiant", namespace = Namespace.ANNUAIRE)
  @MapFromAttribute(attributeName = "username")
  @JsonProperty(value = "Identifiant")
  private String identifiant;

  @JacksonXmlProperty(localName = "NomCommun", namespace = Namespace.ANNUAIRE)
  @MapFromHashmapElement(hashMapName = "attributes", hashMapKey = "nomCommun")
  @JsonProperty(value = "NomCommun")
  private String nomCommun;

  @JacksonXmlProperty(localName = "Nom", namespace = Namespace.ANNUAIRE)
  @MapFromAttribute(attributeName = "lastName")
  @JsonProperty(value = "Nom")
  private String nom;

  @JacksonXmlProperty(localName = "Prenom", namespace = Namespace.ANNUAIRE)
  @MapFromAttribute(attributeName = "firstName")
  @JsonProperty(value = "Prenom")
  private String prenom;

  @JacksonXmlProperty(localName = "DomaineDeGestion", namespace = Namespace.ANNUAIRE)
  @MapFromHashmapElement(hashMapName = "attributes", hashMapKey = "domaineDeGestion")
  @JsonProperty(value = "DomaineDeGestion")
  private String domaineDeGestion;

  @JacksonXmlProperty(localName = "AdresseMessagerie", namespace = Namespace.ANNUAIRE)
  @MapFromAttribute(attributeName = "mail")
  @JsonProperty(value = "AdresseMessagerie")
  private String adresseMessagerie;

  @JacksonXmlProperty(localName = "NumeroTelephone", namespace = Namespace.ANNUAIRE)
  @MapFromHashmapElement(hashMapName = "attributes", hashMapKey = "numeroTelephone")
  @JsonProperty(value = "NumeroTelephone")
  private String numeroTelephone;

  @JacksonXmlProperty(localName = "Description", namespace = Namespace.ANNUAIRE)
  @MapFromHashmapElement(hashMapName = "attributes", hashMapKey = "description")
  @JsonProperty(value = "Description")
  private String description;

  @JacksonXmlProperty(localName = "CertificatAuthentification", namespace = Namespace.ANNUAIRE)
  @MapFromAttribute(attributeName = "certificate")
  @JsonSerialize(using = CertificateSerializer.class)
  @JsonDeserialize(using = CertificateDeserializer.class)
  @JsonProperty(value = "CertificatAuthentification")
  private byte[] certificate;

  @JacksonXmlProperty(localName = "AdressePostale", namespace = Namespace.INSEE)
  @JsonProperty(value = "AdressePostale")
  private Adresse adresse;

  @JacksonXmlProperty(localName = "OrganisationDeRattachementUri", namespace = Namespace.ANNUAIRE)
  @JsonSerialize(using = OrganisationSerializer.class)
  @JsonDeserialize(using = OrganisationDeserializer.class)
  @JsonProperty(value = "OrganisationDeRattachementUri")
  private Organisation organisationDeRattachement;

  @JacksonXmlProperty(localName = "Civilite", namespace = Namespace.ANNUAIRE)
  @MapFromHashmapElement(hashMapName = "attributes", hashMapKey = "civilite")
  @JsonProperty(value = "Civilite")
  private String civilite;

  @JacksonXmlProperty(localName = "IdentifiantMetier", namespace = Namespace.ANNUAIRE)
  @MapFromHashmapElement(hashMapName = "attributes", hashMapKey = "identifiantMetier")
  @JsonProperty(value = "IdentifiantMetier")
  private String identifiantMetier;

  @JacksonXmlProperty(localName = "RepertoireDeDistribution", namespace = Namespace.ANNUAIRE)
  @MapFromHashmapElement(hashMapName = "attributes", hashMapKey = "repertoireDeDistribution")
  @JsonProperty(value = "RepertoireDeDistribution")
  private String repertoireDeDistribution;

  @JacksonXmlProperty(localName = "TelephonePortable", namespace = Namespace.ANNUAIRE)
  @MapFromHashmapElement(hashMapName = "attributes", hashMapKey = "telephonePortable")
  @JsonProperty(value = "TelephonePortable")
  private String telephonePortable;

  @JacksonXmlProperty(localName = "FacSimile", namespace = Namespace.ANNUAIRE)
  @MapFromHashmapElement(hashMapName = "attributes", hashMapKey = "facSimile")
  @JsonProperty(value = "FacSimile")
  private String facSimile;

  @JacksonXmlProperty(localName = "MotDePasseExiste", namespace = Namespace.ANNUAIRE)
  @MapFromHashmapElement(hashMapName = "metadatas", hashMapKey = "hasPassword")
  @JsonValue(value = false)
  @JsonProperty(value = "MotDePasseExiste")
  private boolean hasPassword;

  @MapFromHashmapElement(hashMapName = "attributes", hashMapKey = "proprietes")
  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "propriete", namespace = Namespace.ANNUAIRE)
  @JsonProperty(value = "Propriete")
  private Collection<String> propriete;

  @JacksonXmlProperty(localName = "InseeRoleApplicatif", namespace = Namespace.ANNUAIRE)
  // @MapFromAttribute(attributeName = "habilitations")
  @JsonProperty(value = "InseeRoleApplicatif")
  private Collection<String> inseeRoleApplicatif;

  @JacksonXmlProperty(localName = "CodePin", namespace = Namespace.ANNUAIRE)
  @MapFromHashmapElement(hashMapName = "attributes", hashMapKey = "codePin")
  @JsonProperty(value = "CodePin")
  private byte[] codePin;

  @JacksonXmlProperty(localName = "DateCreation", namespace = Namespace.ANNUAIRE)
  @MapFromHashmapElement(hashMapName = "metadatas", hashMapKey = "dateCreation")
  @JsonProperty(value = "DateCreation")
  private Date dateCreation;

  /* CAS DE L'AGENT INSEE */

  private String inseeTimbre;

  private String inseeOrganisme;

  /* CAS DE L'ADRESSE DANS LE CONTACT : inseeCompteEnquete */

  private String inseeAdresseCorrespondantLigne1;

  private String inseeAdresseCorrespondantLigne2;

  private String inseeAdresseCorrespondantLigne3;

  private String postalCode;

  private String inseeNomCorrespondant;

  private String inseeMailCorrespondant;

  private String inseeTelephoneNumberCorrespondant;

  /* GETTERS SETTERS */

  public String getIdentifiant() {
    return identifiant;
  }

  public void setIdentifiant(String value) {
    this.identifiant = value;
  }

  public String getNomCommun() {
    return nomCommun;
  }

  public void setNomCommun(String value) {
    this.nomCommun = value;
  }

  public String getNom() {
    return nom;
  }

  public void setNom(String nom) {
    this.nom = nom;
  }

  public String getPrenom() {
    return prenom;
  }

  public void setPrenom(String prenom) {
    this.prenom = prenom;
  }

  public String getDomaineDeGestion() {
    return domaineDeGestion;
  }

  public void setDomaineDeGestion(String value) {
    this.domaineDeGestion = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String value) {
    this.description = value;
  }

  public String getAdresseMessagerie() {
    return adresseMessagerie;
  }

  public void setAdresseMessagerie(String value) {
    this.adresseMessagerie = value;
  }

  public String getNumeroTelephone() {
    return numeroTelephone;
  }

  public void setNumeroTelephone(String value) {
    this.numeroTelephone = value;
  }

  public String getTelephonePortable() {
    return telephonePortable;
  }

  public void setTelephonePortable(String telephonePortable) {
    this.telephonePortable = telephonePortable;
  }

  public String getFacSimile() {
    return facSimile;
  }

  public void setFacSimile(String facSimile) {
    this.facSimile = facSimile;
  }

  public Adresse getAdresse() {
    return adresse;
  }

  public void setAdresse(Adresse value) {
    this.adresse = value;
  }

  public void setOrganisationDeRattachement(Organisation organisation) {
    this.organisationDeRattachement = organisation;
  }

  public Organisation getOrganisationDeRattachement() {
    return organisationDeRattachement;
  }

  public String getRepertoireDeDistribution() {
    return repertoireDeDistribution;
  }

  public void setRepertoireDeDistribution(String value) {
    this.repertoireDeDistribution = value;
  }

  public Collection<String> getPropriete() {
    if (propriete == null) {
      propriete = new HashSet<>();
    }
    return propriete;
  }

  public Collection<String> getInseeRoleApplicatif() {
    if (inseeRoleApplicatif == null) {
      inseeRoleApplicatif = new HashSet<>();
    }
    return inseeRoleApplicatif;
  }

  public void setHasPassword(boolean hasPassword) {
    this.hasPassword = hasPassword;
  }

  public boolean isHasPassword() {
    return hasPassword;
  }

  public void setCertificate(byte[] certificate) {
    this.certificate = certificate;
  }

  public byte[] getCertificate() {
    return certificate;
  }

  public String getCivilite() {
    return civilite;
  }

  public void setCivilite(String civilite) {
    this.civilite = civilite;
  }

  public String getIdentifiantMetier() {
    return identifiantMetier;
  }

  public void setIdentifiantMetier(String identifiantMetier) {
    this.identifiantMetier = identifiantMetier;
  }

  public byte[] getCodePin() {
    return codePin;
  }

  public void setCodePin(byte[] codePin) {
    this.codePin = codePin;
  }

  public Date getDateCreation() {
    return dateCreation;
  }

  public void setDateCreation(Date dateCreation) {
    this.dateCreation = dateCreation;
  }

  public String getInseeTimbre() {
    return inseeTimbre;
  }

  public void setInseeTimbre(String inseeTimbre) {
    this.inseeTimbre = inseeTimbre;
  }

  public String getInseeOrganisme() {
    return inseeOrganisme;
  }

  public void setInseeOrganisme(String inseeOrganisme) {
    this.inseeOrganisme = inseeOrganisme;
  }

  public String getInseeMailCorrespondant() {
    return inseeMailCorrespondant;
  }

  public void setInseeMailCorrespondant(String inseemailcorrespondant) {
    this.inseeMailCorrespondant = inseemailcorrespondant;
  }

  public String getInseeNomCorrespondant() {
    return inseeNomCorrespondant;
  }

  public void setInseeNomCorrespondant(String inseenomcorrespondant) {
    this.inseeNomCorrespondant = inseenomcorrespondant;
  }

  public String getInseeTelephoneNumberCorrespondant() {
    return inseeTelephoneNumberCorrespondant;
  }

  public void setInseeTelephoneNumberCorrespondant(String inseetelephonenumbercorrespondant) {
    this.inseeTelephoneNumberCorrespondant = inseetelephonenumbercorrespondant;
  }

  public String getInseeAdresseCorrespondantLigne1() {
    return inseeAdresseCorrespondantLigne1;
  }

  public void setInseeAdresseCorrespondantLigne1(String inseeAdresseCorrespondantLigne1) {
    this.inseeAdresseCorrespondantLigne1 = inseeAdresseCorrespondantLigne1;
  }

  public String getInseeAdresseCorrespondantLigne2() {
    return inseeAdresseCorrespondantLigne2;
  }

  public void setInseeAdresseCorrespondantLigne2(String inseeAdresseCorrespondantLigne2) {
    this.inseeAdresseCorrespondantLigne2 = inseeAdresseCorrespondantLigne2;
  }

  public String getInseeAdresseCorrespondantLigne3() {
    return inseeAdresseCorrespondantLigne3;
  }

  public void setInseeAdresseCorrespondantLigne3(String inseeAdresseCorrespondantLigne3) {
    this.inseeAdresseCorrespondantLigne3 = inseeAdresseCorrespondantLigne3;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }
}
