package fr.insee.sugoi.converter.ouganext;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "role", namespace = Namespace.ANNUAIRE)
@JsonPropertyOrder({ "name", "propriete" })
public class Role {

  public Role() {
    super();
  }

  public Role(String name) {
    super();
    this.name = name;
  }

  @XmlAttribute(name = "name")
  private String name;

  @XmlElement
  private List<String> propriete;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return la liste des propriétés ou une liste vide.
   */
  public List<String> getPropriete() {
    if (propriete == null) {
      propriete = new ArrayList<String>();
    }
    return propriete;
  }

}
