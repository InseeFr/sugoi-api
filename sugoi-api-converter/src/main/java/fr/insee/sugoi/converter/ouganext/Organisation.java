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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import fr.insee.sugoi.converter.ouganext.adapters.OrganisationDeserializer;
import fr.insee.sugoi.converter.ouganext.adapters.OrganisationSerializer;
import fr.insee.sugoi.converter.utils.MapFromAttribute;
import fr.insee.sugoi.converter.utils.MapFromHashmapElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * Java class for OrganisationType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="OrganisationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="NomCommun" type="{http://www.w3.org/2001/XMLSchema}token"/>
 *         &lt;element name="DomaineDeGestion" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AdresseMessagerie" type="{http://www.w3.org/2005/Atom}emailType" minOccurs="0"/>
 *         &lt;element name="NumeroTelephone" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
 *         &lt;element name="AdressePostale" type="{http://xml.insee.fr/schema}AdressePostaleType" minOccurs="0"/>
 *         &lt;element name="CleDeChiffrement" type="{http://www.w3.org/2001/XMLSchema}byte" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="OrganisationDeRattachementUri" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
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
  "domaineDeGestion",
  "description",
  "adresseMessagerie",
  "numeroTelephone",
  "facSimile",
  "adresse",
  "cleDeChiffrement",
  "organisationDeRattachement",
  "repertoireDeDistribution",
  "propriete"
})
@JacksonXmlRootElement(localName = "Organisation", namespace = Namespace.ANNUAIRE)
public class Organisation {

  @JacksonXmlProperty(localName = "Identifiant", namespace = Namespace.ANNUAIRE)
  @MapFromAttribute(attributeName = "identifiant")
  protected String identifiant;

  @JacksonXmlProperty(localName = "NomCommun", namespace = Namespace.ANNUAIRE)
  @MapFromHashmapElement(hashMapName = "attributes", hashMapKey = "nomCommun")
  protected String nomCommun;

  @JacksonXmlProperty(localName = "DomaineDeGestion", namespace = Namespace.ANNUAIRE)
  @MapFromHashmapElement(hashMapName = "attributes", hashMapKey = "domaineDeGestion")
  protected String domaineDeGestion;

  @JacksonXmlProperty(localName = "AdresseMessagerie", namespace = Namespace.ANNUAIRE)
  @MapFromHashmapElement(hashMapName = "attributes", hashMapKey = "adresseMessagerie")
  protected String adresseMessagerie;

  @JacksonXmlProperty(localName = "NumeroTelephone", namespace = Namespace.ANNUAIRE)
  @MapFromHashmapElement(hashMapName = "attributes", hashMapKey = "numeroTelephone")
  protected String numeroTelephone;

  @JacksonXmlProperty(localName = "Description", namespace = Namespace.ANNUAIRE)
  @MapFromHashmapElement(hashMapName = "attributes", hashMapKey = "description")
  protected String description;

  @JacksonXmlProperty(localName = "FacSimile", namespace = Namespace.ANNUAIRE)
  @MapFromHashmapElement(hashMapName = "attributes", hashMapKey = "facSimile")
  protected String facSimile;

  @JacksonXmlProperty(localName = "adressePostale", namespace = Namespace.INSEE)
  protected Adresse adresse;

  @JacksonXmlProperty(localName = "CleDeChiffrement", namespace = Namespace.ANNUAIRE)
  @MapFromAttribute(attributeName = "gpgkey")
  protected byte[] cleDeChiffrement;

  @JacksonXmlProperty(localName = "OrganisationDeRattachementUri", namespace = Namespace.ANNUAIRE)
  @JsonSerialize(using = OrganisationSerializer.class)
  @JsonDeserialize(using = OrganisationDeserializer.class)
  protected Organisation organisationDeRattachement;

  @JacksonXmlProperty(localName = "RepertoireDeDistribution", namespace = Namespace.ANNUAIRE)
  @MapFromHashmapElement(hashMapName = "attributes", hashMapKey = "repertoireDeDistribution")
  protected String repertoireDeDistribution;

  @MapFromHashmapElement(hashMapName = "attributes", hashMapKey = "proprietes")
  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "Propriete", namespace = Namespace.ANNUAIRE)
  protected Collection<String> propriete;

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

  public void setCleDeChiffrement(byte[] cle) {
    this.cleDeChiffrement = cle;
  }

  public byte[] getCleDeChiffrement() {
    return this.cleDeChiffrement;
  }

  public Organisation getOrganisationDeRattachement() {
    return organisationDeRattachement;
  }

  public void setOrganisationDeRattachement(Organisation organisation) {
    this.organisationDeRattachement = organisation;
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

  public String toString() {
    return "Organisation [nomCommun="
        + nomCommun
        + ", domaineDeGestion="
        + domaineDeGestion
        + ", description="
        + description
        + ", adresseMessagerie="
        + adresseMessagerie
        + ", numeroTelephone="
        + numeroTelephone
        + ", facSimile="
        + facSimile
        + ", adresse="
        + adresse
        + ", cleDeChiffrement="
        + Arrays.toString(cleDeChiffrement)
        + ", organisationDeRattachement="
        + ((organisationDeRattachement == null) ? "" : organisationDeRattachement.getIdentifiant())
        + ", repertoireDeDistribution="
        + repertoireDeDistribution
        + ", identifiant="
        + identifiant
        + ", propriete="
        + propriete
        + "]";
  }
}
