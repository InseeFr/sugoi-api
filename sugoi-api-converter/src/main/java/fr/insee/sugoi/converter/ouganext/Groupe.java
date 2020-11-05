package fr.insee.sugoi.converter.ouganext;

import com.fasterxml.jackson.annotation.JsonAlias;

public class Groupe {

  @JsonAlias({ "application", "Application" })
  private String application;
  @JsonAlias({ "nom", "Nom" })
  private String nom;

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public String getNom() {
    return nom;
  }

  public void setNom(String nom) {
    this.nom = nom;
  }

}
