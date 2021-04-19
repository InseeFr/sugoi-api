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

import fr.insee.sugoi.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeeAlsoService {

  protected static final Logger logger = LogManager.getLogger(SeeAlsoService.class);

  @Autowired private MapProtocolSeeAlsoDecorator mapProtocol;

  /**
   * Add an attribute to a user according to a seeAlso. This attribute can be a String or a
   * List<String>. SeeAlso is a String described as "url|subobject|attributetoupdate"
   *
   * <p>The name of the attribute added is attributetoupdate.
   *
   * <p>The value to parse is part of the resource fetched at a url (ex : http://example.org/ex).
   * The url contain protocol and may contain a port.
   *
   * <p>The resource is then parsed and transformed according to the suboject attribute and to the
   * decorator linked to the url protocol.
   *
   * <p>If the retrieving fails no attribute is added.
   *
   * @param user which to add attributes
   * @param seeAlso
   * @return the modified user
   */
  public User decorateWithSeeAlso(User user, String seeAlso) {
    Object valueOfNewAttribute = getSeeAlsoFieldValue(seeAlso);
    if (valueOfNewAttribute != null) {
      user.addAttributes(getSeeAlsoFieldName(seeAlso), getSeeAlsoFieldValue(seeAlso));
    }
    return user;
  }

  private Object getSeeAlsoFieldValue(String seeAlso) {
    try {
      String url = seeAlso.split("\\|")[0];
      String subobject = seeAlso.split("\\|")[1];
      String protocol = url.split(":")[0];
      return mapProtocol.getResourceFromUrl(protocol, url, subobject);
    } catch (Exception e) {
      logger.error("Error while retrieving the seeAlso value from " + seeAlso + " : " + e);
      return null;
    }
  }

  private String getSeeAlsoFieldName(String seeAlso) {
    return seeAlso.split("\\|")[2];
  }
}
