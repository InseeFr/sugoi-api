package fr.insee.sugoi.sugoiapicore.service;

import java.util.List;

import fr.insee.sugoi.model.Realm;

public interface ConfigService {

    Realm getRealm(String name);

    List<Realm> getRealms();
}
