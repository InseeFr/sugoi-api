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
import fr.insee.sugoi.core.seealso.SeeAlsoCredentialsConfiguration.SeeAlsoCredential;
import fr.insee.sugoi.core.seealso.SeeAlsoDecorator;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class HttpSeeAlsoDecorator implements SeeAlsoDecorator {

  @Autowired Map<String, SeeAlsoCredential> credentialsByDomain;

  @Value("${fr.insee.sugoi.seealso.http.timeout:1}")
  private int secondsBeforeTimeout;

  private Map<String, WebClient> clientsByDomain = new HashMap<>();
  private static final Logger logger = LoggerFactory.getLogger(HttpSeeAlsoDecorator.class);

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
    } catch (MalformedURLException e) {
      logger.error("{} is malformed : {}", url, e.getLocalizedMessage());
      return null;
    } catch (IOException e) {
      logger.error("Failed to retrieve object from {} : {}", url, e.getLocalizedMessage());
      return null;
    }
  }

  private String getHttpResourceBody(String url) throws MalformedURLException {
    return getClientByDomain(url)
        .get()
        .retrieve()
        .bodyToMono(String.class)
        .timeout(Duration.ofSeconds(secondsBeforeTimeout))
        .block();
  }

  private WebClient getClientByDomain(String stringUrl) throws MalformedURLException {
    URL url = new URL(stringUrl);
    URL baseUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), "");
    clientsByDomain.putIfAbsent(baseUrl.toString(), createWebClient(baseUrl));
    return clientsByDomain.get(baseUrl.toString());
  }

  private WebClient createWebClient(URL baseUrl) {
    return WebClient.builder()
        .baseUrl(baseUrl.toString())
        .defaultHeaders(httpHeaders -> addBasicAuthFromUrl(httpHeaders, baseUrl.getHost()))
        .build();
  }

  private void addBasicAuthFromUrl(HttpHeaders httpHeaders, String host) {
    if (credentialsByDomain != null && credentialsByDomain.containsKey(host)) {
      httpHeaders.setBasicAuth(
          credentialsByDomain.get(host).getUsername(), credentialsByDomain.get(host).getPassword());
    }
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
