package fr.insee.sugoi.sugoiapicore.utils.ldap;

import java.util.HashMap;
import java.util.Map;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;

import org.springframework.stereotype.Component;

@Component
public class LdapFactory {

    // private static final Logger logger = LogManager.getLogger(LdapUtils.class);

    private static final Map<String, LDAPConnectionPool> openLdapPoolConnection = new HashMap<>();
    private static final Map<String, LDAPConnection> openLdapMonoConnection = new HashMap<>();

    /**
     * Give a Ldap Connection Pool
     *
     * @param url
     * @return
     * @throws LDAPException
     */
    public static LDAPConnectionPool getConnectionPool(Map<String, String> config) throws LDAPException {
        // Check if a ldap connection pool already exist for this userStorage and create
        // it if it doesn't exist
        if (!openLdapPoolConnection.containsKey(config.get("name"))) {
            openLdapPoolConnection.put(config.get("name"), new LDAPConnectionPool(
                    new LDAPConnection(config.get("url"), 389), Integer.valueOf(config.get("pool_size"))));
        }
        return openLdapPoolConnection.get(config.get("name"));
    }

    public static LDAPConnection getSingleConnection(Map<String, String> config) throws LDAPException {
        if (!openLdapMonoConnection.containsKey(config.get("name"))) {
            openLdapMonoConnection.put(config.get("name"), new LDAPConnection(config.get("url"), 389));
        }
        return openLdapMonoConnection.get(config.get("name"));
    }
}
