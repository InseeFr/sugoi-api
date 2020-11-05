package fr.insee.sugoi.converter.ouganext;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "ListeOrganisations", namespace = Namespace.ANNUAIRE)
@JsonPropertyOrder({ "liste" })
public class Organisations {

  @JacksonXmlProperty(localName = "Organisation", namespace = Namespace.ANNUAIRE)
  protected List<Organisation> liste = new ArrayList<Organisation>();

  public List<Organisation> getListe() {
    return this.liste;
  }

}
