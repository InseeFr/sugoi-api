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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UiField implements Serializable, Comparable<UiField> {

  private String name;
  private String helpTextTitle;
  private String helpText;
  private String path;
  private String type;
  private Boolean required = false;
  private Boolean modifiable;
  private String tag;
  private int order;

  private Map<String, String> options = new HashMap<>();

  @Override
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

  public Boolean getRequired() {
    return this.required;
  }

  public void setRequired(Boolean required) {
    this.required = required;
  }
}
