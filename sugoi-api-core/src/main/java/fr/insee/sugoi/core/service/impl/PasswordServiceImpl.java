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

import fr.insee.sugoi.core.exceptions.PasswordPolicyNotMetException;
import fr.insee.sugoi.core.service.PasswordService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordGenerator;
import org.passay.PasswordValidator;
import org.passay.Rule;
import org.passay.RuleResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PasswordServiceImpl implements PasswordService {

  @Value("${fr.insee.sugoi.password.create.length:12}")
  private int pCreateSize;

  @Value("${fr.insee.sugoi.password.create.withDigits:true}")
  private Boolean pCreateWithDigits;

  @Value("${fr.insee.sugoi.password.create.withUpperCase:true}")
  private Boolean pCreateWithUpperCase;

  @Value("${fr.insee.sugoi.password.create.withLowerCase:true}")
  private Boolean pCreateWithLowerCase;

  @Value("${fr.insee.sugoi.password.create.withSpecial:true}")
  private Boolean pCreateWithSpecial;

  @Value("${fr.insee.sugoi.password.validate.minimal.length:12}")
  private int pValidateSize;

  @Value("${fr.insee.sugoi.password.validate.withDigits:true}")
  private Boolean pValidateWithDigits;

  @Value("${fr.insee.sugoi.password.validate.withUpperCase:true}")
  private Boolean pValidateWithUpperCase;

  @Value("${fr.insee.sugoi.password.validate.withLowerCase:true}")
  private Boolean pValidateWithLowerCase;

  @Value("${fr.insee.sugoi.password.validate.withSpecial:true}")
  private Boolean pValidateWithSpecial;

  @Override
  public String generatePassword() {
    return generatePassword(
        pCreateWithUpperCase,
        pCreateWithLowerCase,
        pCreateWithDigits,
        pCreateWithSpecial,
        pCreateSize);
  }

  @Override
  public boolean validatePassword(
      String password,
      Boolean withUpperCase,
      Boolean withLowerCase,
      Boolean withDigit,
      Boolean withSpecial,
      Integer size) {
    if (password != null) {
      List<Rule> rules = new ArrayList<>();
      rules.add(new LengthRule(size != null ? size : pValidateSize, Integer.MAX_VALUE));
      generateCharacterRules(
              withUpperCase != null ? withUpperCase : pValidateWithUpperCase,
              withLowerCase != null ? withLowerCase : pValidateWithLowerCase,
              withDigit != null ? withDigit : pValidateWithDigits,
              withSpecial != null ? withSpecial : pValidateWithSpecial)
          .forEach(rule -> rules.add(rule));
      PasswordValidator passwordValidator = new PasswordValidator(rules);
      PasswordData passwordData = new PasswordData(password);
      RuleResult validate = passwordValidator.validate(passwordData);
      if (validate.isValid()) {
        return true;

      } else {
        throw new PasswordPolicyNotMetException(
            validate.getDetails().stream()
                .map(rd -> rd.getErrorCode())
                .collect(Collectors.toList())
                .toString());
      }

    } else {

      return false;
    }
  }

  @Override
  public String generatePassword(
      Boolean withUpperCase,
      Boolean withLowerCase,
      Boolean withDigit,
      Boolean withSpecial,
      Integer size) {

    PasswordGenerator passwordGenerator = new PasswordGenerator();
    String password =
        passwordGenerator.generatePassword(
            size != null ? size : pCreateSize,
            generateCharacterRules(
                withUpperCase != null ? withUpperCase : pCreateWithUpperCase,
                withLowerCase != null ? withLowerCase : pCreateWithLowerCase,
                withDigit != null ? withDigit : pCreateWithDigits,
                withSpecial != null ? withSpecial : pCreateWithSpecial));
    return password;
  }

  public List<CharacterRule> generateCharacterRules(
      Boolean withUpperCase, Boolean withLowerCase, Boolean withDigit, Boolean withSpecial) {
    List<CharacterRule> characterRules = new ArrayList<>();
    if (withUpperCase) characterRules.add(new CharacterRule(EnglishCharacterData.UpperCase, 1));
    if (withLowerCase) characterRules.add(new CharacterRule(EnglishCharacterData.LowerCase, 1));
    if (withDigit) characterRules.add(new CharacterRule(EnglishCharacterData.Digit, 1));
    if (withSpecial) characterRules.add(new CharacterRule(EnglishCharacterData.Special, 1));
    return characterRules;
  }
}
