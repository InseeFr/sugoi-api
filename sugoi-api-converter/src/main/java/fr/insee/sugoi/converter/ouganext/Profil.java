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
import fr.insee.sugoi.converter.utils.MapFromAttribute;
import java.util.Arrays;
import java.util.List;

@JsonPropertyOrder({
  "nomProfil",
  "ldapUrl",
  "port",
  "brancheContact",
  "brancheAdresse",
  "brancheOrganisation",
  "mailUnicity",
  "idGlobalUnicity",
  "passwordReleasedAllowed",
  "pwdResetAllowed",
  "verifyChangePassword",
  "longueurMiniPassword",
  "nombreMaxHabilitations",
  "vlvSupported",
  "vlvEnabled",
  "pagingSupported",
  "objectClassContact",
  "habilitationsPossibles",
  "branchesApplicativesPossibles",
  "groupesPossibles"
})
@JacksonXmlRootElement(localName = "Profil", namespace = Namespace.ANNUAIRE)
public class Profil {

  public static final ObjectClass[] TABLEAU_CLASSES_DEFAUT = {
    ObjectClass.TOP,
    ObjectClass.INSEE_COMPTE,
    ObjectClass.INSEE_CONTACT,
    ObjectClass.INSEE_ATTRIBUTS_AUTHENTIFICATION,
    ObjectClass.INSEE_ATTRIBUTS_HABILITATION,
    ObjectClass.INSEE_ATTRIBUTS_COMMUNICATION
  };

  @JacksonXmlProperty
  @MapFromAttribute(attributeName = "url")
  private String ldapUrl;

  @JacksonXmlProperty private int port;

  @JacksonXmlProperty private String username;

  @JacksonXmlProperty private String password;

  // Autorisation de mettre le mot de passe dans le DLM
  @JacksonXmlProperty private boolean passwordReleasedAllowed = false;

  @JacksonXmlProperty private boolean verifyChangePassword = false;

  @JacksonXmlProperty private String brancheContact;

  @JacksonXmlProperty private String brancheAdresse;

  @JacksonXmlProperty private String brancheOrganisation;

  @JacksonXmlProperty private String nomProfil;

  // Valorisation de l'attribut pwdreset lors d'une réinitialisation de mdp
  @JacksonXmlProperty private boolean pwdResetAllowed = false;

  // Vérification de l'unicité du mail au sein du DDG
  @JacksonXmlProperty private boolean mailUnicity = false;

  // Longueur minimale de mot de passe imposée
  @JacksonXmlProperty private int longueurMiniPassword = 8;

  // L'unicité globale des identifiants doit etre controlé
  @JacksonXmlProperty private boolean idGlobalUnicity = true;

  // Nombre maximal d'habilitation par contact
  @JacksonXmlProperty private int nombreMaxHabilitations = 50;

  // objectClass des entrees
  @JacksonXmlProperty
  private List<ObjectClass> objectClassContact = Arrays.asList(TABLEAU_CLASSES_DEFAUT);

  // Habilitation possibles
  @JacksonXmlProperty private Habilitations habilitationsPossibles;

  // Branches applicatives dont la gestion des groupes est autorisée
  @JacksonXmlProperty private List<String> branchesApplicativesPossibles;
  // groupes déduits du champ precedent
  @JacksonXmlProperty private List<Groupe> groupesPossibles;

  // Support de la pagination ou du vlv
  @JacksonXmlProperty private boolean vlvSupported = false;

  @JacksonXmlProperty private boolean pagingSupported = false;

  // autorisation de l'usage de vlv dans les recherches
  @JacksonXmlProperty private boolean vlvEnabled = false;

  @Override
  public String toString() {
    return "Profil [ldapUrl="
        + ldapUrl
        + ", port="
        + port
        + ", username="
        + username
        + ", password="
        + password
        + ", passwordReleasedAllowed="
        + passwordReleasedAllowed
        + ", verifyChangePassword="
        + verifyChangePassword
        + ", brancheContact="
        + brancheContact
        + ", brancheAdresse="
        + brancheAdresse
        + ", brancheOrganisation="
        + brancheOrganisation
        + ", nomProfil="
        + nomProfil
        + ", pwdResetAllowed="
        + pwdResetAllowed
        + ", mailUnicity="
        + mailUnicity
        + ", longueurMiniPassword="
        + longueurMiniPassword
        + ", vlvSupported="
        + vlvSupported
        + ", pagingSupported="
        + pagingSupported
        + "]";
  }

