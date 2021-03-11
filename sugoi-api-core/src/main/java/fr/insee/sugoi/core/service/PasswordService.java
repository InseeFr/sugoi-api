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
package fr.insee.sugoi.core.service;

public interface PasswordService {

  String generatePassword();

  String generatePassword(
      Boolean withUpperCase,
      Boolean withLowerCase,
      Boolean withDigit,
      Boolean withSpecial,
      Integer size);

  boolean validatePassword(
      String password,
      Boolean withUpperCase,
      Boolean withLowerCase,
      Boolean withDigit,
      Boolean withSpecial,
      Integer size);
}
