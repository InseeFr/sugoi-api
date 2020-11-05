package fr.insee.sugoi.converter.ouganext;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonPropertyOrder({ "liste" })
@JacksonXmlRootElement(localName = "Profils", namespace = Namespace.INSEE)
public class Profils {

  @JacksonXmlProperty(localName = "Profil", namespace = Namespace.ANNUAIRE)
  private List<Profil> liste;

  /**
   * @return la liste des profils ou une liste vide.
   */
  public List<Profil> getListe() {
    if (liste == null) {
      liste = new ArrayList<Profil>();
    }
    return this.liste;
  }

}
