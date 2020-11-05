package fr.insee.sugoi.sugoiapicore.mapper;

import java.util.List;

import com.unboundid.ldap.sdk.SearchResultEntry;

import org.springframework.stereotype.Component;

import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;

/**
 * ProfilContextMapper
 */
@Component
public class RealmLdapMapper {

    public Realm mapFromSearchEntry(SearchResultEntry searchResultEntry) {
        Realm realm = new Realm();
        UserStorage userStorage = new UserStorage();
        realm.setName(searchResultEntry.getAttributeValue("cn").split("_")[1]);
        String[] inseeProperties = searchResultEntry.getAttributeValues("inseepropriete");
        for (String inseeProperty : inseeProperties) {
            String[] property = inseeProperty.split("\\$");
            // Test if property is valid
            if (property.length == 2) {
                if (property[0].equals("ldapUrl")) {
                    realm.setUrl(property[1]);
                }
                if (property[0].contains("branchesApplicativePossibles")) {
                    realm.setAppBranch(property[1]);
                }

                if (property[0].contains("brancheContact")) {
                    userStorage.setUserBranch(property[1]);
                }
                if (property[0].equals("brancheOrganisation")) {
                    userStorage.setOrganizationBranch(property[1]);
                }
            }
        }
        realm.setUserStorages(List.of(userStorage));
        return realm;
    }

}