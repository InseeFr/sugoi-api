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
package fr.insee.sugoi.app.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/** CucumberIntegrationTest */
@RunWith(Cucumber.class)
@CucumberOptions(
    features = {"src/test/resources/scenario/"},
    glue = {"fr.insee.sugoi.app.cucumber.glue"},
    dryRun = false,
    strict = true,
    plugin = {
      "pretty",
      "json:target/cucumber/cucumber.json",
      "usage:target/cucumber/usage.jsonx",
      "junit:target/cucumber/junit.xml"
    })
public class CucumberIntegrationTest {}
