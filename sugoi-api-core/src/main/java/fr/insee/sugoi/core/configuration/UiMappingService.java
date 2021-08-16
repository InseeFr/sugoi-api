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

import fr.insee.sugoi.core.model.UiField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@ConfigurationProperties(prefix = "fr.insee.sugoi.ui-mapping")
public class UiMappingService {

  private static final Logger logger = LoggerFactory.getLogger(UiMappingService.class);

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
    List<UiField> userUiFields = new ArrayList<>();
    for (String entry : userFields.values()) {
      try {
        entry = new String(entry.getBytes("ISO-8859-1"), "UTF-8");
        UiField field = convertStringToUIField(entry);
        if (field != null) {
          userUiFields.add(field);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return userUiFields;
  }

  public List<UiField> getOrganizationUiDefaultField() {
    List<UiField> organizationUiFields = new ArrayList<>();
    for (String entry : organizationFields.values()) {
      try {
        entry = new String(entry.getBytes("ISO-8859-1"), "UTF-8");
        UiField field = convertStringToUIField(entry);
        if (field != null) {
          organizationUiFields.add(field);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return organizationUiFields;
  }

  public UiField convertStringToUIField(String entry) {
    try {
      String[] fieldProperties = entry.split(";", 8);
      UiField uifield = new UiField();
      uifield.setName(fieldProperties[0]);
      uifield.setHelpTextTitle(fieldProperties[1]);
      uifield.setHelpText(fieldProperties[2]);
      uifield.setPath(fieldProperties[3]);
      uifield.setType(fieldProperties[4]);
      uifield.setModifiable(Boolean.parseBoolean(fieldProperties[5]));
      uifield.setTag(fieldProperties[6]);
      if (fieldProperties[7].contains(";")) {
        String[] optionsProperties = fieldProperties[7].split(";");
        Map<String, Object> options = new HashMap<>();
        for (String optionProperty : optionsProperties) {
          options.put(optionProperty.split("=")[0], optionProperty.split("=")[1]);
        }
        uifield.setOptions(options);
      }
      return uifield;
    } catch (Exception e) {
      logger.info("Entry " + entry + " not parseable");
      return null;
    }
  }
}
