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
package fr.insee.sugoi.core.event.configuration;

import fr.insee.sugoi.core.configuration.GlobalKeysConfig;

public class EventKeysConfig extends GlobalKeysConfig {
  public static final String REALM_NAME = "realmName";
  public static final String ORGANIZATION = "organization";
  public static final String ORGANIZATION_ID = "organizationId";
  public static final String ORGANIZATION_FILTER = "organizationFilter";

  public static final String APPLICATION = "application";
  public static final String APPLICATION_ID = "applicationId";
  public static final String APPLICATION_NAME = "appName";
  public static final String APPLICATION_FILTER = "applicationFilter";

  public static final String GROUP = "group";
  public static final String GROUP_ID = "groupId";
  public static final String GROUP_NAME = "groupName";
  public static final String GROUP_FILTER = "groupFilter";

  public static final String HABILITATION = "habilitation";
  public static final String HABILITATION_ID = "habilitationId";
  public static final String HABILITATION_NAME = "habilitationName";
  public static final String HABILITATION_FILTER = "habilitationFilter";

  public static final String USER = "user";
  public static final String USER_ID = "userId";
  public static final String USER_NAME = "userName";
  public static final String USER_FILTER = "userFilter";
  public static final String USER_PROPERTIES = "userProperties";

  public static final String MAIL = "mail";
  public static final String ADDRESS = "address";
  public static final String PROPERTIES = "properties";

  public static final String PAGEABLE = "pageable";
  public static final String PAGEABLE_RESULT = "pageableResult";
  public static final String TYPE_RECHERCHE = "typeRecherche";
  public static final String SENDMODES = "sendModes";

  public static final String MUST_CHANGE_PASSWORD = "mustChangePassword";
  public static final String PASSWORD_CHANGE_REQUEST = "pcr";
  public static final String PASSWORD = "password";

  public static final String ERROR = "error";

  public static final String ATTRIBUTE_KEY = "attribute-key";
  public static final String ATTRIBUTE_VALUE = "attribute-value";

  public static final String TYPE = "type";
}
