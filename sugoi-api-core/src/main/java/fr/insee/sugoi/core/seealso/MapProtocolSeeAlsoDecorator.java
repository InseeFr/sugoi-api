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
package fr.insee.sugoi.core.seealso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("fr.insee.sugoi.seealso")
public class MapProtocolSeeAlsoDecorator {

  private final Map<String, SeeAlsoDecorator> seeAlsoDecoratorByProtocol;

  @Autowired
  public MapProtocolSeeAlsoDecorator(List<SeeAlsoDecorator> seeAlsoDecorators) {
    Map<String, SeeAlsoDecorator> map = new HashMap<>();
    for (SeeAlsoDecorator seeAlsoDecorator : seeAlsoDecorators) {
      for (String protocols : seeAlsoDecorator.getProtocols()) {
        map.put(protocols, seeAlsoDecorator);
      }
    }
    seeAlsoDecoratorByProtocol = map;
  }

  public Object getResourceFromUrl(String protocol, String url, String subobject) {
    SeeAlsoDecorator seeAlsoDecorator = seeAlsoDecoratorByProtocol.get(protocol);
    return seeAlsoDecorator != null ? seeAlsoDecorator.getResourceFromUrl(url, subobject) : null;
  }
}
