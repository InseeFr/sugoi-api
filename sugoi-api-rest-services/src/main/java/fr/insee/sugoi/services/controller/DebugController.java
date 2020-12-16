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
package fr.insee.sugoi.services.controller;

import fr.insee.sugoi.services.beans.MessageKeyValueGeneriqueBean;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug")
@SecurityRequirement(name = "oAuth")
public class DebugController {

  private static final Logger logger = LogManager.getLogger(DebugController.class);
  private static final String RETOUR_A_LA_LIGNE = "\n";

  @Autowired Environment env;

  @GetMapping(
      path = "/config",
      produces = {MediaType.TEXT_PLAIN_VALUE})
  @ResponseBody
  @PreAuthorize("@NewAuthorizeMethodDecider.isAdmin()")
  public ResponseEntity<?> printConfig() {

    logger.debug("Liste des properties");
    StringBuilder sb = new StringBuilder();
    sb.append(
        "---------------------------------------------------------------------------------------------------------"
            + RETOUR_A_LA_LIGNE);
    sb.append("		Liste des properties SUGOI				" + RETOUR_A_LA_LIGNE);
    sb.append(
        "---------------------------------------------------------------------------------------------------------"
            + RETOUR_A_LA_LIGNE);

    Collection<MessageKeyValueGeneriqueBean> listeKeyProperties;
    MessageKeyValueGeneriqueBean comp = null;

    try {
      listeKeyProperties = new TreeSet<MessageKeyValueGeneriqueBean>();

      Map<String, Object> map = new HashMap<>();
      for (Iterator<PropertySource<?>> it =
              ((AbstractEnvironment) env).getPropertySources().iterator();
          it.hasNext(); ) {
        PropertySource<?> propertySource = (PropertySource<?>) it.next();
        if (propertySource instanceof MapPropertySource) {
          map.putAll(((MapPropertySource) propertySource).getSource());
        }
      }

      for (String key : map.keySet()) {
        comp = new MessageKeyValueGeneriqueBean();
        comp.setKey(key);
        comp.setValue(map.get(key).toString());
        listeKeyProperties.add(comp);
      }

      for (MessageKeyValueGeneriqueBean mkgb : listeKeyProperties) {
        MessageKeyValueGeneriqueBean messageKeyValueGeneriqueBean =
            (MessageKeyValueGeneriqueBean) mkgb;
        sb.append(
            messageKeyValueGeneriqueBean.getKey().toString()
                + " = "
                + messageKeyValueGeneriqueBean.getValue().toString()
                + RETOUR_A_LA_LIGNE);
        sb.append(
            "--------------------------------------------"
                + "-------------------------------------------------------------"
                + RETOUR_A_LA_LIGNE);
      }

    } catch (Exception e) {
      logger.error(e, e);
      sb.append("Récupération des properties impossible !" + RETOUR_A_LA_LIGNE);
    }

    return new ResponseEntity<>(sb, HttpStatus.OK);
  }
}
