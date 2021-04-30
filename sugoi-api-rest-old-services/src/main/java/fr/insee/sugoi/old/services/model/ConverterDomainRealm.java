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
package fr.insee.sugoi.old.services.model;

import fr.insee.sugoi.core.exceptions.RealmNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ConverterDomainRealm {

  private Map<String, RealmStorage> domainMapRealm;

  public ConverterDomainRealm(List<String> listMapDomainRealm) {
    this.domainMapRealm = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    listMapDomainRealm.stream()
        .forEach(
            el -> {
              String[] res = el.split(":", 2);
              domainMapRealm.put(res[0], new RealmStorage(res[1]));
            });
  }

  public RealmStorage getRealmForDomain(String domain) {
    if (domainMapRealm.get(domain) != null) {
      return domainMapRealm.get(domain);
    }
    throw new RealmNotFoundException("Cannot map domain " + domain + " to realm and userstorage");
  }

  public Map<String, RealmStorage> getDomainMapRealm() {
    return domainMapRealm;
  }

  public void setDomainMapRealm(Map<String, RealmStorage> domainMapRealm) {
    this.domainMapRealm = domainMapRealm;
  }
}
