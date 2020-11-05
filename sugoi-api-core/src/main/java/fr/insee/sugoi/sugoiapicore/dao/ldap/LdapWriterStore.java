package fr.insee.sugoi.sugoiapicore.dao.ldap;

import java.util.Map;

import com.unboundid.ldap.sdk.DeleteRequest;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;

import fr.insee.sugoi.model.Technique.WriterStore;
import fr.insee.sugoi.sugoiapicore.utils.ldap.LdapFactory;

public class LdapWriterStore implements WriterStore {

    private LDAPConnection ldapConnection;
    private LDAPConnectionPool ldapPoolConnection;
    private LdapReaderStore ldapReader;

    private Map<String, String> config;

    public LdapWriterStore(Map<String, String> config) throws LDAPException {
        this.ldapConnection = LdapFactory.getSingleConnection(config);
        this.ldapPoolConnection = LdapFactory.getConnectionPool(config);
        this.ldapReader = new LdapReaderStore(config);
        this.config = config;
    }

    @Override
    public String deleteUser(String domain, String id) {
        try {
            DeleteRequest dr = new DeleteRequest("uid=" + id + "," + config.get("user_branch"));
            ldapPoolConnection.delete(dr);
        } catch (LDAPException e) {
            throw new RuntimeException("Impossible de supprimer l'utilisateur");
        }
        return null;
    }

}
