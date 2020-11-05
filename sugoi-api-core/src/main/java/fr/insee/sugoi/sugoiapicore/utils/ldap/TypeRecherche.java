package fr.insee.sugoi.sugoiapicore.utils.ldap;

import java.util.ArrayList;
import java.util.List;

public enum TypeRecherche {

    ET("et"), OU("ou");

    private String typeRechercheLitteral;

    private TypeRecherche(String typeRecherche) {
        this.typeRechercheLitteral = typeRecherche;
    }

    public String getTypeRecherche() {
        return typeRechercheLitteral;
    }

    /**
     * Vérifie si typeRecherche correspond à OU ou ET.
     * 
     * @param typeRechercheEntre chaine de caractère à tester
     * @return tru si typeRechercheEntre est assimilable à OU ou ET
     */
    public boolean estTypeRecherche(String typeRechercheEntre) {
        for (String typeRecherche : listerTypeRecherche()) {
            if (typeRechercheEntre.toLowerCase().equals(typeRecherche)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> listerTypeRecherche() {
        List<String> typesRecherche = new ArrayList<String>();
        for (TypeRecherche typeRecherche : TypeRecherche.values()) {
            typesRecherche.add(typeRecherche.getTypeRecherche());
        }
        return typesRecherche;
    }

}
