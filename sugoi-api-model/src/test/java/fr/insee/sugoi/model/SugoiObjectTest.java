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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SugoiObjectTest {
  @Test
  void testGet() throws Exception {
    User user = new User();
    user.setLastName("roger");
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("roger", "rabbit");
    attributes.put("fieldwithnullvalue", null);
    user.setAttributes(attributes);
    assertThat(user.get("lastname"), is(Optional.of("roger")));
    assertThat(user.get("attributes.roger"), is(Optional.of("rabbit")));
    assertThat(user.get("attributes.nonexistingfield"), is(Optional.empty()));
    assertThat(user.get("firstname"), is(Optional.empty()));
    assertThat(user.get("attributes.fieldwithnullvalue"), is(Optional.empty()));
    assertThrows(ClassCastException.class, () -> user.get("firstname.toto"));
    assertThrows(NoSuchFieldException.class, () -> user.get("mapwithoutname.name"));
  }

  @Test
  void testSet() throws Exception {
    User user = new User();
    user.setLastName("roger");
    user.setAttributes(Map.of("roger", "rabbit"));
    // already existing simple
    user.set("lastname", "Stephane");
    assertThat(user.get("lastname"), is(Optional.of("Stephane")));
    // already existing map
    user.set("attributes.roger", "doe");
    assertThat(user.get("attributes.roger"), is(Optional.of("doe")));
    // not existing simple
    assertThrows(NoSuchFieldException.class, () -> user.set("tototo", "Roger"));
    // not existing map
    assertThrows(NoSuchFieldException.class, () -> user.set("azoeia.yolo", "yolo"));
  }
}
