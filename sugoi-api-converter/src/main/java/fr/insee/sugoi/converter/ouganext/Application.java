package fr.insee.sugoi.converter.ouganext;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import fr.insee.sugoi.converter.utils.MapFromAttribute;

@JacksonXmlRootElement(localName = "application", namespace = Namespace.ANNUAIRE)
@JsonPropertyOrder({ "name", "role" })
public class Application {

  @JacksonXmlProperty(namespace = Namespace.ANNUAIRE)
  @MapFromAttribute(attributeName = "name")
  private String name;
  @JacksonXmlProperty(namespace = Namespace.ANNUAIRE)
  private Collection<Role> role = new ArrayList<>();

  public Application(String appName) {
    this.name = appName;
  }

  public Application() {

  }

  @XmlAttribute(name = "name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return la liste des roles ou une liste vide.
   */
  public Collection<Role> getRole() {
    if (role == null) {
      role = new ArrayList<Role>();
    }
    return role;
  }

  public void addRole(Role role) {
    this.role.add(role);
  }

}