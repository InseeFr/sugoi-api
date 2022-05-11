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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import fr.insee.sugoi.model.technics.UiField;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class UiMappingServiceTest {

  @Test
  public void createStringFromUiMappingNoOptions() {
    UiField uiFieldWithoutOptions = new UiField();
    uiFieldWithoutOptions.setName("name");
    uiFieldWithoutOptions.setHelpTextTitle("help text title");
    uiFieldWithoutOptions.setHelpText("help text");
    uiFieldWithoutOptions.setPath("toto.titi");
    uiFieldWithoutOptions.setType("string");
    uiFieldWithoutOptions.setModifiable(true);
    uiFieldWithoutOptions.setTag("main");
    uiFieldWithoutOptions.setOrder(3);
    assertThat(
        "Should have all parameters in a string",
        uiFieldWithoutOptions.toString(),
        is("name;help text title;help text;toto.titi;string;true;main;3"));
  }

  @Test
  public void createStringFromUiMappingWithMissingProps() {
    UiField uiFieldWithMissingProps = new UiField();
    uiFieldWithMissingProps.setName("name");
    uiFieldWithMissingProps.setHelpTextTitle("help text title");
    uiFieldWithMissingProps.setPath("toto.titi");
    uiFieldWithMissingProps.setType("string");
    uiFieldWithMissingProps.setTag("main");
    assertThat(
        "Order should be max value, null string should be nothing and boolean should be false",
        uiFieldWithMissingProps.toString(),
        is("name;help text title;;toto.titi;string;false;main;2147483647"));
  }

  @Test
  public void createStringFromUiMappingWithOptions() {
    UiField uiFieldWithOptions = new UiField();
    uiFieldWithOptions.setName("name");
    uiFieldWithOptions.setHelpTextTitle("help text title");
    uiFieldWithOptions.setHelpText("help text");
    uiFieldWithOptions.setPath("toto.titi");
    uiFieldWithOptions.setType("string");
    uiFieldWithOptions.setModifiable(true);
    uiFieldWithOptions.setTag("main");
    uiFieldWithOptions.setOrder(3);
    uiFieldWithOptions.setOptions(Map.of("option1", "value1", "option2", "value2"));
    assertThat(
        "Should also have options",
        uiFieldWithOptions.toString(),
        anyOf(
            is(
                "name;help text title;help text;toto.titi;string;true;main;3;option1=value1;option2=value2"),
            is(
                "name;help text title;help text;toto.titi;string;true;main;3;option2=value2;option1=value1")));
  }

  @Test
  public void createStringFromUiMappingWithRequiredAndOptions() {
    UiField uiFieldWithRequired = new UiField();
    uiFieldWithRequired.setName("name");
    uiFieldWithRequired.setHelpTextTitle("help text title");
    uiFieldWithRequired.setHelpText("help text");
    uiFieldWithRequired.setPath("toto.titi");
    uiFieldWithRequired.setType("string");
    uiFieldWithRequired.setRequired(true);
    uiFieldWithRequired.setTag("main");
    uiFieldWithRequired.setModifiable(true);
    uiFieldWithRequired.setOrder(3);
    uiFieldWithRequired.setOptions(Map.of("option1", "value1", "option2", "value2"));
    assertThat(
        "Should also have options",
        uiFieldWithRequired.toString(),
        anyOf(
            is(
                "name;help text title;help text;toto.titi;string;true;main;3;required;option1=value1;option2=value2"),
            is(
                "name;help text title;help text;toto.titi;string;true;main;3;required;option2=value2;option1=value1")));
  }
}
