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

  /**
   * Convert a string of form :
   *
   * <p>name;helpTextTitle;helpText;path;type;modifiable;tag[;order;option:value...]
   *
   * <p>to the adequate UiField
   *
   * <p>with :
   *
   * <p>- name : a short name for the field
   *
   * <p>- helpTextTitle : the human readable name of the field
   *
   * <p>- helpText : an extensive explanation of the field
   *
   * <p>- path : an informative value indicating where the field comes from
   *
   * <p>- type : string, list_string...
   *
   * <p>- modifiable : true or false depending of the field modification status
   *
   * <p>- tag : a tag defining a category for the field
   *
   * <p>- order : an int to order the different fields
   *
   * <p>The next parameters constitute a map of option/value
   *
   * @param entry a string with appropriate format
   * @return the converted UiField, null if format is invalid
   */
  public UiField convertStringToUIField(String entry) {
    try {
      String[] fieldProperties = entry.split(";", 9);
      UiField uifield = new UiField();
      uifield.setName(fieldProperties[0]);
      uifield.setHelpTextTitle(fieldProperties[1]);
      uifield.setHelpText(fieldProperties[2]);
      uifield.setPath(fieldProperties[3]);
      uifield.setType(fieldProperties[4]);
      uifield.setModifiable(Boolean.parseBoolean(fieldProperties[5]));
      uifield.setTag(fieldProperties[6]);
      if (fieldProperties.length >= 8) {
        uifield.setOrder(getIntOrInfty(fieldProperties[7]));
      }
      if (fieldProperties.length >= 9 && !fieldProperties[8].isEmpty()) {
        String[] optionsProperties = fieldProperties[8].split(";");
        Map<String, String> options = new HashMap<>();
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

  private int getIntOrInfty(String intString) {
    try {
      return Integer.valueOf(intString);
    } catch (NumberFormatException e) {
      return Integer.MAX_VALUE;
    }
  }
}
