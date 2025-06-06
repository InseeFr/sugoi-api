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
package fr.insee.sugoi.core.realm;

import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.exceptions.RealmNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

/**
 * This is called to fetch realms configuration.
 *
 * <p>All implementations need to declare a value for the property `fr.insee.sugoi.config.type` like
 * this :
 *
 * <pre>
 * &#64;ConditionalOnProperty(
 *  value = "fr.insee.sugoi.realm.config.type",
 *  havingValue = "<value>",
 *  matchIfMissing = false)
 * </pre>
 */
public interface RealmProvider {

  /**
   * Load the realm. Even if the returned object is cached, Implementations should take care of not
   * solliciting too much resources as this is called often.
   *
   * @param realmName, the realm name to return (case insensitive)
   * @return the realm found
   */
  @Cacheable(value = "Realm", key = "#realmName")
  public Optional<Realm> load(String realmName);

  /**
   * Load all realms. Even if the returned List of object is cached, Implementations should take
   * care of not solliciting too much resources as this is called often.
   *
   * @return List<Realm>, the realm list
   */
  @Cacheable(value = "Realms", key = "#root.methodName")
  public List<Realm> findAll();

  @CacheEvict(
      value = {"Realms", "Realm"},
      allEntries = true)
  public ProviderResponse createRealm(Realm realm, ProviderRequest providerRequest);

  /**
   * Update the properties of a realm
   *
   * @param realm that will override the realm with same name
   * @param providerRequest
   * @throws RealmNotFoundException if realm does not exist
   * @return a response containing the updated realm and the status of the action
   */
  @CacheEvict(
      value = {"Realms", "Realm"},
      allEntries = true)
  public ProviderResponse updateRealm(Realm realm, ProviderRequest providerRequest);

  @CacheEvict(
      value = {"Realms", "Realm"},
      allEntries = true)
  public ProviderResponse deleteRealm(String realmName, ProviderRequest providerRequest);
}
