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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RealmConfigKeysDeserializer
    extends StdDeserializer<Map<RealmConfigKeys, List<String>>> {

  private static final RealmConfigKeysFinder configuration = new RealmConfigKeysFinder();

  protected RealmConfigKeysDeserializer() {
    this(null);
  }

  protected RealmConfigKeysDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public Map<RealmConfigKeys, List<String>> deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException {
    JsonNode node = p.readValueAsTree();
    Map<RealmConfigKeys, List<String>> map = new HashMap<>();
    Iterator<Entry<String, JsonNode>> fieldsIterator = node.fields();
    while (fieldsIterator.hasNext()) {
      Entry<String, JsonNode> field = fieldsIterator.next();
      RealmConfigKeys keyConfig = configuration.getRealmConfigKey(field.getKey());
      if (keyConfig != null) {
        map.put(keyConfig, new ArrayList<>());
        field.getValue().elements().forEachRemaining(e -> map.get(keyConfig).add(e.asText()));
      }
    }
    return map;
  }
}
