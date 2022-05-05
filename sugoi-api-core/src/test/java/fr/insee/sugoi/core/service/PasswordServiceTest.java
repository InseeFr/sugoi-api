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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.insee.sugoi.core.service.impl.PasswordServiceImpl;
import fr.insee.sugoi.model.exceptions.PasswordPolicyNotMetException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = PasswordServiceImpl.class)
@TestPropertySource(locations = "classpath:/application.properties")
public class PasswordServiceTest {

  @Autowired private PasswordService passwordService;

  @Test
  public void testValidatePassword() {
    assertThrows(
        PasswordPolicyNotMetException.class,
        () -> passwordService.validatePassword("toto", true, true, true, true, 10));
    assertThrows(
        PasswordPolicyNotMetException.class,
        () -> passwordService.validatePassword("toto", true, false, false, false, 4));
    assertThat(
        "Should be valid",
        passwordService.validatePassword("e1―gAN7‡/a*", true, true, true, true, 10),
        is(true));
  }

  @Test
  public void testGeneratePassword() {
    assertThat(
        "Should be valid",
        passwordService.validatePassword(
            passwordService.generatePassword(true, true, true, true, 25),
            true,
            true,
            true,
            true,
            10),
        is(true));
  }
}
