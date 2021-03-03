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
package fr.insee.sugoi.core.service.impl;

import fr.insee.sugoi.core.service.PasswordService;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordGenerator;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PasswordServiceImpl implements PasswordService {

  @Value("${fr.insee.sugoi.password.length:5}")
  private String passwordLength;

  @Value("${fr.insee.sugoi.password.allowed.regexp:}")
  private String allowedRegexp;

  @Override
  public String generatePassword() {
    return generatePassword(Integer.valueOf(passwordLength));
  }

  @Override
  public boolean validatePassword(String password) {
    if (password != null) {
      PasswordValidator passwordValidator =
          new PasswordValidator(new LengthRule(Integer.valueOf(passwordLength), Integer.MAX_VALUE));
      PasswordData passwordData = new PasswordData(password);
      RuleResult validate = passwordValidator.validate(passwordData);
      return validate.isValid();
    } else {
      return false;
    }
  }

  @Override
  public String generatePassword(int length) {
    CharacterRule letter = new CharacterRule(EnglishCharacterData.Alphabetical);
    PasswordGenerator passwordGenerator = new PasswordGenerator();
    String password = passwordGenerator.generatePassword(length, letter);
    return password;
  }
}
