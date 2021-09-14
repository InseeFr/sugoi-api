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
package fr.insee.sugoi.core.model;

import org.passay.CharacterData;

/** Special set of character data for password generation. */
public enum SugoiRandomPasswordCharacterData implements CharacterData {

  /** Lower case characters without i, l and o. */
  LowerCase("INSUFFICIENT_LOWERCASE", "abcdefghjkmnpqrstuvwxyz"),

  /** Upper case characters without I, O and Q. */
  UpperCase("INSUFFICIENT_UPPERCASE", "ABCDEFGHJKLMNPRSTUVWXYZ"),

  /** Digit characters without 1 and 0. */
  Digit("INSUFFICIENT_DIGIT", "23456789"),

  /** Alphabetical characters (upper and lower case). */
  Alphabetical("INSUFFICIENT_ALPHABETICAL", UpperCase.getCharacters() + LowerCase.getCharacters()),

  /** Special characters. */
  Special(
      "INSUFFICIENT_SPECIAL",
      // ASCII symbols
      "!$%&()*+?@");

  /** Error code. */
  private final String errorCode;

  /** Characters. */
  private final String characters;

  /**
   * Creates a new english character data.
   *
   * @param code Error code.
   * @param charString Characters as string.
   */
  SugoiRandomPasswordCharacterData(final String code, final String charString) {
    errorCode = code;
    characters = charString;
  }

  @Override
  public String getErrorCode() {
    return errorCode;
  }

  @Override
  public String getCharacters() {
    return characters;
  }
}
