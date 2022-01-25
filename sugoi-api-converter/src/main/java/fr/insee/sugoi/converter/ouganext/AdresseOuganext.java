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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Java class for AdressePostaleType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AdressePostaleType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="LigneUne" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
 *         &lt;element name="LigneDeux" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
 *         &lt;element name="LigneTrois" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
 *         &lt;element name="LigneQuatre" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
 *         &lt;element name="LigneCinq" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
 *         &lt;element name="LigneSix" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
 *         &lt;element name="LigneSept" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@JsonInclude(Include.NON_NULL)
@JacksonXmlRootElement(localName = "AdressePostale", namespace = Namespace.INSEE)
public class AdresseOuganext {

  @JsonIgnore private String identifiant;

  @JacksonXmlProperty(localName = "LigneUne", namespace = Namespace.INSEE)
  protected String ligneUne;

  @JacksonXmlProperty(localName = "LigneDeux", namespace = Namespace.INSEE)
  protected String ligneDeux;

  @JacksonXmlProperty(localName = "LigneTrois", namespace = Namespace.INSEE)
  protected String ligneTrois;

  @JacksonXmlProperty(localName = "LigneQuatre", namespace = Namespace.INSEE)
  protected String ligneQuatre;

  @JacksonXmlProperty(localName = "LigneCinq", namespace = Namespace.INSEE)
  protected String ligneCinq;

  @JacksonXmlProperty(localName = "LigneSix", namespace = Namespace.INSEE)
  protected String ligneSix;

  @JacksonXmlProperty(localName = "LigneSept", namespace = Namespace.INSEE)
  protected String ligneSept;

  public AdresseOuganext(String[] lines) {
    for (int i = 1; i <= lines.length; i++) {
      setLine(lines[i - 1], i);
    }
  }

  public AdresseOuganext() {}

  private void setLine(String value, int nbLine) {
    switch (nbLine) {
      case 1:
        this.ligneUne = value;
        break;
      case 2:
        this.ligneDeux = value;
        break;
      case 3:
        this.ligneTrois = value;
        break;
      case 4:
        this.ligneQuatre = value;
        break;
      case 5:
        this.ligneCinq = value;
        break;
      case 6:
        this.ligneSix = value;
        break;
      case 7:
        this.ligneSept = value;
        break;
      default:
        break;
    }
  }

  public String getLigneUne() {
    return ligneUne;
  }

  public void setLigneUne(String value) {
    this.ligneUne = value;
  }

  public String getLigneDeux() {
    return ligneDeux;
  }

  public void setLigneDeux(String value) {
    this.ligneDeux = value;
  }

  public String getLigneTrois() {
    return ligneTrois;
  }

  public void setLigneTrois(String value) {
    this.ligneTrois = value;
  }

  public String getLigneQuatre() {
    return ligneQuatre;
  }

  public void setLigneQuatre(String value) {
    this.ligneQuatre = value;
  }

  public String getLigneCinq() {
    return ligneCinq;
  }

  public void setLigneCinq(String value) {
    this.ligneCinq = value;
  }

  public String getLigneSix() {
    return ligneSix;
  }

  public void setLigneSix(String value) {
    this.ligneSix = value;
  }

  public String getLigneSept() {
    return ligneSept;
  }

  public void setLigneSept(String value) {
    this.ligneSept = value;
  }

  public void setIdentifiant(String identifiant) {
    this.identifiant = identifiant;
  }

  public String getIdentifiant() {
    return identifiant;
  }
}
