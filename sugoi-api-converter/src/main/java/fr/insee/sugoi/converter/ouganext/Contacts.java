package fr.insee.sugoi.converter.ouganext;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "ListeCompte", namespace = Namespace.ANNUAIRE)
@JsonPropertyOrder({ "liste" })
public class Contacts {

  @JacksonXmlProperty(localName = "Contact", namespace = Namespace.ANNUAIRE)
  @JacksonXmlElementWrapper(useWrapping = false)
  protected List<Contact> liste = new ArrayList<Contact>();

  public List<Contact> getListe() {
    return this.liste;
  }

}
