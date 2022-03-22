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

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.model.*;
import fr.insee.sugoi.model.fixtures.StoreMappingFixture;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = UserLdapMapper.class)
public class UserLdapMapperFromObjectTest {

  UserLdapMapper userLdapMapper;
  List<Attribute> mappedAttributes;
  User user;
  String[] objectClassesToCompare = {"top", "person"};

  @BeforeEach
  public void setup() {

    Map<RealmConfigKeys, String> config = new HashMap<>();
    config.put(GlobalKeysConfig.ORGANIZATION_SOURCE, "ou=organisations,o=insee,c=fr");
    config.put(GlobalKeysConfig.ADDRESS_SOURCE, "ou=address,o=insee,c=fr");
    config.put(LdapConfigKeys.USER_OBJECT_CLASSES, "top,person");

    userLdapMapper = new UserLdapMapper(config, StoreMappingFixture.getUserStoreMappings());

    user = new User();
  }

  @Test
  public void getSimpleUserAttributesFromJavaObject() {

    user.setUsername("Toto");
    user.setFirstName("Toto");
    user.setLastName("Tata");
    user.setMail("toto@insee.fr");
    List<Attribute> mappedAttributes = userLdapMapper.mapToAttributesForCreation(user);
    assertThat(
        "Should have right objectClasses",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("objectClass")
                        && Arrays.equals(attribute.getValues(), objectClassesToCompare)));
    assertThat(
        "Should have username",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("uid") && attribute.getValue().equals("Toto")));
    assertThat(
        "Should have last name",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("sn") && attribute.getValue().equals("Tata")));
    assertThat(
        "Should have first name",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equalsIgnoreCase("givenName")
                        && attribute.getValue().equals("Toto")));
    assertThat(
        "Should have mail",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("mail")
                        && attribute.getValue().equals("toto@insee.fr")));
  }

  @Test
  public void getUserAttributesAttributesFromJavaObject() {

    user.addAttributes("common_name", "TotoCommun");
    user.addAttributes("phone_number", "012345678");
    user.addAttributes("description", "Ceci est un user");
    user.addAttributes("personal_title", "Camarade");
    List<Attribute> mappedAttributes = userLdapMapper.mapToAttributesForCreation(user);

    assertThat(
        "Should have cn",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("cn") && attribute.getValue().equals("TotoCommun")));

    assertThat(
        "Should have phone_number",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("telephoneNumber")
                        && attribute.getValue().equals("012345678")));

    assertThat(
        "Should have description",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("description")
                        && attribute.getValue().equals("Ceci est un user")));

    assertThat(
        "Should have personal title",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("personalTitle")
                        && attribute.getValue().equals("Camarade")));
  }

  @Test
  public void getUserAddressAttributesFromJavaObject() {

    PostalAddress postalAddress = new PostalAddress();
    String[] adresses = {"33 rue des Fleurs", "56700 Fleurville", null, null, null, null, null};
    postalAddress.setLines(adresses);
    postalAddress.setId("generatedBefore");
    user.setAddress(postalAddress);
    List<Attribute> mappedAttributes = userLdapMapper.mapToAttributesForCreation(user);

    assertThat(
        "Should have address attribute",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("inseeAdressePostaleDN")
                        && attribute
                            .getValue()
                            .equals("l=generatedBefore,ou=address,o=insee,c=fr")));
  }

  @Test
  public void getUserHabilitationsAttributeFromJavaObject() {

    Habilitation habilitation1 = new Habilitation("property_role_application");
    Habilitation habilitation2 = new Habilitation("property_role_application2");
    List<Habilitation> habilitations = new ArrayList<>();
    habilitations.add(habilitation1);
    habilitations.add(habilitation2);
    user.setHabilitations(habilitations);
    List<Attribute> mappedAttributes = userLdapMapper.mapToAttributesForCreation(user);

    assertThat(
        "Should have first habilitation in inseeGroupeDefault",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("inseeGroupeDefaut")
                        && Arrays.asList(attribute.getValues())
                            .contains("property_role_application")));

    assertThat(
        "Should have second habilitation in inseeGroupeDefault",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("inseeGroupeDefaut")
                        && Arrays.asList(attribute.getValues())
                            .contains("property_role_application2")));
  }

  @Test
  public void getUserOrganizationAttributeFromJavaObject() {

    Organization organization = new Organization();
    organization.setIdentifiant("SuperOrga");
    user.setOrganization(organization);
    List<Attribute> mappedAttributes = userLdapMapper.mapToAttributesForCreation(user);

    assertThat(
        "Should have SuperOrga link",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("inseeOrganisationDN")
                        && attribute
                            .getValue()
                            .equals("uid=SuperOrga,ou=organisations,o=insee,c=fr")));
  }

  @Test
  public void dontGetUserModifyTimestampFromJavaObject() {

    user.addMetadatas("modifyTimestamp", "toto");
    List<Attribute> mappedAttributes = userLdapMapper.mapToAttributesForCreation(user);

    assertThat(
        "Shouldn't have a modifyTimestamp",
        mappedAttributes.stream()
            .allMatch(attribute -> !attribute.getName().equals("modifyTimestamp")));
  }

  @Test
  public void getUserInseeRoleApplicatifFromJavaObject() {

    List<String> inseeRoleApplicatif = new ArrayList<String>();
    inseeRoleApplicatif.add("toto");
    inseeRoleApplicatif.add("tata");
    user.addAttributes("insee_roles_applicatifs", inseeRoleApplicatif);
    List<Attribute> mappedAttributes = userLdapMapper.mapToAttributesForCreation(user);

    assertThat(
        "Should have attribute inseeRoleApplicatif toto",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("inseeRoleApplicatif")
                        && Arrays.asList(attribute.getValues()).contains("toto")));
    assertThat(
        "Should have attribute inseeRoleApplicatif tata",
        mappedAttributes.stream()
            .anyMatch(
                attribute ->
                    attribute.getName().equals("inseeRoleApplicatif")
                        && Arrays.asList(attribute.getValues()).contains("tata")));
  }

  @Test
  public void removeInseeRoleApplicatifIfEmptyListSet() {

    List<String> inseeRoleApplicatif = new ArrayList<String>();
    user.addAttributes("insee_roles_applicatifs", inseeRoleApplicatif);
    List<Modification> modifications = userLdapMapper.createMods(user);

    assertThat(
        "Should remove attribute inseeRoleApplicatif",
        modifications.stream()
            .anyMatch(
                mod ->
                    mod.getModificationType().equals(ModificationType.REPLACE)
                        && mod.getAttributeName().equals("inseeRoleApplicatif")
                        && mod.getRawValues().length == 0));
  }

  @Test
  public void dontRemoveInseeRoleApplicatifIfNoListSet() {

    // when no explicit insee_roles_applicatifs attribute is set
    List<Modification> modifications = userLdapMapper.createMods(user);

    assertThat(
        "Should not change attribute inseeRoleApplicatif",
        modifications.stream()
            .noneMatch(mod -> mod.getAttributeName().equals("inseeRoleApplicatif")));
  }

  @Test
  public void removeAllHabilitationsIfEmptyList() {

    // Default is a null habilitation list and means no changes on habilitation
    // An explicit empty habilitation list means remove all habilitations
    user.setHabilitations(new ArrayList<Habilitation>());
    List<Modification> modifications = userLdapMapper.createMods(user);

    assertThat(
        "Should not change attribute inseeGroupeDefaut",
        modifications.stream()
            .anyMatch(
                mod ->
                    mod.getAttributeName().equals("inseeGroupeDefaut")
                        && mod.getValues().length == 0));
  }
}
