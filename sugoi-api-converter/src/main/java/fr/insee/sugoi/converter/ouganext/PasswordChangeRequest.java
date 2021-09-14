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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import fr.insee.sugoi.converter.utils.MapFromAttribute;

/**
 * Java class for PasswordChangeRequestType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
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
 */
@JsonPropertyOrder({
  "adresseMessagerie",
  "adressePostale",
  "ancienMotDePasse",
  "nouveauMotDePasse",
  "infoFormattageEnvoi"
})
@JacksonXmlRootElement(localName = "PasswordChangeRequest", namespace = Namespace.ANNUAIRE)
public class PasswordChangeRequest {

  @JacksonXmlProperty(localName = "AdresseMessagerie")
  @MapFromAttribute(attributeName = "email")
  @JsonProperty(value = "AdresseMessagerie")
  protected String adresseMessagerie;

  @JacksonXmlProperty(localName = "AdressePostale")
  private Adresse adressePostale;

  @JacksonXmlProperty(localName = "AncienMotDePasse")
  @MapFromAttribute(attributeName = "oldPassword")
  @JsonProperty(value = "AncienMotDePasse")
  protected String ancienMotDePasse;

  @JacksonXmlProperty(localName = "NouveauMotDePasse")
  @MapFromAttribute(attributeName = "newPassword")
  @JsonProperty(value = "NouveauMotDePasse")
  protected String nouveauMotDePasse;

  @JacksonXmlProperty(localName = "InfoFormattageEnvoi")
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
