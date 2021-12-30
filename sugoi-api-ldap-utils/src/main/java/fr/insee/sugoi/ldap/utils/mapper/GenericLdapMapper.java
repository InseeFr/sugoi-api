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
import fr.insee.sugoi.core.exceptions.LdapMappingConfigurationException;
import fr.insee.sugoi.ldap.utils.LdapUtils;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Habilitation;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GenericLdapMapper {

  public static <ReturnType> ReturnType mapLdapAttributesToObject(
      Collection<Attribute> attributes,
      Class<ReturnType> returnClazz,
      Map<String, String> config,
      Map<String, String> mapping) {
    try {
      ReturnType mappedEntity = returnClazz.getDeclaredConstructor().newInstance();
      for (Entry<String, String> mappingDefinition : mapping.entrySet()) {
        try {
          String[] splitedMappingDefinition = mappingDefinition.getValue().split(",");
          String attributeLdapName = splitedMappingDefinition[0];
          ModelType mappingType = ModelType.valueOf(splitedMappingDefinition[1].toUpperCase());
          String fieldToSetName = mappingDefinition.getKey();
          List<Attribute> correspondingAttributes =
              attributes.stream()
                  .filter(attribute -> attributeLdapName.equalsIgnoreCase(attribute.getName()))
                  .collect(Collectors.toList());
          if (correspondingAttributes.size() > 0) {
            if (fieldToSetName.contains(".")) {
              Object sugoiAttribute =
                  transformLdapAttributeToSugoiAttribute(
                      mappingType, correspondingAttributes, config);
              putSugoiAttributeInEntityMapField(sugoiAttribute, fieldToSetName, mappedEntity);
            } else {
              Object sugoiAttribute =
                  transformLdapAttributeToSugoiAttribute(
                      mappingType, correspondingAttributes, config);
              putSugoiAttributeInEntityField(sugoiAttribute, fieldToSetName, mappedEntity);
            }
          } else if (mappingType == ModelType.EXISTS) {
            if (fieldToSetName.contains(".")) {
              putSugoiAttributeInEntityMapField(false, fieldToSetName, mappedEntity);
            } else {
              putSugoiAttributeInEntityField(false, fieldToSetName, mappedEntity);
            }
          }
        } catch (Exception e) {
          throw new LdapMappingConfigurationException(
              "Error occured while mapping attribute to Ldap. Must be caused by the configuration "
                  + mappingDefinition.getKey()
                  + ":"
                  + mappingDefinition.getValue()
                  + " for entity "
                  + returnClazz.getName(),
              e);
        }
      }
      return mappedEntity;
    } catch (InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException e) {
      throw new RuntimeException("Exception while getting the entity " + returnClazz.getName(), e);
    }
  }

  @SuppressWarnings("unchecked")
  private static <SugoiType> List<Attribute> mapObjectToLdapAttributes(
      SugoiType entity,
      Class<SugoiType> entityClazz,
      Map<String, String> config,
      Map<String, String> mapping,
      List<String> objectClasses) {
    List<Attribute> attributes = new ArrayList<>();
    if (objectClasses != null && !objectClasses.isEmpty()) {
      attributes.add(new Attribute("objectClass", objectClasses));
    }

    for (Entry<String, String> mappingDefinition : mapping.entrySet()) {
      try {
        String[] splitedMappingDefinition = mappingDefinition.getValue().split(",");
        String attributeLdapName = splitedMappingDefinition[0];
        ModelType mappingType = ModelType.valueOf(splitedMappingDefinition[1].toUpperCase());
        String readonlyStatus = splitedMappingDefinition[2];
        String fieldToSetName = mappingDefinition.getKey();
        if (!readonlyStatus.equalsIgnoreCase("ro")) {
          if (fieldToSetName.contains(".")) {
            String[] splitedFieldName = fieldToSetName.split("\\.");
            String mapToModifyName = splitedFieldName[0];
            String keyToModify = splitedFieldName[1];
            Field modelField = entity.getClass().getDeclaredField(mapToModifyName);
            modelField.setAccessible(true);
            Map<String, Object> map = (Map<String, Object>) modelField.get(entity);
            if (map != null && map.containsKey(keyToModify)) {
              Object sugoiValue = map.get(keyToModify);
              if (sugoiValue != null) {
                attributes.addAll(
                    transformSugoiToAttribute(mappingType, attributeLdapName, sugoiValue, config));
              }
            }
          } else {
            Field sugoiField = entity.getClass().getDeclaredField(fieldToSetName);
            sugoiField.setAccessible(true);
            Object sugoiValue = sugoiField.get(entity);
            if (sugoiValue != null) {
              attributes.addAll(
                  transformSugoiToAttribute(mappingType, attributeLdapName, sugoiValue, config));
            }
          }
        }
      } catch (Exception e) {
        throw new LdapMappingConfigurationException(
            "Error occured while mapping attribute to Ldap. Must be caused by the configuration "
                + mappingDefinition.getKey()
                + ":"
                + mappingDefinition.getValue()
                + " for entity "
                + entityClazz.getName(),
            e);
      }
    }
    return attributes;
  }

  private static Object transformLdapAttributeToSugoiAttribute(
      ModelType type, List<Attribute> attrs, Map<String, String> config)
      throws CertificateException {
    switch (type) {
      case STRING:
        return attrs.get(0).getValue();
      case BYTE_ARRAY:
        return attrs.get(0).getValueByteArray();
      case ORGANIZATION:
        Organization orga = new Organization();
        orga.setIdentifiant(LdapUtils.getNodeValueFromDN(attrs.get(0).getValue()));
        return orga;
      case ADDRESS:
        Map<String, String> address = new HashMap<>();
        address.put("id", LdapUtils.getNodeValueFromDN(attrs.get(0).getValue()));
        return address;
      case LIST_HABILITATION:
        List<String> values = new ArrayList<>();
        attrs.stream().forEach(attribute -> values.addAll(Arrays.asList(attribute.getValues())));
        return values.stream()
            .filter(
                attributeValue ->
                    attributeValue.split("_").length == 2 || attributeValue.split("_").length == 3)
            .map(attributeValue -> new Habilitation(attributeValue))
            .collect(Collectors.toList());
      case LIST_USER:
        values = new ArrayList<>();
        attrs.stream().forEach(attribute -> values.addAll(Arrays.asList(attribute.getValues())));
        return values.stream()
            .map(attributeValue -> new User(LdapUtils.getNodeValueFromDN(attributeValue)))
            .collect(Collectors.toList());
      case LIST_GROUP:
        values = new ArrayList<>();
        attrs.stream().forEach(attribute -> values.addAll(Arrays.asList(attribute.getValues())));
        return values.stream()
            .map(
                attributeValue -> {
                  Pattern pattern =
                      Pattern.compile(
                          config
                              .get(LdapConfigKeys.GROUP_SOURCE_PATTERN)
                              .replace("{appliname}", "(.*)"));
                  Matcher matcher =
                      pattern.matcher(attributeValue.substring(attributeValue.indexOf(",") + 1));
                  if (matcher.matches()) {
                    return new Group(
                        matcher.group(1), LdapUtils.getNodeValueFromDN(attributeValue));
                  } else {
                    return null;
                  }
                })
            .collect(Collectors.toList());
      case LIST_STRING:
        values = new ArrayList<>();
        attrs.stream().forEach(attribute -> values.addAll(Arrays.asList(attribute.getValues())));
        return values.stream().collect(Collectors.toList());
      case EXISTS:
        return true;
      default:
        return null;
    }
  }

  @SuppressWarnings("unchecked")
  private static List<Attribute> transformSugoiToAttribute(
      ModelType type, String ldapAttributeName, Object sugoiValue, Map<String, String> config) {
    if (sugoiValue instanceof List && ((List<?>) sugoiValue).isEmpty()) {
      return List.of(new Attribute(ldapAttributeName, ""));
    }
    switch (type) {
      case STRING:
        return List.of(new Attribute(ldapAttributeName, (String) sugoiValue));
      case ORGANIZATION:
        return List.of(
            new Attribute(
                ldapAttributeName,
                String.format(
                    "%s=%s,%s",
                    // TODO should be a param
                    "uid",
                    //
                    ((Organization) sugoiValue).getIdentifiant(),
                    config.get(LdapConfigKeys.ORGANIZATION_SOURCE))));
      case ADDRESS:
        if (((Map<String, String>) sugoiValue).containsKey("id")
            && config.get(LdapConfigKeys.ADDRESS_SOURCE) != null) {
          return List.of(
              new Attribute(
                  ldapAttributeName,
                  String.format(
                      "%s=%s,%s",
                      // TODO should be a param
                      "l",
                      //
                      ((Map<String, String>) sugoiValue).get("id"),
                      config.get(LdapConfigKeys.ADDRESS_SOURCE))));
        } else return List.of();
      case LIST_HABILITATION:
        return ((List<Habilitation>) sugoiValue)
            .stream()
                // Dont check contents (application nor role) for searching purpose
                .filter(habilitation -> habilitation.getId() != null)
                .map(habilitation -> new Attribute(ldapAttributeName, habilitation.getId()))
                .collect(Collectors.toList());
      case LIST_USER:
        return List.of();
      case LIST_GROUP:
        return ((List<Group>) sugoiValue)
            .stream()
                .map(
                    group ->
                        new Attribute(
                            ldapAttributeName,
                            String.format(
                                // TODO should be a param
                                "cn",
                                //
                                group.getName(),
                                config.get(LdapConfigKeys.APP_SOURCE))))
                .collect(Collectors.toList());
      case LIST_STRING:
        return ((List<String>) sugoiValue)
            .stream()
                .map(value -> new Attribute(ldapAttributeName, value))
                .collect(Collectors.toList());
      default:
        List.of();
    }
    return List.of();
  }

  private static <ReturnType> void putSugoiAttributeInEntityField(
      Object sugoiAttribute, String fieldToSetName, ReturnType sugoiEntity)
      throws NoSuchFieldException, IllegalAccessException {
    Field modelField = sugoiEntity.getClass().getDeclaredField(fieldToSetName);
    modelField.setAccessible(true);
    modelField.set(sugoiEntity, sugoiAttribute);
  }

  private static <ReturnType> void putSugoiAttributeInEntityMapField(
      Object sugoiAttribute, String fieldToSetName, ReturnType sugoiEntity)
      throws NoSuchFieldException, IllegalAccessException {
    String[] splitedFieldName = fieldToSetName.split("\\.");
    String mapToModifyName = splitedFieldName[0];
    String keyToModify = splitedFieldName[1];
    Field modelField = sugoiEntity.getClass().getDeclaredField(mapToModifyName);
    modelField.setAccessible(true);
    Map<String, Object> map =
        ((Map<?, ?>) modelField.get(sugoiEntity))
            .entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), e -> (Object) e.getValue()));
    map.put(keyToModify, sugoiAttribute);
    modelField.set(sugoiEntity, map);
  }

  public static <S> List<Attribute> createAttributes(
      S entity,
      Class<S> entityClazz,
      Map<String, String> config,
      Map<String, String> mapping,
      List<String> objectClasses) {
    return mapObjectToLdapAttributes(entity, entityClazz, config, mapping, objectClasses).stream()
        .filter(attribute -> attribute.hasValue() && !attribute.getValue().isBlank())
        .collect(Collectors.toList());
  }

  public static <O> List<Modification> createMods(
      O entity, Class<O> propertiesClazz, Map<String, String> config, Map<String, String> mapping) {
    return LdapUtils.convertAttributesToModifications(
        // Modification => no need to specify object classes
        mapObjectToLdapAttributes(entity, propertiesClazz, config, mapping, null));
  }
}
