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
package fr.insee.sugoi.event.listener.metrics;

import fr.insee.sugoi.core.event.model.SugoiEvent;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "fr.insee.sugoi.api.event.metrics.enabled",
    havingValue = "true",
    matchIfMissing = false)
public class SugoiEventMetrics {

  @Autowired private MeterRegistry meterRegistry;

  @EventListener
  public void handleContextStart(SugoiEvent cse) {
    switch (cse.getEventType()) {
      case CREATE_REALM:

      case UPDATE_REALM:

      case DELETE_REALM:

      case FIND_REALMS:

      case FIND_REALM_BY_ID:

      case FIND_USER_BY_ID:
      case FIND_APPLICATION_BY_ID:
      case FIND_GROUP_BY_ID:
      case FIND_ORGANIZATION_BY_ID:
      case FIND_GROUPS:
      case FIND_APPLICATIONS:
      case FIND_HABILITATION_BY_ID:
      case FIND_USERS:
      case FIND_HABILITATIONS:
      case FIND_ORGANIZATIONS:
      case FIND_REALM_BY_ID_ERROR:
        // We don't increment those counters
        break;
      default:
        if (cse.getUserStorage() != null) {
          meterRegistry
              .counter(
                  cse.getEventType().name(),
                  "realm",
                  cse.getRealm(),
                  "userStorage",
                  cse.getUserStorage())
              .increment();
        } else {
          if (cse.getRealm() != null) {
            meterRegistry.counter(cse.getEventType().name(), "realm", cse.getRealm()).increment();
          } else {
            meterRegistry.counter(cse.getEventType().name()).increment();
          }
        }
        break;
    }
  }
}
