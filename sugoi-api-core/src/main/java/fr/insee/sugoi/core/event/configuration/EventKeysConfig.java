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

public class EventKeysConfig {

  private EventKeysConfig() {}

  public static final String REALM = "realm";
  public static final String USERSTORAGE = "userStorage";

  public static final String USER = "user";
  public static final String USER_ID = "userId";

  public static final String MAILS = "mails";
  public static final String PROPERTIES = "properties";

  public static final String PWD = "password";

  public static final String NEW_PWD = "new-password";
  public static final String OLD_PWD = "old-password";
  public static final String WEBSERVICE_TAG = "webserviceTag";
}
