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
package fr.insee.sugoi.core.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UiField implements Serializable, Comparable {
  // userUiMapping$name;helpTextTitle;helpText;path;type;modifiable;tag;order;options1:value,options2:value;

  private String name;
  private String HelpTextTitle;
  private String helpText;
  private String path;
  private String type;
  private Boolean modifiable;
  private String tag;
  private int order;

  private Map<String, Object> options = new HashMap<>();

  public UiField() {}

  @Override
  public int compareTo(Object o) {
    if (o instanceof UiField) {
      UiField oUiField = (UiField) o;
      return Integer.compare(this.getOrder(), oUiField.getOrder());
    } else {
      return 0;
    }
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHelpTextTitle() {
    return this.HelpTextTitle;
  }

  public void setHelpTextTitle(String HelpTextTitle) {
    this.HelpTextTitle = HelpTextTitle;
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

  public Boolean getModifiable() {
    return this.modifiable;
  }

  public void setModifiable(Boolean modifiable) {
    this.modifiable = modifiable;
  }

  public String getTag() {
    return this.tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public Map<String, Object> getOptions() {
    return options;
  }

  public void setOptions(Map<String, Object> options) {
    this.options = options;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }
}
