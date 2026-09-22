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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

public class RealmConfigKeysDeserializer
    extends StdDeserializer<Map<RealmConfigKeys, List<String>>> {

  private static final RealmConfigKeysFinder configuration = new RealmConfigKeysFinder();

  protected RealmConfigKeysDeserializer() {
    super(RealmConfigKeys.class);
  }

  @Override
  public Map<RealmConfigKeys, List<String>> deserialize(JsonParser p, DeserializationContext ctxt) {
    JsonNode node = p.readValueAsTree();
    Map<RealmConfigKeys, List<String>> map = new HashMap<>();
    for (Entry<String, JsonNode> field : node.properties()) {
      RealmConfigKeys keyConfig = configuration.getRealmConfigKey(field.getKey());
      if (keyConfig != null) {
        map.put(keyConfig, new ArrayList<>());
        field.getValue().values().forEach(e -> map.get(keyConfig).add(e.asString()));
      }
    }
    return map;
  }
}
