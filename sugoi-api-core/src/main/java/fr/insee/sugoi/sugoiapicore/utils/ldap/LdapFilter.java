package fr.insee.sugoi.sugoiapicore.utils.ldap;

import java.util.List;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;

public class LdapFilter {

    public static Filter exists(String property) {
        return Filter.createPresenceFilter(property);
    }

    public static Filter contains(String property, String value) {
        return Filter.createSubAnyFilter(property, value);
    }

    public static Filter equalsProperty(String property, String value) {
        return Filter.createEqualityFilter(property, value);
    }

    public static Filter likeFilterOrganisation(String value) {
        return Filter.createSubInitialFilter("inseeOrganisationDN", "uid=" + encodeString(value));
    }

    public static Filter lessThan(String property, String value) {
        return Filter.createLessOrEqualFilter(property, value);
    }

    public static Filter greaterThan(String property, String value) {
        return Filter.createGreaterOrEqualFilter(property, value);
    }

    public static Filter and(List<Filter> andFilterCollection) {
        return Filter.createANDFilter(andFilterCollection);
    }

    public static Filter or(List<Filter> orFilterCollection) {
        return Filter.createORFilter(orFilterCollection);
    }

    public static Filter not(Filter filter) {
        return Filter.createNOTFilter(filter);
    }

    public static Filter create(String filter) throws LDAPException {
        return Filter.create(filter);
    }

    private static String encodeString(String propertyValue) {
        return Filter.encodeValue(propertyValue);
    }

}
