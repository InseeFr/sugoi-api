package fr.insee.sugoi.sugoiapicore.mapper;

import com.unboundid.ldap.sdk.SearchResultEntry;

import fr.insee.sugoi.model.User;
import fr.insee.sugoi.sugoiapicore.mapper.properties.UserLdap;

public class UserLdapMapper {

    public static User mapFromSearchEntry(SearchResultEntry searchResultEntry) {
        User user = GenericLdapMapper.transform(searchResultEntry, UserLdap.class, User.class);
        return user;
    }

}
