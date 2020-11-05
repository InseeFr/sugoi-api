package fr.insee.sugoi.sugoiapicore.configuration.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.sugoiapicore.configuration.RealmProviderDAO;
import fr.insee.sugoi.sugoiapicore.mapper.RealmLdapMapper;
import fr.insee.sugoi.sugoiapicore.utils.Exceptions.RealmNotFoundException;
import fr.insee.sugoi.sugoiapicore.utils.ldap.LdapFilter;

@Component
/*
 * This class load the realm configuration from an ldap
 */
// @ConditionalOnProperty(value = "fr.insee.sugoi.realm.config.ldap")
public class LdapRealmProviderDAOImpl implements RealmProviderDAO {

    @Autowired
    private RealmLdapMapper realmMapper;

    @Value("${fr.insee.sugoi.ldap.profils.url:}")
    private String url;
    @Value("${fr.insee.sugoi.ldap.profils.port:}")
    private String port;
    @Value("${fr.insee.sugoi.ldap.profils.branche:}")
    private String baseDn;

    private static final Logger logger = LogManager.getLogger(LdapRealmProviderDAOImpl.class);

    public Realm load(String realmName) throws RealmNotFoundException {
        try {
            logger.info("Chargement de la conf sur le ldap dpii");
            LDAPConnectionPool ldapConnection = new LDAPConnectionPool(new LDAPConnection(url, 389), 1);
            SearchResultEntry entry = ldapConnection.getEntry("cn=Profil_" + realmName + "_WebServiceLdap," + baseDn);
            return realmMapper.mapFromSearchEntry(entry);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RealmNotFoundException("Erreur lors du chargement du realm " + realmName);
        }
    }

    @Override
    public List<Realm> findAll() {
        try {
            logger.info("Chargement des confs sur le ldap dpii");
            LDAPConnectionPool ldapConnection = new LDAPConnectionPool(new LDAPConnection(url, 389), 1);
            SearchRequest searchRequest = new SearchRequest(baseDn, SearchScope.ONE,
                    LdapFilter.create("(objectClass=*)"), "*", "+");
            SearchResult searchResult = ldapConnection.search(searchRequest);
            List<Realm> realms = searchResult.getSearchEntries().stream().map(e -> realmMapper.mapFromSearchEntry(e))
                    .collect(Collectors.toList());
            return realms;
        } catch (Exception e) {
            throw new RealmNotFoundException("Impossible de charger les realms");
        }
    }

}
