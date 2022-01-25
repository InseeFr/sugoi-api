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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import com.unboundid.ldap.sdk.Attribute;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.fixtures.StoreMappingFixture;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = UserLdapMapper.class)
public class UserLdapMapperFromAttributesTest {

  UserLdapMapper userLdapMapper;

  @BeforeEach
  public void setup() {

    Map<String, String> config = new HashMap<>();
    config.put("organization_source", "ou=organisations,ou=clients_domaine1,o=insee,c=fr");
    config.put(
        "group_source_pattern",
        "ou={appliname}_Objets,ou={appliname},ou=Applications,o=insee,c=fr");

    userLdapMapper = new UserLdapMapper(config, StoreMappingFixture.getUserStoreMappings());
  }

  @Test
  public void getSimpleUserFromAttributes() {

    Attribute lastNameAttribute = new Attribute("sn", "Toto");
    Attribute firstNameAttribute = new Attribute("givenName", "Tata");
    Attribute mailAttribute = new Attribute("mail", "toto@tata.insee.fr");
    Attribute usernameAttribute = new Attribute("uid", "totoid");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(lastNameAttribute);
    attributes.add(firstNameAttribute);
    attributes.add(mailAttribute);
    attributes.add(usernameAttribute);
    User mappedUser = userLdapMapper.mapFromAttributes(attributes);

    assertThat("Should have a username", mappedUser.getUsername(), is("totoid"));
    assertThat("Should have a lastname", mappedUser.getLastName(), is("Toto"));
    assertThat("Should have a firstname", mappedUser.getFirstName(), is("Tata"));
    assertThat("Should have a mail", mappedUser.getMail(), is("toto@tata.insee.fr"));
  }

  @Test
  public void getUserAttributesFromAttributes() {

    Attribute commonNameAttribute = new Attribute("cn", "Toto Tata");
    Attribute personalTitleAttribute = new Attribute("personalTitle", "Camarade");
    Attribute descriptionAttribute = new Attribute("description", "ma description");
    Attribute telAttribute = new Attribute("telephoneNumber", "012345678");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(commonNameAttribute);
    attributes.add(personalTitleAttribute);
    attributes.add(descriptionAttribute);
    attributes.add(telAttribute);
    User mappedUser = userLdapMapper.mapFromAttributes(attributes);

    assertThat("Should hava a cn", mappedUser.getAttributes().get("common_name"), is("Toto Tata"));
    assertThat(
        "Should hava a phone_number",
        mappedUser.getAttributes().get("phone_number"),
        is("012345678"));
    assertThat(
        "Should hava a personalTitle",
        mappedUser.getAttributes().get("personal_title"),
        is("Camarade"));
    assertThat(
        "Should hava a description",
        mappedUser.getAttributes().get("description"),
        is("ma description"));
  }

  @Test
  public void getUserHabilitationsFromAttributes() {

    Attribute habilitationAttribute1 =
        new Attribute("inseeGroupeDefaut", "property_role_application");
    Attribute habilitationWithoutPropAttribute =
        new Attribute("inseeGroupeDefaut", "role_application2");
    Attribute malformedHabilitation = new Attribute("inseeGroupeDefaut", "toto");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(habilitationAttribute1);
    attributes.add(habilitationWithoutPropAttribute);
    attributes.add(malformedHabilitation);
    User mappedUser = userLdapMapper.mapFromAttributes(attributes);

    assertThat(
        "Should have habilitation habilitation1",
        mappedUser.getHabilitations().stream()
            .anyMatch(habilitation -> habilitation.getApplication().equals("application")));
    assertThat(
        "Should have habilitation without prop",
        mappedUser.getHabilitations().stream()
            .anyMatch(
                habilitation ->
                    habilitation.getApplication().equals("application2")
                        && habilitation.getProperty() == null));
    assertThat(
        "All habilitation have role",
        mappedUser.getHabilitations().stream()
            .allMatch(habilitation -> habilitation.getRole() != null));
  }

