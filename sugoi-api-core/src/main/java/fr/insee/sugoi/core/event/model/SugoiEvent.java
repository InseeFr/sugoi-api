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
package fr.insee.sugoi.core.event.model;

import java.util.Map;
import org.springframework.context.ApplicationEvent;

public class SugoiEvent extends ApplicationEvent {

  private static final long serialVersionUID = 1L;

  private SugoiEventTypeEnum eventType;

  private String realm;

  private String userStorage;

  private Map<String, Object> properties;

  public SugoiEvent(
      String realm, String storage, SugoiEventTypeEnum eventType, Map<String, Object> properties) {
    super(eventType);
    this.realm = realm;
    this.userStorage = storage;
    this.eventType = eventType;
    this.properties = properties;
  }

  public SugoiEventTypeEnum getEventType() {
    return eventType;
  }

  public String getRealm() {
    return realm;
  }

  public String getUserStorage() {
    return userStorage;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }
}
