/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fr.insee.sugoi.ldap.utils.mapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.SearchResultEntry;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.model.Realm;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.Test;

public class RealmLdapMapperFromAttributesTest {

  @Test
  public void getRealmObjectFromAttributes() {
    String[] values = {
      "branchesApplicativesPossibles$ou=applications,o=insee,c=fr",
      "groupSourcePattern$ou={appliname}_Objets,ou={appliname},ou=Applications,o=insee,c=fr",
      "groupFilterPattern$(cn={group}_{appliname})"
    };
    Attribute brancheContactAttribute = new Attribute("inseepropriete", values);
    Attribute cnAttribute = new Attribute("cn", "Profil_RP_Sugoi");

    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(cnAttribute);
    attributes.add(brancheContactAttribute);
    SearchResultEntry searchResultEntry = new SearchResultEntry("null", attributes, new Control[0]);
    Realm realm = RealmLdapMapper.mapFromSearchEntry(searchResultEntry);
    assertThat(
        "Should have group source pattern",
        realm.getProperties().get(LdapConfigKeys.GROUP_SOURCE_PATTERN),
        is("ou={appliname}_Objets,ou={appliname},ou=Applications,o=insee,c=fr"));
    assertThat(
        "Should have application source", realm.getAppSource(), is("ou=applications,o=insee,c=fr"));
    assertThat(
        "Should have group filter pattern",
        realm.getProperties().get(LdapConfigKeys.GROUP_FILTER_PATTERN),
        is("(cn={group}_{appliname})"));
  }
}
