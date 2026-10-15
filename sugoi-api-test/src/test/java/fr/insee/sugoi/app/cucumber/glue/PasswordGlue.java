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
package fr.insee.sugoi.app.cucumber.glue;

import fr.insee.sugoi.app.cucumber.utils.StepData;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

@SuppressWarnings("unused")
public class PasswordGlue {

  @Before
  public void before(Scenario scenario) {}

  public PasswordGlue(StepData stepData) {}
}
