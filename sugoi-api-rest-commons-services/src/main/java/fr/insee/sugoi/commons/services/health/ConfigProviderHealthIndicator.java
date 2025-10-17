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
package fr.insee.sugoi.commons.services.health;

import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.model.Realm;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.stereotype.Component;

@Component
public class ConfigProviderHealthIndicator extends AbstractHealthIndicator {

  @Autowired private ConfigService sugoiConfig;

  @Override
  protected void doHealthCheck(Builder builder) throws Exception {
    List<Realm> realms = sugoiConfig.getRealms();
    System.out.println("test");
    builder.up().withDetail("Realms", realms.size() + " realms");
  }
}
