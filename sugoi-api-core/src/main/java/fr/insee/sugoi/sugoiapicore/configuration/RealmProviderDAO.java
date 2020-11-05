package fr.insee.sugoi.sugoiapicore.configuration;

import java.util.List;

import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.sugoiapicore.utils.Exceptions.RealmNotFoundException;

/**
 * This class is in charge to load the realm configuration on a specified
 * datasource
 */
public interface RealmProviderDAO {
    public Realm load(String realmName) throws RealmNotFoundException;

    public List<Realm> findAll();
}
