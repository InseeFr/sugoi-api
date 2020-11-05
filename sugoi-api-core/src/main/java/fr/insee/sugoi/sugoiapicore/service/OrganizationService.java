package fr.insee.sugoi.sugoiapicore.service;

import fr.insee.sugoi.model.Organization;

public interface OrganizationService {

    Organization searchOrganization(String realm, String name);

}
