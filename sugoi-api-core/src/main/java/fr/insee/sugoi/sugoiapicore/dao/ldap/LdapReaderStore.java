package fr.insee.sugoi.sugoiapicore.dao.ldap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

import fr.insee.sugoi.model.Habilitation;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.Technique.MyPage;
import fr.insee.sugoi.model.Technique.MyPageable;
import fr.insee.sugoi.model.Technique.ReaderStore;
import fr.insee.sugoi.sugoiapicore.mapper.AddressLdapMapper;
import fr.insee.sugoi.sugoiapicore.mapper.OrganizationLdapMapper;
import fr.insee.sugoi.sugoiapicore.mapper.UserLdapMapper;
import fr.insee.sugoi.sugoiapicore.utils.Exceptions.EntityNotFoundException;
import fr.insee.sugoi.sugoiapicore.utils.ldap.LdapFactory;
import fr.insee.sugoi.sugoiapicore.utils.ldap.LdapUtils;

public class LdapReaderStore implements ReaderStore {

	private LDAPConnection ldapConnection;
	private LDAPConnectionPool ldapPoolConnection;

	private Map<String, String> config;

	public LdapReaderStore(Map<String, String> config) throws LDAPException {
		this.ldapConnection = LdapFactory.getSingleConnection(config);
		this.ldapPoolConnection = LdapFactory.getConnectionPool(config);
		this.config = config;
	}

	@Override
	public User searchUser(String domaine, String id) {
		SearchResultEntry entry = getEntryByDn("uid=" + id + "," + config.get("user_branch"));
		return UserLdapMapper.mapFromSearchEntry(entry);
	}

	@Override
	public Organization searchOrganization(String domaine, String id) {
		SearchResultEntry entry = getEntryByDn("uid=" + id + "," + config.get("organization_branch"));
		Organization org = OrganizationLdapMapper.mapFromSearchEntry(entry);
		if (org.getAttributes().containsKey("adressDn")) {
			SearchResultEntry result = getEntryByDn(org.getAttributes().get("adressDn").toString());
			org.setAddress(AddressLdapMapper.mapFromSearchEntry(result));
		}
		return org;
	}

	@Override
	public MyPage<User> searchUsers(String identifiant, String nomCommun, String description, String organisationId,
			String domaineGestion, String mail, MyPageable pageable, String typeRecherche, List<String> habilitations,
			String application, String role, String rolePropriete, String certificat) {
		try {
			MyPage<User> page = new MyPage<>();
			Filter filter = LdapUtils.filterRechercher(typeRecherche, identifiant, nomCommun, description,
					organisationId, mail, pageable, habilitations, certificat);
			SearchRequest searchRequest = new SearchRequest(config.get("user_branch"), SearchScope.SUBORDINATE_SUBTREE,
					filter, "*", "+");
			LdapUtils.setRequestControls(searchRequest, pageable);
			SearchResult searchResult = ldapPoolConnection.search(searchRequest);
			List<User> users = searchResult.getSearchEntries().stream().map(e -> UserLdapMapper.mapFromSearchEntry(e))
					.collect(Collectors.toList());
			LdapUtils.setResponseControls(page, searchResult);
			page.setResults(users);
			return page;
		} catch (LDAPSearchException e) {
			throw new RuntimeException("Impossible de recupérer les utilisateurs du domaine " + domaineGestion);
		}
	}

	public SearchResultEntry getEntryByDn(String dn) {
		try {
			SearchResultEntry entry = ldapPoolConnection.getEntry(dn, "+", "*");
			return entry;
		} catch (LDAPException e) {
			throw new EntityNotFoundException("Entry not found");
		}
	}

	@Override
	public Habilitation getHabilitation(String domaine, String id) {
		// TODO Auto-generated method stub
		return null;
	}

}
