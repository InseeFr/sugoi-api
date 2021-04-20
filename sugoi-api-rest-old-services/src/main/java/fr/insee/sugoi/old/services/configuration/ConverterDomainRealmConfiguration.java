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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConverterDomainRealmConfiguration {

  @Value("${fr.insee.sugoi.old.domain.realm_userStorage.association:}")
  private List<String> listMapDomainRealm;

  @Bean
  public ConverterDomainRealm converterDomainRealm() {
    return new ConverterDomainRealm(listMapDomainRealm);
  }

  public class ConverterDomainRealm {

    private Map<String, RealmStorage> domainMapRealm = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public ConverterDomainRealm(List<String> listMapDomainRealm) {
      listMapDomainRealm.stream()
          .forEach(
              el -> {
                String[] res = el.split(":");
                domainMapRealm.put(res[0], new RealmStorage(res[1]));
              });
    }

    public RealmStorage getRealmForDomain(String domain) {
      return domainMapRealm.get(domain);
    }

    public class RealmStorage {
      private String realm;
      private String userStorage;

      public RealmStorage(String realmStorage) {
        String[] res = realmStorage.split("_");
        realm = res[0];
        userStorage = res[1];
      }

      public String getRealm() {
        return realm;
      }

      public String getUserStorage() {
        return userStorage;
      }
    }
  }
}
