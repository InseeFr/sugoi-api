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
import fr.insee.sugoi.model.Habilitation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Java class for HabilitationsType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
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
 */
@JacksonXmlRootElement(localName = "Habilitations", namespace = Namespace.ANNUAIRE)
@XmlAccessorType(XmlAccessType.FIELD)
@JsonPropertyOrder({"application"})
public class Habilitations {

  public Habilitations() {}

  public Habilitations(List<Habilitation> habilitations) {
    List<Habilitation> filteredHabilitations =
        habilitations.stream()
            .filter(habilitation -> habilitation.getApplication() != null)
            .collect(Collectors.toList());
    this.application =
        filteredHabilitations.stream()
            .map(habilitation -> habilitation.getApplication())
            .distinct()
            .map(
                appName -> {
                  return new Application(appName);
                })
            .collect(Collectors.toList());
    application.forEach(
        app -> {
          filteredHabilitations.stream()
              .filter(habilitation -> habilitation.getApplication().equals(app.getName()))
              .map(habilitation -> habilitation.getRole())
              .distinct()
              .forEach(roleName -> app.addRole(new Role(roleName)));
        });
    application.forEach(
        app ->
            app.getRole()
                .forEach(
                    role -> {
                      filteredHabilitations.stream()
                          .filter(
                              habilitation -> habilitation.getApplication().equals(app.getName()))
                          .filter(habilitation -> habilitation.getRole().equals(role.getName()))
                          .map(habilitation -> habilitation.getProperty())
                          .filter(property -> property != null)
                          .distinct()
                          .forEach(
                              property -> {
                                role.addPropriete(property);
                              });
                    }));
  }

  public List<Habilitation> convertSugoiHabilitation() {
    List<Habilitation> habilitations = new ArrayList<>();
    this.application.stream()
        .forEach(
            app -> {
              app.getRole().stream()
                  .forEach(
                      role -> {
                        if (role.getPropriete().size() == 0) {
                          habilitations.add(new Habilitation(app.getName(), role.getName(), null));
                        } else {
                          role.getPropriete().stream()
                              .forEach(
                                  propriete ->
                                      habilitations.add(
                                          new Habilitation(
                                              app.getName(), role.getName(), propriete)));
                        }
                      });
            });
    return habilitations;
  }

  @JacksonXmlProperty(namespace = Namespace.ANNUAIRE)
  protected List<Application> application = new ArrayList<Application>();

  /** @return la liste des applications avec habilitations ou une liste vide. */
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

  public void addHabilitation(String appName, List<String> nomRoles) {
    if (!this.application.stream().anyMatch(app -> app.getName().equalsIgnoreCase(appName))) {
      this.application.add(new Application(appName));
    }
    Application appli =
        this.application.stream()
            .filter(app -> app.getName().equalsIgnoreCase(appName))
            .collect(Collectors.toList())
            .get(0);
    nomRoles.stream()
        .forEach(
            roleName -> {
              if (!appli.getRole().stream()
                  .anyMatch(role -> role.getName().equalsIgnoreCase(roleName))) {
                appli.addRole(new Role(roleName));
              }
            });
  }

  public void removeHabilitation(String appName, List<String> nomRoles) {
    if (this.application.stream().anyMatch(app -> app.getName().equalsIgnoreCase(appName))) {
      Application appli =
          this.application.stream()
              .filter(app -> app.getName().equalsIgnoreCase(appName))
              .collect(Collectors.toList())
              .get(0);
      nomRoles.stream().forEach(role -> appli.removeRole(role));
    }
  }

  public void removeHabilitation(String appName, String roleName, List<String> proprietes) {
    if (this.application.stream().anyMatch(app -> app.getName().equalsIgnoreCase(appName))) {
      Application appli =
          this.application.stream()
              .filter(app -> app.getName().equalsIgnoreCase(appName))
              .collect(Collectors.toList())
              .get(0);
      if (appli.getRole().stream()
          .anyMatch(roleFilter -> roleFilter.getName().equalsIgnoreCase(roleName))) {
        Role role =
            appli.getRole().stream()
                .filter(roleFilter -> roleFilter.getName().equalsIgnoreCase(roleName))
                .collect(Collectors.toList())
                .get(0);
        proprietes.stream().forEach(prop -> role.removePropriete(prop));
      }
    }
  }

  public void addHabilitations(String appName, String roleName, List<String> proprietes) {
    Application appli;
    Role role;
    if (!this.application.stream().anyMatch(app -> app.getName().equalsIgnoreCase(appName))) {
      appli = new Application(appName);
      this.application.add(appli);
      role = new Role(roleName);
      appli.addRole(role);
    } else {
      appli =
          this.application.stream()
              .filter(app -> app.getName().equalsIgnoreCase(appName))
              .collect(Collectors.toList())
              .get(0);
      if (appli.getRole().stream()
          .anyMatch(roleFilter -> roleFilter.getName().equalsIgnoreCase(roleName))) {
        role =
            appli.getRole().stream()
                .filter(roleFilter -> roleFilter.getName().equalsIgnoreCase(roleName))
                .collect(Collectors.toList())
                .get(0);
      } else {
        role = new Role(roleName);
        appli.addRole(role);
      }
    }
    proprietes.stream().forEach(prop -> role.addPropriete(prop));
    ;
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
