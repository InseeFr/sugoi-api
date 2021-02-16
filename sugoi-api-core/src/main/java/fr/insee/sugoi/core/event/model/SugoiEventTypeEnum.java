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
package fr.insee.sugoi.core.event.model;

public enum SugoiEventTypeEnum {
  CREATE_USER,
  DELETE_USER,
  UPDATE_USER,
  FIND_USERS,
  FIND_USER_BY_ID,
  FIND_REALMS,
  FIND_REALM_BY_ID,
  CREATE_REALM,
  DELETE_REALM,
  UPDATE_REALM,
  CREATE_ORGANIZATION,
  DELETE_ORGANIZATION,
  UPDATE_ORGANIZATION,
  FIND_ORGANIZATIONS,
  FIND_ORGANIZATION_BY_ID,
  CREATE_HABILITATION,
  DELETE_HABILITATION,
  UPDATE_HABILITATION,
  FIND_HABILITATIONS,
  FIND_HABILITATION_BY_ID,
  CREATE_GROUP,
  DELETE_GROUP,
  UPDATE_GROUP,
  FIND_GROUPS,
  FIND_GROUP_BY_ID,
  CREATE_APPLICATION,
  DELETE_APPLICATION,
  UPDATE_APPLICATION,
  FIND_APPLICATIONS,
  FIND_APPLICATION_BY_ID,
  CHANGE_PASSWORD,
  INIT_PASSWORD,
  RESET_PASSWORD,
  VALIDATE_CREDENTIAL,
  ADD_USER_TO_GROUP,
  DELETE_USER_FROM_GROUP
}
