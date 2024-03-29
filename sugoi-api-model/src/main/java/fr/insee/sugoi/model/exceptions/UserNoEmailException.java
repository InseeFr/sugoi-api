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

public class UserNoEmailException extends BadRequestException {

  /** */
  private static final long serialVersionUID = 1L;

  public UserNoEmailException(String message) {
    super(message);
  }

  public UserNoEmailException(String message, Throwable cause) {
    super(message, cause);
  }

  public UserNoEmailException(String realm, String userstorage, String userid) {
    super(
        String.format(
            "User %s in realm %s and userstorage %s have no email", userid, realm, userstorage));
  }
}
