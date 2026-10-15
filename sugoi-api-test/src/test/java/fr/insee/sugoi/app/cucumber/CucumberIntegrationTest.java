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

import org.junit.platform.suite.api.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("scenario/")
@ConfigurationParameters({
    @ConfigurationParameter(key = "cucumber.glue", value = "fr.insee.sugoi.app.cucumber.glue"),
    @ConfigurationParameter(key = "cucumber.plugin", value = "pretty"),
    @ConfigurationParameter(key = "cucumber.plugin", value = "json:target/cucumber/cucumber.json"),
    @ConfigurationParameter(key = "cucumber.plugin", value = "junit:target/cucumber/junit.xml")
})
public class CucumberIntegrationTest {
}