  @Test
  public void getUserOrganizationFromAttributes() {

    Attribute organizationAttribute =
        new Attribute(
            "inseeOrganisationDN", "uid=monOrga,ou=organisations,ou=clients_domaine1,o=insee,c=fr");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(organizationAttribute);
    User mappedUser = userLdapMapper.mapFromAttributes(attributes);

    assertThat(
        "Should have organization", mappedUser.getOrganization().getIdentifiant(), is("monOrga"));
  }

  @Test
  public void getUserAddressFromAttributes() {

    Attribute addressAttribute =
        new Attribute("inseeAdressePostaleDN", "l=generatedBefore,ou=address,o=insee,c=fr");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(addressAttribute);
    User mappedUser = userLdapMapper.mapFromAttributes(attributes);

    assertThat("Should have address id", mappedUser.getAddress().getId(), is("generatedBefore"));
  }

  @Test
  public void getUserGroupFromAttributes() {

    Attribute groupAttributes1 =
        new Attribute(
            "memberOf", "cn=admin,ou=monappli_Objets,ou=monappli,ou=Applications,o=insee,c=fr");
    Attribute groupAttributes2 =
        new Attribute(
            "memberOf", "cn=reader,ou=monappli_Objets,ou=monappli,ou=Applications,o=insee,c=fr");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(groupAttributes1);
    attributes.add(groupAttributes2);
    User mappedUser = userLdapMapper.mapFromAttributes(attributes);

    assertThat(
        "Should have admin group",
        mappedUser.getGroups().stream().anyMatch(group -> group.getName().equals("admin")));
    assertThat(
        "Admin group should have monappli app name",
        mappedUser.getGroups().stream().anyMatch(group -> group.getAppName().equals("monappli")));
    assertThat(
        "Should have admin group",
        mappedUser.getGroups().stream().anyMatch(group -> group.getName().equals("reader")));
  }

  @Test
  public void getInseeRolesApplicatifsFromAttributes() {

    Attribute inseeRoleAppAttribute1 = new Attribute("inseeRoleApplicatif", "toto");
    Attribute inseeRoleAppAttribute2 = new Attribute("inseeRoleApplicatif", "tata");
    Collection<Attribute> attributes = new ArrayList<>();
    attributes.add(inseeRoleAppAttribute1);
    attributes.add(inseeRoleAppAttribute2);
    User mappedUser = userLdapMapper.mapFromAttributes(attributes);
    @SuppressWarnings("unchecked")
    List<String> inseeRoleApplicatifs =
        (List<String>) mappedUser.getAttributes().get("insee_roles_applicatifs");

    assertThat(
        "Should have inseeRoleapplicatif tata",
        inseeRoleApplicatifs.stream().anyMatch(role -> role.equals("toto")));
    assertThat(
        "Should have inseeRoleapplicatif tata",
        inseeRoleApplicatifs.stream().anyMatch(role -> role.equals("tata")));
  }

  @Test
  public void getHasPasswordFromAttributeWhenPasswordIsSet() {
    Attribute passwordAttribute = new Attribute("userPassword", "mypassword");
    User mappedUser = userLdapMapper.mapFromAttributes(List.of(passwordAttribute));
    assertThat(mappedUser.getAttributes().get("hasPassword"), is(true));
  }

  @Test
  public void getHasPasswordFromAttributeWhenPasswordIsNotSet() {
    User mappedUser = userLdapMapper.mapFromAttributes(List.of());
    assertThat(mappedUser.getAttributes().get("hasPassword"), is(false));
  }

