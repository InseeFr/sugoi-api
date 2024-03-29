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
package fr.insee.sugoi.core.configuration;

import fr.insee.sugoi.model.technics.UiField;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@ConfigurationProperties(prefix = "fr.insee.sugoi.ui-mapping")
public class UiMappingService {

  private Map<String, String> userFields = new HashMap<>();

  private Map<String, String> organizationFields = new HashMap<>();

  public Map<String, String> getOrganizationFields() {
    return organizationFields;
  }

  public Map<String, String> getUserFields() {
    return userFields;
  }

  public void setOrganizationFields(String key, String value) {
    organizationFields.put(key, value);
  }

  public void setUserFields(String key, String value) {
    userFields.put(key, value);
  }

  public List<UiField> getUserUiDefaultField() {
    return createListFromDefaultValues(userFields);
  }

  public List<UiField> getOrganizationUiDefaultField() {
    return createListFromDefaultValues(organizationFields);
  }

  private List<UiField> createListFromDefaultValues(Map<String, String> defaultValues) {
    List<UiField> userUiFields = new ArrayList<>();
    for (String entry : defaultValues.values()) {
      try {
        entry = new String(entry.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        userUiFields.add(new UiField(entry));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return userUiFields;
  }
}
