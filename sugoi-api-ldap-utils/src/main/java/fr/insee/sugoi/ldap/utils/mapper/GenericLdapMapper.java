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
import fr.insee.sugoi.ldap.utils.LdapUtils;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.ldap.utils.mapper.properties.AddressLdap;
import fr.insee.sugoi.ldap.utils.mapper.properties.GroupLdap;
import fr.insee.sugoi.ldap.utils.mapper.properties.LdapObjectClass;
import fr.insee.sugoi.ldap.utils.mapper.properties.OrganizationLdap;
import fr.insee.sugoi.ldap.utils.mapper.properties.utils.AttributeLdapName;
import fr.insee.sugoi.ldap.utils.mapper.properties.utils.MapToAttribute;
import fr.insee.sugoi.ldap.utils.mapper.properties.utils.MapToMapElement;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Habilitation;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GenericLdapMapper {

  private static final Logger logger = LogManager.getLogger(GenericLdapMapper.class);

  public static <LdapType, ReturnType> ReturnType mapLdapAttributesToObject(
      Collection<Attribute> attributes,
      Class<LdapType> ldapClazz,
      Class<ReturnType> returnClazz,
      Map<String, String> config) {
    try {
      ReturnType mappedEntity = returnClazz.getDeclaredConstructor().newInstance();

      Arrays.stream(ldapClazz.getDeclaredFields())
          .filter(
              ldapField ->
                  ldapField.getDeclaredAnnotationsByType(AttributeLdapName.class).length > 0
                      && ldapField.getDeclaredAnnotationsByType(MapToAttribute.class).length > 0)
          .forEach(
              ldapField ->
                  setFieldFromAttributeAnnotation(mappedEntity, ldapField, attributes, config));

      Arrays.stream(ldapClazz.getDeclaredFields())
          .filter(
              ldapField ->
                  ldapField.getDeclaredAnnotationsByType(AttributeLdapName.class).length > 0
                      && ldapField.getDeclaredAnnotationsByType(MapToMapElement.class).length > 0)
          .forEach(ldapField -> setFieldFromMapAnnotation(mappedEntity, ldapField, attributes));

      return mappedEntity;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <LdapType, ObjectType> List<Attribute> mapObjectToLdapAttributes(
      ObjectType entity,
      Class<LdapType> ldapClazz,
      Class<ObjectType> entityClazz,
      Map<String, String> config) {
    try {
      List<Attribute> attributes = new ArrayList<>();
      LdapObjectClass ldapObjectClass = ldapClazz.getAnnotation(LdapObjectClass.class);

      if (ldapObjectClass != null) {
        attributes.add(new Attribute("objectClass", ldapObjectClass.values()));
      }

      Arrays.stream(ldapClazz.getDeclaredFields())
          .filter(
              ldapField ->
                  ldapField.getDeclaredAnnotationsByType(AttributeLdapName.class).length > 0
                      && ldapField.getDeclaredAnnotationsByType(MapToAttribute.class).length > 0
                      && !ldapField.getAnnotation(MapToAttribute.class).readonly())
          .forEach(
              ldapField -> {
                List<Attribute> createdAttributeList =
                    createAttributesFromAttributeAnnotation(entity, ldapField, config);
                if (createdAttributeList != null) {
                  attributes.addAll(createdAttributeList);
                }
              });

      Arrays.stream(ldapClazz.getDeclaredFields())
          .filter(
              ldapField ->
                  ldapField.getDeclaredAnnotationsByType(AttributeLdapName.class).length > 0
                      && ldapField.getDeclaredAnnotationsByType(MapToMapElement.class).length > 0)
          .forEach(
              ldapField -> {
                List<Attribute> mappedAttribute =
                    createAttributesFromMapAnnotation(entity, ldapField);
                if (mappedAttribute != null) {
                  attributes.addAll(mappedAttribute);
                }
              });

      return attributes;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <O, N> List<Modification> createMods(
      N entity, Class<O> propertiesClazz, Class<N> clazz, Map<String, String> config) {
    return LdapUtils.convertAttributesToModifications(
        mapObjectToLdapAttributes(entity, propertiesClazz, clazz, config));
  }

  private static <ObjectType> void setFieldFromAttributeAnnotation(
      ObjectType mappedEntity,
      Field ldapField,
      Collection<Attribute> attributes,
      Map<String, String> config) {
    try {
      Field entityField =
          mappedEntity
              .getClass()
              .getDeclaredField(ldapField.getAnnotation(MapToAttribute.class).value());
      entityField.setAccessible(true);
      ModelType type = ldapField.getAnnotation(MapToAttribute.class).type();
      List<String> attributeValues = getAttributeValuesFromField(attributes, ldapField);
      if (attributeValues.size() > 0) {
        switch (type) {
          case ORGANIZATION:
            Organization orga = new Organization();
            orga.setIdentifiant(LdapUtils.getNodeValueFromDN(attributeValues.get(0)));
            entityField.set(mappedEntity, orga);
            break;
          case ADDRESS:
            Map<String, String> address = new HashMap<>();
            address.put("id", LdapUtils.getNodeValueFromDN(attributeValues.get(0)));
            entityField.set(mappedEntity, address);
            break;
          case LIST_HABILITATION:
            entityField.set(
                mappedEntity,
                attributeValues.stream()
                    .filter(
                        attributeValue ->
                            attributeValue.split("_").length == 2
                                || attributeValue.split("_").length == 3)
                    .map(attributeValue -> new Habilitation(attributeValue))
                    .collect(Collectors.toList()));
            break;
          case LIST_USER:
            entityField.set(
                mappedEntity,
                attributeValues.stream()
                    .map(attributeValue -> new User(LdapUtils.getNodeValueFromDN(attributeValue)))
                    .collect(Collectors.toList()));
            break;
          case LIST_GROUP:
            entityField.set(
                mappedEntity,
                attributeValues.stream()
                    .map(
                        attributeValue -> {
                          Pattern pattern =
                              Pattern.compile(
                                  config
                                      .get(LdapConfigKeys.GROUP_SOURCE_PATTERN)
                                      .replace("{appliname}", "(.*)"));
                          Matcher matcher =
                              pattern.matcher(
                                  attributeValue.substring(attributeValue.indexOf(",") + 1));
                          if (matcher.matches()) {
                            return new Group(
                                matcher.group(1), LdapUtils.getNodeValueFromDN(attributeValue));
                          } else {
                            return null;
                          }
                        })
                    .collect(Collectors.toList()));
            break;
          case LIST_STRING:
            entityField.set(mappedEntity, attributeValues);
            break;
          default:
            entityField.set(mappedEntity, attributeValues.get(0));
        }
      }
    } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
      logger.info("Unable to map field " + ldapField.getName());
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  private static <ObjectType> void setFieldFromMapAnnotation(
      ObjectType mappedEntity, Field ldapField, Collection<Attribute> attributes) {
    try {
      Field entityField =
          mappedEntity
              .getClass()
              .getDeclaredField(ldapField.getAnnotation(MapToMapElement.class).name());
      entityField.setAccessible(true);
      List<String> attributeValue = getAttributeValuesFromField(attributes, ldapField);
      if (attributeValue.size() > 0) {
        Map<String, Object> entityFieldMap = (Map<String, Object>) entityField.get(mappedEntity);
        switch (ldapField.getAnnotation(MapToMapElement.class).type()) {
          case LIST_STRING:
            entityFieldMap.put(
                ldapField.getAnnotation(MapToMapElement.class).key(), attributeValue);
            break;
          default:
            entityFieldMap.put(
                ldapField.getAnnotation(MapToMapElement.class).key(), attributeValue.get(0));
            break;
        }
        entityField.set(mappedEntity, entityFieldMap);
      }
    } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
      logger.info("Unable to map field " + ldapField.getName());
      throw new RuntimeException(e);
    }
  }

  private static List<String> getAttributeValuesFromField(
      Collection<Attribute> attributes, Field ldapField) {
    List<String> attributeValueStrings = new ArrayList<>();
    attributes.stream()
        .filter(
            attribute ->
                ldapField
                    .getAnnotation(AttributeLdapName.class)
                    .value()
                    .equalsIgnoreCase(attribute.getName()))
        .forEach(attribute -> attributeValueStrings.addAll(Arrays.asList(attribute.getValues())));
    return attributeValueStrings;
  }

  @SuppressWarnings("unchecked")
  private static <ObjectType> List<Attribute> createAttributesFromAttributeAnnotation(
      ObjectType entity, Field ldapField, Map<String, String> config) {
    try {
      Field entityField =
          entity.getClass().getDeclaredField(ldapField.getAnnotation(MapToAttribute.class).value());
      entityField.setAccessible(true);
      Object attributeValue = entityField.get(entity);
      String attributeName = ldapField.getAnnotation(AttributeLdapName.class).value();
      if (attributeValue != null) {
        ModelType type = ldapField.getAnnotation(MapToAttribute.class).type();
        switch (type) {
          case ORGANIZATION:
            Attribute organizationAttribute =
                new Attribute(
                    attributeName,
                    String.format(
                        "%s=%s,%s",
                        OrganizationLdap.class
                            .getAnnotation(LdapObjectClass.class)
                            .rdnAttributeName(),
                        ((Organization) attributeValue).getIdentifiant(),
                        config.get(LdapConfigKeys.ORGANIZATION_SOURCE)));
            List<Attribute> organizationAttributeList = new ArrayList<>();
            organizationAttributeList.add(organizationAttribute);
            return organizationAttributeList;
          case LIST_HABILITATION:
            return ((List<Habilitation>) attributeValue)
                .stream()
                    .filter(
                        habilitation ->
                            habilitation.getApplication() != null && habilitation.getRole() != null)
                    .map(habilitation -> new Attribute(attributeName, habilitation.getId()))
                    .collect(Collectors.toList());
          case LIST_USER:
            return new ArrayList<>();
          case LIST_GROUP:
            return ((List<Group>) attributeValue)
                .stream()
                    .map(
                        group ->
                            new Attribute(
                                attributeName,
                                String.format(
                                    GroupLdap.class
                                        .getAnnotation(LdapObjectClass.class)
                                        .rdnAttributeName(),
                                    group.getName(),
                                    config.get(LdapConfigKeys.APP_SOURCE))))
                    .collect(Collectors.toList());
          case ADDRESS:
            List<Attribute> addressAttributeList = new ArrayList<>();
            if (((Map<String, String>) attributeValue).containsKey("id")
                && config.get(LdapConfigKeys.ADDRESS_SOURCE) != null) {
              Attribute addressAttribute =
                  new Attribute(
                      attributeName,
                      String.format(
                          "%s=%s,%s",
                          AddressLdap.class.getAnnotation(LdapObjectClass.class).rdnAttributeName(),
                          ((Map<String, String>) attributeValue).get("id"),
                          config.get(LdapConfigKeys.ADDRESS_SOURCE)));
              addressAttributeList.add(addressAttribute);
            }
            return addressAttributeList;
          case LIST_STRING:
            return ((List<String>) attributeValue)
                .stream()
                    .map(value -> new Attribute(attributeName, value))
                    .collect(Collectors.toList());
          default:
            if ((String) attributeValue != "") {
              return List.of(
                  new Attribute(
                      ldapField.getAnnotation(AttributeLdapName.class).value(),
                      (String) attributeValue));
            } else {
              return List.of();
            }
        }
      }
      return null;
    } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
      logger.info("Unable to map field " + ldapField.getName());
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  private static <ObjectType> List<Attribute> createAttributesFromMapAnnotation(
      ObjectType entity, Field ldapField) {
    try {
      Field entityField =
          entity.getClass().getDeclaredField(ldapField.getAnnotation(MapToMapElement.class).name());
      entityField.setAccessible(true);
      Map<String, Object> entityFieldObject = (Map<String, Object>) entityField.get(entity);
      if (!ldapField.getAnnotation(MapToMapElement.class).readonly()) {
        Object attributeValue =
            entityFieldObject.get(ldapField.getAnnotation(MapToMapElement.class).key());
        if (attributeValue != null) {
          ModelType type = ldapField.getAnnotation(MapToMapElement.class).type();
          switch (type) {
            case LIST_STRING:
              return ((List<String>) attributeValue)
                  .stream()
                      .map(
                          value ->
                              new Attribute(
                                  ldapField.getAnnotation(AttributeLdapName.class).value(),
                                  (String) value))
                      .collect(Collectors.toList());
            default:
              List<Attribute> attributes = new ArrayList<>();
              attributes.add(
                  new Attribute(
                      ldapField.getAnnotation(AttributeLdapName.class).value(),
                      (String) attributeValue));
              return attributes;
          }
        }
      }
      return null;
    } catch (IllegalAccessException | NoSuchFieldException e) {
      logger.info("Unable to map field " + ldapField.getName());
      throw new RuntimeException(e);
    }
  }
}
