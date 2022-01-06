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
package fr.insee.sugoi.ldap.fixtures;

import fr.insee.sugoi.model.technics.ModelType;
import fr.insee.sugoi.model.technics.StoreMapping;

import java.util.ArrayList;
import java.util.List;

public class StoreMappingFixture {

  public static List<StoreMapping> getUserStoreMappings() {
    List<StoreMapping> mappings = new ArrayList<>();
    mappings.add(new StoreMapping("username", "uid", ModelType.STRING, true));
    mappings.add(new StoreMapping("lastName", "sn", ModelType.STRING, true));
    mappings.add(new StoreMapping("mail", "mail", ModelType.STRING, true));
    mappings.add(new StoreMapping("firstName", "givenname", ModelType.STRING, true));
    mappings.add(new StoreMapping("attributes.common_name", "cn", ModelType.STRING, true));
    mappings.add(
        new StoreMapping("attributes.personal_title", "personalTitle", ModelType.STRING, true));
    mappings.add(new StoreMapping("attributes.description", "description", ModelType.STRING, true));
    mappings.add(
        new StoreMapping("attributes.phone_number", "telephoneNumber", ModelType.STRING, true));
    mappings.add(
        new StoreMapping("habilitations", "inseeGroupeDefaut", ModelType.LIST_HABILITATION, true));
    mappings.add(
        new StoreMapping("organization", "inseeOrganisationDN", ModelType.ORGANIZATION, true));

    mappings.add(new StoreMapping("address", "inseeAdressePostaleDN", ModelType.ADDRESS, true));
    mappings.add(new StoreMapping("groups", "memberOf", ModelType.LIST_GROUP, false));
    mappings.add(
        new StoreMapping(
            "attributes.insee_roles_applicatifs",
            "inseeRoleApplicatif",
            ModelType.LIST_STRING,
            true));
    mappings.add(
        new StoreMapping("attributes.hasPassword", "userPassword", ModelType.EXISTS, false));
    mappings.add(
        new StoreMapping("metadatas.modifyTimestamp", "modifyTimestamp", ModelType.STRING, false));
    mappings.add(
        new StoreMapping(
            "attributes.additionalMail", "inseeMailCorrespondant", ModelType.STRING, true));
    mappings.add(
        new StoreMapping("attributes.passwordShouldBeReset", "pwdReset", ModelType.STRING, false));

    return mappings;
  }

  public static List<StoreMapping> getGroupStoreMappings() {
    List<StoreMapping> mappings = new ArrayList<>();
    mappings.add(new StoreMapping("name", "cn", ModelType.STRING, true));
    mappings.add(new StoreMapping("description", "description", ModelType.STRING, true));
    mappings.add(new StoreMapping("users", "uniquemember", ModelType.LIST_USER, true));

    return mappings;
  }

  public static List<StoreMapping> getApplicationStoreMappings() {
    return List.of(new StoreMapping("name", "ou", ModelType.STRING, true));
  }

  public static List<StoreMapping> getOrganizationStoreMappings() {
    List<StoreMapping> mappings = new ArrayList<>();
    mappings.add(new StoreMapping("identifiant", "uid", ModelType.STRING, true));
    mappings.add(new StoreMapping("attributes.description", "description", ModelType.STRING, true));
    mappings.add(new StoreMapping("attributes.mail", "mail", ModelType.STRING, true));
    mappings.add(new StoreMapping("address", "inseeAdressePostaleDN", ModelType.ADDRESS, true));
    mappings.add(
        new StoreMapping("organization", "inseeOrganisationDN", ModelType.ORGANIZATION, true));

    return mappings;
  }
}
