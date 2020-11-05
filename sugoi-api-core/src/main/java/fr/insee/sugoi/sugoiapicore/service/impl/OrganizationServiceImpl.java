package fr.insee.sugoi.sugoiapicore.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.sugoiapicore.configuration.RealmStorage;
import fr.insee.sugoi.sugoiapicore.service.OrganizationService;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    @Autowired
    private RealmStorage realmStorage;

    @Override
    public Organization searchOrganization(String realmName, String name) {
        return realmStorage.getRealm(realmName).getUserStorages().get(0).getStore().getReader()
                .searchOrganization(realmName, name);
    }

}
