package fr.insee.sugoi.sugoiapicore.configuration;

import java.util.List;

import fr.insee.sugoi.model.Realm;

/**
 * This class manage the different configuration on the current realm
 */
public interface RealmStorage {

    public Realm getRealm(String realmName);

    public List<Realm> getRealms();
}