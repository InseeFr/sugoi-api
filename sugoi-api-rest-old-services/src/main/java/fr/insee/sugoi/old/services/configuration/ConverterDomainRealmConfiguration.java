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
package fr.insee.sugoi.old.services.configuration;

import fr.insee.sugoi.old.services.model.ConverterDomainRealm;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConverterDomainRealmConfiguration {

  @Value("${fr.insee.sugoi.api.old.domain.realm_userStorage.association:}")
  private List<String> listMapDomainRealm;

  @Bean
  public ConverterDomainRealm converterDomainRealm() {
    return new ConverterDomainRealm(listMapDomainRealm);
  }
}
