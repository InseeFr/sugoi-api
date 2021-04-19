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
package fr.insee.sugoi.seealso;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.core.seealso.SeeAlsoDecorator;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class HttpSeeAlsoDecorator implements SeeAlsoDecorator {

  private WebClient client = WebClient.create();

  @Override
  public List<String> getProtocols() {
    return List.of("http", "https");
  }

  /**
   * Deal with fetching a resource with http or https protocol and parse it as a JSON node.
   *
   * @param url
   * @param subobject the json representation of the node to extract. The succession of node in
   *     which to get node are separated with dot. When fetching an array the position of the item
   *     to fetch is described under []. For example items[3].something is a valid subobject.
   * @return an Object that may be a String or a List<String> depending on if the node is text or an
   *     array
   */
  @Override
  public Object getResourceFromUrl(String url, String subobject) {
    try {
      JsonNode rootNode = (new ObjectMapper()).readTree(getHttpResourceBody(url));
      return transformJsonToValue(rootNode, subobject);
    } catch (IOException | InterruptedException e) {
      return null;
    }
  }

  private String getHttpResourceBody(String url) throws IOException, InterruptedException {
    return client
        .get()
        .uri(url)
        .retrieve()
        .bodyToMono(String.class)
        .timeout(Duration.ofSeconds(1))
        .block();
  }

  private Object transformJsonToValue(JsonNode rootNode, String subobject) {
    String[] leafs = subobject.split("\\.");
    for (String leaf : leafs) {
      String isArrayPositionRegex = ".*\\[[0-9]+\\]";
      if (leaf.matches(isArrayPositionRegex)) {
        int arrayPosition =
            Integer.parseInt(leaf.substring(leaf.lastIndexOf("[") + 1, leaf.lastIndexOf("]")));
        rootNode = rootNode.get(leaf.substring(0, leaf.lastIndexOf("[")));
        rootNode = rootNode.get(arrayPosition);
      } else {
        rootNode = rootNode.get(leaf);
      }
    }

    if (rootNode.isArray()) {
      List<String> result = new ArrayList<>();
      rootNode.forEach(e -> result.add(e.asText()));
      return result;
    } else {
      return rootNode.asText();
    }
  }
}
