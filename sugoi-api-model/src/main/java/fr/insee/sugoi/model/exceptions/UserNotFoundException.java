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
package fr.insee.sugoi.model.exceptions;

public class UserNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 5107823250419750984L;

  public UserNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public UserNotFoundException(String realm, String userid) {
    super(String.format("User %s does not exist in realm %s", userid, realm));
  }

  public UserNotFoundException(String realm, String userstorage, String userid) {
    super(
        String.format(
            "User %s does not exist in realm %s and userstorage %s", userid, realm, userstorage));
  }
}
