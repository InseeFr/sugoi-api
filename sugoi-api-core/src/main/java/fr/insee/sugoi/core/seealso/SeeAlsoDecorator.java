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

import java.util.List;

public interface SeeAlsoDecorator {

  /**
   * Fetch a resource at url and parse it to retrieve the subobject which can be a String or
   * List<String>. The distant request timeout after 1 second.
   *
   * @param url location of the resource to parse (ex : http://example.org/ex). The url contain
   *     protocol and may contain the port.
   * @param subobject description of how to get the String or List<String> of the seeAlso
   * @return the String or List<String> described by the seeAlso
   */
  public Object getResourceFromUrl(String url, String subobject);

  /** @return the list of the protocols the SeeAlsoDecorator is able to deal with */
  public List<String> getProtocols();
}
