package fr.insee.sugoi.sugoiapicore.mapper;

import com.unboundid.ldap.sdk.SearchResultEntry;

import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.sugoiapicore.mapper.properties.OrganizationLdap;

public class OrganizationLdapMapper {

    public static Organization mapFromSearchEntry(SearchResultEntry searchResultEntry) {
        Organization org = GenericLdapMapper.transform(searchResultEntry, OrganizationLdap.class, Organization.class);
        org.setGpgkey(searchResultEntry.getAttribute("inseeClefChiffrement").getValueByteArray());
        return org;
    }

}