  @Test
  public void getUserCertificateFromAttributes() throws ParseException, CertificateException {
    Attribute certificatAttribute =
        new Attribute(
            "userCertificate",
            Base64.getDecoder()
                .decode(
                    "MIIDJDCCAgwCCQDzaF9oNeXFKTANBgkqhkiG9w0BAQsFADBUMQswCQYDVQQGEwJG"
                        + "UjEOMAwGA1UECAwFUGFyaXMxDjAMBgNVBAoMBUluc2VlMRIwEAYDVQQLDAlVbml0"
                        + "IFRlc3QxETAPBgNVBAMMCEpvaG4gRG9lMB4XDTIxMTExOTE3NDUxMloXDTIyMTEx"
                        + "OTE3NDUxMlowVDELMAkGA1UEBhMCRlIxDjAMBgNVBAgMBVBhcmlzMQ4wDAYDVQQK"
                        + "DAVJbnNlZTESMBAGA1UECwwJVW5pdCBUZXN0MREwDwYDVQQDDAhKb2huIERvZTCC"
                        + "ASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJ5YQ14T/YjlKwE341JrzMbQ"
                        + "58ZK6/4n3W194/txrIFMThyVMF76YxZj8qTcufqLHv6XXZtWMWupPhG2PtzhkAfL"
                        + "Cxeb+92HjKmCMRi35VvtMQn9VExmpm467tMnCoMdM50Y8FBKdvFJwDIbL48LqA11"
                        + "UyVwibyT9NPcjtd5Xr4ZOQqvoqonPbYp7Atbl1hEtVNkJNvU/W7I15u6NRzY6VvB"
                        + "UGwYR0z+/sGq3fPzEU7YQefaa1mJYKoT+A5ITDUDtT72SGU/WnYX2ShcpN6G8oWk"
                        + "BrH4DZk8r4nSGXDz6DQSwX7ssA/bHERf0oaLh/1f6zIh8HJISyzLGC998ALl2xsC"
                        + "AwEAATANBgkqhkiG9w0BAQsFAAOCAQEAHQ0p9QsU9kXMAjQKUkKgE6bGack2GzGJ"
                        + "CZEvlrOeqfYyhujtg2sdDln5Mj+fn5i1l23U7qXkzwj7aiVSAZ2tLIVmZgoLYcyi"
                        + "bP4Gjwen1vV8GmYd0XHONx6fmuuPEObl5mXKz8Eovxw9TYYMcUeZQ8gRnp+t0jfz"
                        + "5Q7ZoQVm5Nkbkz8gZpTLoOL6S8aUI0C93GzZZwYkWwrFzpsssAJk/6oz1ugUiFI2"
                        + "TZF/XgwdfQCOFjSF1NX2ED9sLsiBBvjYaavk/NO9vNH6eDTZH5n1UO3/fA+bTRUj"
                        + "UYRN0GdkHQCliefZ0Y6XEususCiTApLYfdjUHsIWGldf8C2vxRv+mw=="));
    Collection<Attribute> attributes = List.of(certificatAttribute);
    User mappedUser = userLdapMapper.mapFromAttributes(attributes);

    X509Certificate certificate =
        (X509Certificate)
            CertificateFactory.getInstance("X509")
                .generateCertificate(new ByteArrayInputStream(mappedUser.getCertificate()));

    assertThat(
        "Certificate should have a john doe subject",
        "CN=John Doe,OU=Unit Test,O=Insee,ST=Paris,C=FR",
        is(certificate.getSubjectX500Principal().getName()));

    Map<?, ?> certMetadatas = (Map<?, ?>) mappedUser.getMetadatas().get("cert");
    assertThat(
        "Issuer should be john doe",
        certMetadatas.get("subject"),
        is("CN=John Doe,OU=Unit Test,O=Insee,ST=Paris,C=FR"));
    assertThat(
        "Should see in metadatas that is selfsigned",
        certMetadatas.get("subject"),
        is(certMetadatas.get("issuer")));
    assertThat(
        "Expiration date should be Nov 19 2022",
        certMetadatas.get("expiration"),
        is(Date.from(Instant.parse("2022-11-19T17:45:12.00Z")).toString()));
  }
}
