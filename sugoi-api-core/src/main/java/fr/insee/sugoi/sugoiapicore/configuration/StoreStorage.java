package fr.insee.sugoi.sugoiapicore.configuration;

import java.util.Map;

import fr.insee.sugoi.model.Technique.Store;

/**
 * Class to get a connection associated to a specified userStorageConfig
 */
public interface StoreStorage {

    public Store getStore(Map<String, String> config);

}
