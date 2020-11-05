package fr.insee.sugoi.converter.ouganext;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Java class for HabilitationsType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="HabilitationsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="application" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="role" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@JacksonXmlRootElement(localName = "Habilitations", namespace = Namespace.ANNUAIRE)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonPropertyOrder({ "application" })
public class Habilitations {

  @JacksonXmlProperty(namespace = Namespace.ANNUAIRE)
  protected List<Application> application = new ArrayList<Application>();

  /**
   * @return la liste des applications avec habilitations ou une liste vide.
   */
  public List<Application> getApplication() {
    if (application == null) {
      application = new ArrayList<Application>();
    }
    return this.application;
  }

  public void addApplication(Application application) {
    this.application.add(application);
  }

  public void setApplicationList(List<Application> applicationList) {
    this.application = applicationList;
  }

  // /**
  // * Ajoute les {@link Role} passés en paramètre pour l'{@link Application}
  // * correspondant au nom appName passé en paramètre.<br/>
  // * Si les {@link Habilitations} ne contiennent pas déjà de {@link Role} pour
  // * l'{@link Application}, l'{@link Application} est créée.<br>
  // * Si le {@link Role} n'est pas encore existant pour l'{@link Application}, il
  // * est créé.<br>
  // * Si le {@link Role} ne possède pas de propriétés, la propriete est
  // renseignée
  // * à vide.<br>
  // * Si l'un des {@link Role} est en doublon, il n'est pas ajouté. A VOIR POUR
  // PAS
  // * CREER PLUSIEURS MEMES ROLES AVEC CHACUN UNE PROPRIETE.
  // *
  // * @param appName : nom de l'{@link Application} pour laquelle des {@link
  // Role}
  // * seront ajoutés.
  // * @param role : {@link Role} à ajouter.
  // */
  // public void addApplicationRole(String appName, Role... role) {
  // List<Role> rolesApp;
  // if (getApplication().contains(new Application(appName))) {
  // Application app = new Application(appName);
  // rolesApp = application.get(application.indexOf(app)).getRole();
  // } else {
  // Application app = new Application();
  // app.setName(appName);
  // application.add(app);
  // rolesApp = application.get(application.indexOf(app)).getRole();
  // }
  // for (Role roleUnique : role) {
  // if (!rolesApp.contains(roleUnique)) {
  // if (roleUnique.getPropriete().isEmpty()) {
  // roleUnique.getPropriete().add("");
  // }
  // rolesApp.add(roleUnique);
  // } else if (!roleUnique.getPropriete().isEmpty()) {
  // // Cadeau !
  // // pour chaque propriete du role actuel:
  // // * on vérifie si il n'est pas déjà présent dans les propriété du role
  // // (indépendemment de
  // // la casse) (-> filter)
  // // * si non, on l'ajoute dans dans le role
  // roleUnique.getPropriete().removeIf(s ->
  // rolesApp.get(rolesApp.indexOf(roleUnique)).getPropriete().stream()
  // .map(String::toLowerCase).collect(Collectors.toList()).contains(s.toLowerCase()));
  // roleUnique.getPropriete().forEach(p ->
  // rolesApp.get(rolesApp.indexOf(roleUnique)).getPropriete().add(p));
  // }
  // }

}
