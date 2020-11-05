package fr.insee.sugoi.sugoiapicore.mapper;

import java.util.HashMap;
import java.util.Map;

import com.unboundid.ldap.sdk.SearchResultEntry;

public class AddressLdapMapper {

    public static Map<String, String> mapFromSearchEntry(SearchResultEntry searchResultEntry) {
        Map<String, String> address = new HashMap<>();
        address.put("Ligne1", searchResultEntry.getAttributeValue("inseeAdressePostaleCorrespondantLigne1"));
        address.put("Ligne2", searchResultEntry.getAttributeValue("inseeAdressePostaleCorrespondantLigne2"));
        address.put("Ligne3", searchResultEntry.getAttributeValue("inseeAdressePostaleCorrespondantLigne3"));
        address.put("Ligne4", searchResultEntry.getAttributeValue("inseeAdressePostaleCorrespondantLigne4"));
        address.put("Ligne5", searchResultEntry.getAttributeValue("inseeAdressePostaleCorrespondantLigne5"));
        address.put("Ligne6", searchResultEntry.getAttributeValue("inseeAdressePostaleCorrespondantLigne6"));
        address.put("Ligne7", searchResultEntry.getAttributeValue("inseeAdressePostaleCorrespondantLigne7"));
        return address;
    }
}
