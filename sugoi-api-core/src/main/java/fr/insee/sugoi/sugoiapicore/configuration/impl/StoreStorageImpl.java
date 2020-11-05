package fr.insee.sugoi.sugoiapicore.configuration.impl;

import java.util.HashMap;
import java.util.Map;

import com.unboundid.ldap.sdk.LDAPException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.sugoi.model.Technique.Store;
import fr.insee.sugoi.sugoiapicore.configuration.StoreStorage;
import fr.insee.sugoi.sugoiapicore.dao.ldap.LdapReaderStore;
import fr.insee.sugoi.sugoiapicore.dao.ldap.LdapWriterStore;

@Component
public class StoreStorageImpl implements StoreStorage {

    private static final Logger logger = LogManager.getLogger(StoreStorageImpl.class);

    private static final Map<String, Store> connections = new HashMap<>();

    @Override
    public Store getStore(Map<String, String> config) {
        if (!connections.containsKey(config.get("name"))) {
            try {
                if (config.get("type").equalsIgnoreCase("ldap")) {
                    logger.info("Création de la configuration de type LdapConfiguration pour {]", config.get("name"));
                    connections.put(config.get("name"),
                            new Store(new LdapReaderStore(config), new LdapWriterStore(config)));
                }
            } catch (LDAPException e) {
                logger.info("Erreur lors du chargement du UserStorage {}", config.get("name"));
            }
        }
        return connections.get(config.get("name"));
    }

}
