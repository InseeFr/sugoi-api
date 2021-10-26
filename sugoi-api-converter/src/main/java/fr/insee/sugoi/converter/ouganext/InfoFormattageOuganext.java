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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Java class for InfoFormattageType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="InfoFormattageType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ChefSignataire" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HotlineFax" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HotlineMail" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HotlineTel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="IdentifiantApplication" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="IdentifiantEnt" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ModeleCourrier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NomDepartement" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NomDirection" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NomFamille" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NomService" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NomSignataire" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="UEIdentifiant" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="URLSite" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@JacksonXmlRootElement(localName = "InfoFormattage", namespace = Namespace.ANNUAIRE)
@JsonPropertyOrder({
  "chefSignataire",
  "hotlineFax",
  "hotlineMail",
  "hotlineTel",
  "identifiantApplication",
  "identifiantEnt",
  "modeleCourrier",
  "nomDepartement",
  "nomDirection",
  "nomApplicationLettre",
  "nomService",
  "nomSignataire",
  "ueIdentifiant",
  "urlSite",
  "logo",
  "adresseMessagerieEmettrice"
})
public class InfoFormattageOuganext {

  @JacksonXmlProperty(localName = "ChefSignataire")
  protected String chefSignataire;

  @JacksonXmlProperty(localName = "HotlineFax")
  protected String hotlineFax;

  @JacksonXmlProperty(localName = "HotlineMail")
  protected String hotlineMail;

  @JacksonXmlProperty(localName = "HotlineTel")
  protected String hotlineTel;

  @JacksonXmlProperty(localName = "IdentifiantApplication")
  protected String identifiantApplication;

  @JacksonXmlProperty(localName = "IdentifiantEnt")
  protected String identifiantEnt;

  @JacksonXmlProperty(localName = "ModeleCourrier")
  protected String modeleCourrier;

  @JacksonXmlProperty(localName = "NomDepartement")
  protected String nomDepartement;

  @JacksonXmlProperty(localName = "NomDirection")
  protected String nomDirection;

  @JacksonXmlProperty(localName = "NomApplicationLettre")
  protected String nomApplicationLettre;

  @JacksonXmlProperty(localName = "NomService")
  protected String nomService;

  @JacksonXmlProperty(localName = "NomSignataire")
  protected String nomSignataire;

  @JacksonXmlProperty(localName = "UEIdentifiant")
  protected String ueIdentifiant;

  @JacksonXmlProperty(localName = "URLSite")
  protected String urlSite;

  @JacksonXmlProperty(localName = "Logo")
  protected String logo;

  @JacksonXmlProperty(localName = "AdresseMessagerieEmettrice")
  protected String adresseMessagerieEmettrice;

  public String getChefSignataire() {
    return chefSignataire;
  }

  public void setChefSignataire(String value) {
    this.chefSignataire = value;
  }

  public String getHotlineFax() {
    return hotlineFax;
  }

  public void setHotlineFax(String value) {
    this.hotlineFax = value;
  }

  public String getHotlineMail() {
    return hotlineMail;
  }

  public void setHotlineMail(String value) {
    this.hotlineMail = value;
  }

  public String getHotlineTel() {
    return hotlineTel;
  }

  public void setHotlineTel(String value) {
    this.hotlineTel = value;
  }

  public String getIdentifiantApplication() {
    return identifiantApplication;
  }

  public void setIdentifiantApplication(String value) {
    this.identifiantApplication = value;
  }

  public String getIdentifiantEnt() {
    return identifiantEnt;
  }

  public void setIdentifiantEnt(String value) {
    this.identifiantEnt = value;
  }

  public String getModeleCourrier() {
    return modeleCourrier;
  }

  public void setModeleCourrier(String value) {
    this.modeleCourrier = value;
  }

  public String getNomDepartement() {
    return nomDepartement;
  }

  public void setNomDepartement(String value) {
    this.nomDepartement = value;
  }

  public String getNomDirection() {
    return nomDirection;
  }

  public void setNomDirection(String value) {
    this.nomDirection = value;
  }

  public String getNomApplicationLettre() {
    return nomApplicationLettre;
  }

  public void setNomApplicationLettre(String value) {
    this.nomApplicationLettre = value;
  }

  public String getNomService() {
    return nomService;
  }

  public void setNomService(String value) {
    this.nomService = value;
  }

  public String getNomSignataire() {
    return nomSignataire;
  }

  public void setNomSignataire(String value) {
    this.nomSignataire = value;
  }

  public String getUeIdentifiant() {
    return ueIdentifiant;
  }

  public void setUeIdentifiant(String value) {
    this.ueIdentifiant = value;
  }

  public String getUrlSite() {
    return urlSite;
  }

  public void setUrlSite(String value) {
    this.urlSite = value;
  }

  public String getLogo() {
    return logo;
  }

  public void setLogo(String logo) {
    this.logo = logo;
  }

  public String getAdresseMessagerieEmettrice() {
    return adresseMessagerieEmettrice;
  }

  public void setAdresseMessagerieEmettrice(String adresseMessagerieEmettrice) {
    this.adresseMessagerieEmettrice = adresseMessagerieEmettrice;
  }
}
