package fr.insee.sugoi.sugoiapicore.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.sugoiapicore.configuration.RealmStorage;
import fr.insee.sugoi.sugoiapicore.service.ConfigService;

/**
 * ConfigServiceImpl
 */
@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private RealmStorage realmStorage;

    @Override
    public Realm getRealm(String name) {
        return realmStorage.getRealm(name);
    }

    @Override
    public List<Realm> getRealms() {
        return realmStorage.getRealms();
    }

}