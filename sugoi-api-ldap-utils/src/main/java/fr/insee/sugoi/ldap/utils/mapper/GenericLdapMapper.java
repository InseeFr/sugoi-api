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

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.SearchResultEntry;
import fr.insee.sugoi.ldap.utils.mapper.properties.LdapObjectClass;
import fr.insee.sugoi.ldap.utils.mapper.properties.utils.AttributeLdapName;
import fr.insee.sugoi.ldap.utils.mapper.properties.utils.MapToAttribute;
import fr.insee.sugoi.ldap.utils.mapper.properties.utils.MapToMapElement;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

  @SuppressWarnings("unchecked")
  public static <O, N> List<Attribute> toAttribute(
      N entity, Class<O> propertiesClazz, Class<N> clazz) {
    try {
      List<Attribute> attributes = new ArrayList<>();
      Arrays.stream(propertiesClazz.getAnnotations())
          .forEach(x -> System.out.println(x.annotationType()));
      LdapObjectClass ldapObjectClass = propertiesClazz.getAnnotation(LdapObjectClass.class);
      if (ldapObjectClass != null) {
        attributes.add(new Attribute("objectClass", ldapObjectClass.values()));
      }
      for (Field mapperField : propertiesClazz.getDeclaredFields()) {
        try {
          mapperField.setAccessible(true);
          if (mapperField.getDeclaredAnnotationsByType(AttributeLdapName.class).length > 0) {
            String attributeName = mapperField.getAnnotation(AttributeLdapName.class).value();
            if (mapperField.getDeclaredAnnotationsByType(MapToAttribute.class).length > 0) {
              Field entityField =
                  entity
                      .getClass()
                      .getDeclaredField(mapperField.getAnnotation(MapToAttribute.class).value());
              entityField.setAccessible(true);
              Object attributeValue = entityField.get(entity);
              if (attributeValue != null) {
                ModelType type = mapperField.getAnnotation(MapToAttribute.class).type();
                switch (type) {
                    // TODO: manage other types
                  default:
                    attributes.add(new Attribute(attributeName, (String) attributeValue));
                    break;
                }
              }
            }

            if (mapperField.getDeclaredAnnotationsByType(MapToMapElement.class).length > 0) {
              Field entityField =
                  entity
                      .getClass()
                      .getDeclaredField(mapperField.getAnnotation(MapToMapElement.class).name());
              entityField.setAccessible(true);
              Map<String, Object> entityFieldObject = (Map<String, Object>) entityField.get(entity);
              Object attributeValue =
                  entityFieldObject.get(mapperField.getAnnotation(MapToMapElement.class).key());
              if (attributeValue != null) {
                ModelType type = mapperField.getAnnotation(MapToMapElement.class).type();
                switch (type) {
                    // TODO: manage other types
                  default:
                    attributes.add(new Attribute(attributeName, (String) attributeValue));
                    break;
                }
              }
            }
          }

        } catch (Exception e) {
          logger.info("Impossible de récuperer le field " + mapperField.getName());
        }
      }
      return attributes;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <O, N> List<Modification> createMods(
      N entity, Class<O> propertiesClazz, Class<N> clazz) {
    return toAttribute(entity, propertiesClazz, clazz).stream()
        .map(
            attribute ->
                new Modification(
                    ModificationType.REPLACE, attribute.getName(), attribute.getValues()))
        .collect(Collectors.toList());
  }
}
