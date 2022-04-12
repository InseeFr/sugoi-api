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
import java.util.Arrays;

public class PostalAddress {

  @JsonIgnore private String id;

  public PostalAddress() {}

  public PostalAddress(String id) {
    this.id = id;
  }

  private String line1;
  private String line2;
  private String line3;
  private String line4;
  private String line5;
  private String line6;
  private String line7;

  public String getLine1() {
    return line1;
  }

  public String getLine2() {
    return line2;
  }

  public String getLine3() {
    return line3;
  }

  public String getLine4() {
    return line4;
  }

  public String getLine5() {
    return line5;
  }

  public String getLine6() {
    return line6;
  }

  public String getLine7() {
    return line7;
  }

  @JsonIgnore
  public String[] getLines() {
    return new String[] {line1, line2, line3, line4, line5, line6, line7};
  }

  @JsonIgnore
  public boolean isNotEmpty() {
    return Arrays.stream(this.getLines()).anyMatch(s -> s != null && !s.isBlank());
  }

  public void setLines(String[] lines) {
    if (lines.length != 7) throw new IllegalArgumentException("An address should contain 7 lines");
    this.line1 = lines[0];
    this.line2 = lines[1];
    this.line3 = lines[2];
    this.line4 = lines[3];
    this.line5 = lines[4];
    this.line6 = lines[5];
    this.line7 = lines[6];
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    for (String line : this.getLines()) {
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
