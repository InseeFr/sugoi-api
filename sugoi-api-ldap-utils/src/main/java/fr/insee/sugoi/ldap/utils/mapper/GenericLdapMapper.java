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
package fr.insee.sugoi.ldap.utils.mapper;

import com.unboundid.ldap.sdk.SearchResultEntry;
import fr.insee.sugoi.core.mapper.properties.utils.AttributeLdapName;
import fr.insee.sugoi.core.mapper.properties.utils.MapToAttribute;
import fr.insee.sugoi.core.mapper.properties.utils.MapToMapElement;
import java.lang.reflect.Field;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GenericLdapMapper {

  private static final Logger logger = LogManager.getLogger(GenericLdapMapper.class);

  @SuppressWarnings("unchecked")
  public static <O, N> N transform(
      SearchResultEntry result, Class<O> propertiesClazz, Class<N> clazz) {
    try {
      N object = clazz.getDeclaredConstructor().newInstance();
      for (Field field : propertiesClazz.getDeclaredFields()) {
        try {
          field.setAccessible(true);
          if (field.getDeclaredAnnotationsByType(AttributeLdapName.class).length > 0) {

            if (field.getDeclaredAnnotationsByType(MapToAttribute.class).length > 0) {
              Field userField =
                  object
                      .getClass()
                      .getDeclaredField(field.getAnnotation(MapToAttribute.class).value());
              userField.setAccessible(true);
              userField.set(
                  object,
                  result.getAttributeValue(field.getAnnotation(AttributeLdapName.class).value()));
            }

            if (field.getDeclaredAnnotationsByType(MapToMapElement.class).length > 0) {
              Field userField =
                  object
                      .getClass()
                      .getDeclaredField(field.getAnnotation(MapToMapElement.class).name());
              userField.setAccessible(true);
              Map<String, Object> userFieldObject = (Map<String, Object>) userField.get(object);
              userFieldObject.put(
                  field.getAnnotation(MapToMapElement.class).key(),
                  result.getAttributeValue(field.getAnnotation(AttributeLdapName.class).value()));
              userField.set(object, userFieldObject);
            }
          }

        } catch (Exception e) {
          logger.info("Impossible de récuperer le field " + field.getName());
        }
      }
      return object;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
