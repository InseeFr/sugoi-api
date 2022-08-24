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
package fr.insee.sugoi.model.technics;

import fr.insee.sugoi.model.technics.exceptions.EntryIsNotUIFieldException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UiField implements Serializable, Comparable<UiField> {

  private String name = "";
  private String helpTextTitle = "";
  private String helpText = "";
  private String path = "";
  private String type = "";
  private boolean required = false;
  private boolean modifiable = false;
  private String tag = "";
  private int order = Integer.MAX_VALUE;

  private Map<String, String> options = new HashMap<>();

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
   * @throws EntryIsNotUIFieldException
   */
  public UiField(String entry) throws EntryIsNotUIFieldException {
    try {
      String[] fieldProperties = entry.split(";", 9);
      name = fieldProperties[0];
      helpTextTitle = fieldProperties[1];
      helpText = fieldProperties[2];
      path = fieldProperties[3];
      type = fieldProperties[4];
      modifiable = Boolean.parseBoolean(fieldProperties[5]);
      tag = fieldProperties[6];
      if (fieldProperties.length >= 8) {
        order = getIntOrInfty(fieldProperties[7]);
      }
      if (fieldProperties.length > 8 && !fieldProperties[8].isEmpty()) {
        String[] optionsProperties = fieldProperties[8].split(";");
        options = new HashMap<>();
        for (String optionProperty : optionsProperties) {
          if (optionProperty.equals("required")) {
            required = true;
          } else {
            options.put(optionProperty.split("=")[0], optionProperty.split("=")[1]);
          }
        }
      }
    } catch (Exception e) {
      throw new EntryIsNotUIFieldException("The entry " + entry + " cannot be parsed as a UIField");
    }
  }

  public UiField() {}

  public int compareTo(UiField uiField) {
    return Integer.compare(this.getOrder(), uiField.getOrder());
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHelpTextTitle() {
    return this.helpTextTitle;
  }

  public void setHelpTextTitle(String helpTextTitle) {
    this.helpTextTitle = helpTextTitle;
  }

  public String getHelpText() {
    return this.helpText;
  }

  public void setHelpText(String helpText) {
    this.helpText = helpText;
  }

  public String getPath() {
    return this.path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getType() {
    return this.type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean getModifiable() {
    return this.modifiable;
  }

  public void setModifiable(boolean modifiable) {
    this.modifiable = modifiable;
  }

  public String getTag() {
    return this.tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public void setOptions(Map<String, String> options) {
    this.options = options;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public boolean getRequired() {
    return this.required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public String toString() {
    List<String> uiFieldStrings = new ArrayList<>();
    uiFieldStrings.add(this.getName());
    uiFieldStrings.add(this.getHelpTextTitle());
    uiFieldStrings.add(this.getHelpText());
    uiFieldStrings.add(this.getPath());
    uiFieldStrings.add(this.getType());
    uiFieldStrings.add(String.valueOf(this.getModifiable()));
    uiFieldStrings.add(this.getTag());
    uiFieldStrings.add(String.valueOf(this.getOrder()));
    if (this.getRequired()) uiFieldStrings.add("required");
    this.getOptions().forEach((k, v) -> uiFieldStrings.add(k + "=" + v));
    return uiFieldStrings.stream().collect(Collectors.joining(";"));
  }

  private int getIntOrInfty(String intString) {
    try {
      return Integer.valueOf(intString);
    } catch (NumberFormatException e) {
      return Integer.MAX_VALUE;
    }
  }
}