  public String getLdapUrl() {
    return ldapUrl;
  }

  public void setLdapUrl(String ldapUrl) {
    this.ldapUrl = ldapUrl;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isPasswordReleasedAllowed() {
    return passwordReleasedAllowed;
  }

  public void setPasswordReleasedAllowed(boolean passwordReleasedAllowed) {
    this.passwordReleasedAllowed = passwordReleasedAllowed;
  }

  public boolean isVerifyChangePassword() {
    return verifyChangePassword;
  }

  public void setVerifyChangePassword(boolean verifyChangePassword) {
    this.verifyChangePassword = verifyChangePassword;
  }

  public String getBrancheContact() {
    return brancheContact;
  }

  public void setBrancheContact(String brancheContact) {
    this.brancheContact = brancheContact;
  }

  public String getBrancheAdresse() {
    return brancheAdresse;
  }

  public void setBrancheAdresse(String brancheAdresse) {
    this.brancheAdresse = brancheAdresse;
  }

  public String getBrancheOrganisation() {
    return brancheOrganisation;
  }

  public void setBrancheOrganisation(String brancheOrganisation) {
    this.brancheOrganisation = brancheOrganisation;
  }

  public String getNomProfil() {
    return nomProfil;
  }

  public void setNomProfil(String nomProfil) {
    this.nomProfil = nomProfil;
  }

  public boolean isPwdResetAllowed() {
    return pwdResetAllowed;
  }

  public void setPwdResetAllowed(boolean pwdResetAllowed) {
    this.pwdResetAllowed = pwdResetAllowed;
  }

  public boolean isMailUnicity() {
    return mailUnicity;
  }

  public void setMailUnicity(boolean mailUnicity) {
    this.mailUnicity = mailUnicity;
  }

  public int getLongueurMiniPassword() {
    return longueurMiniPassword;
  }

  public void setLongueurMiniPassword(int longueurMiniPassword) {
    this.longueurMiniPassword = longueurMiniPassword;
  }

  public boolean isVlvSupported() {
    return vlvSupported;
  }

  public void setVlvSupported(boolean vlvSupported) {
    this.vlvSupported = vlvSupported;
  }

  public boolean isPagingSupported() {
    return pagingSupported;
  }

  public void setPagingSupported(boolean pagingSupported) {
    this.pagingSupported = pagingSupported;
  }

  public boolean isVlvEnabled() {
    return vlvEnabled;
  }

  public void setVlvEnabled(boolean vlvEnabled) {
    this.vlvEnabled = vlvEnabled;
  }

  public List<ObjectClass> getObjectClassContact() {
    return objectClassContact;
  }

  public void setObjectClassContact(List<ObjectClass> objectClass) {
    this.objectClassContact = objectClass;
  }

  public boolean isIdGlobalUnicity() {
    return idGlobalUnicity;
  }

  public void setIdGlobalUnicity(boolean idUnicity) {
    this.idGlobalUnicity = idUnicity;
  }

  public int getNombreMaxHabilitations() {
    return nombreMaxHabilitations;
  }

  public void setNombreMaxHabilitations(int nombreMaxHabilitations) {
    this.nombreMaxHabilitations = nombreMaxHabilitations;
  }

  public Habilitations getHabilitationsPossibles() {
    return habilitationsPossibles;
  }

  public void setHabilitationsPossibles(Habilitations habilitationsPossibles) {
    this.habilitationsPossibles = habilitationsPossibles;
  }

  public List<String> getBranchesApplicativesPossibles() {
    return branchesApplicativesPossibles;
  }

  public void setBranchesApplicativesPossibles(List<String> branchesApplicativesPossibles) {
    this.branchesApplicativesPossibles = branchesApplicativesPossibles;
  }

  public List<Groupe> getGroupesPossibles() {
    return groupesPossibles;
  }

  public void setGroupesPossibles(List<Groupe> groupesPossibles) {
    this.groupesPossibles = groupesPossibles;
  }
}
