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
package fr.insee.sugoi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;

public class PostalAddress {

  @JsonIgnore private String id;

  @JsonProperty("address")
  private String[] lines = new String[7];

  public PostalAddress() {}

  public String[] getLines() {
    return lines;
  }

  public PostalAddress(String id) {
    this.id = id;
  }

  @JsonIgnore
  public boolean isNotEmpty() {
    return Arrays.stream(lines).anyMatch(s -> s != null && !s.isBlank());
  }

  public void setLines(String[] lines) {
    this.lines = lines;
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    for (String line : lines) {
      if (line != null && !line.isEmpty()) s.append(line).append(" ");
    }
    return s.toString();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
