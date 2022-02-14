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

import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Modification;
import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.ldap.utils.LdapUtils;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Habilitation;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.PostalAddress;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.exceptions.LdapMappingConfigurationException;
import fr.insee.sugoi.model.technics.ModelType;
import fr.insee.sugoi.model.technics.StoreMapping;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GenericLdapMapper {

  public static <ReturnType> ReturnType mapLdapAttributesToObject(
      Collection<Attribute> attributes,
      Class<ReturnType> returnClazz,
      Map<String, String> config,
      List<StoreMapping> mappings) {

    try {
      ReturnType mappedEntity = returnClazz.getDeclaredConstructor().newInstance();
      for (StoreMapping mappingDefinition : mappings) {
        try {

          List<Attribute> correspondingAttributes =
              attributes.stream()
                  .filter(
                      attribute ->
                          mappingDefinition.getStoreName().equalsIgnoreCase(attribute.getName()))
                  .collect(Collectors.toList());
          if (!correspondingAttributes.isEmpty()) {
            Object sugoiAttribute =
                transformLdapAttributeToSugoiAttribute(
                    mappingDefinition.getModelType(), correspondingAttributes, config);
            if (mappingDefinition.getSugoiName().contains(".")) {
              putSugoiAttributeInEntityMapField(
                  sugoiAttribute, mappingDefinition.getSugoiName(), mappedEntity);
            } else {
              putSugoiAttributeInEntityField(
                  sugoiAttribute, mappingDefinition.getSugoiName(), mappedEntity);
            }
          } else if (mappingDefinition.getModelType() == ModelType.EXISTS) {
            if (mappingDefinition.getSugoiName().contains(".")) {
              putSugoiAttributeInEntityMapField(
                  false, mappingDefinition.getSugoiName(), mappedEntity);
            } else {
              putSugoiAttributeInEntityField(false, mappingDefinition.getSugoiName(), mappedEntity);
            }
          }
        } catch (Exception e) {
          throw new LdapMappingConfigurationException(
              "Error occured while mapping attribute to Ldap. Must be caused by the configuration "
                  + mappingDefinition
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
      Map<String, String> config,
      List<StoreMapping> mappings,
      List<String> objectClasses,
      boolean isToWrite) {
    List<Attribute> attributes = new ArrayList<>();
    if (objectClasses != null && !objectClasses.isEmpty()) {
      attributes.add(new Attribute("objectClass", objectClasses));
    }

    for (StoreMapping mappingDefinition : mappings) {
      try {

        if (mappingDefinition.isWritable() || !isToWrite) {
          if (mappingDefinition.getSugoiName().contains(".")) {
            String[] splitedFieldName = mappingDefinition.getSugoiName().split("\\.");
            String mapToModifyName = splitedFieldName[0];
            String keyToModify = splitedFieldName[1];
            Field modelField = entity.getClass().getDeclaredField(mapToModifyName);
            modelField.setAccessible(true);
            Map<String, Object> mapToModify = (Map<String, Object>) modelField.get(entity);
            if (mapToModify != null && mapToModify.containsKey(keyToModify)) {
              Object sugoiValue = mapToModify.get(keyToModify);
              if (sugoiValue != null) {
                attributes.addAll(
                    transformSugoiToAttribute(
                        mappingDefinition.getModelType(),
                        mappingDefinition.getStoreName(),
                        sugoiValue,
                        config));
              }
            }
          } else {
            Field sugoiField = entity.getClass().getDeclaredField(mappingDefinition.getSugoiName());
            sugoiField.setAccessible(true);
            Object sugoiValue = sugoiField.get(entity);
            if (sugoiValue != null) {
              attributes.addAll(
                  transformSugoiToAttribute(
                      mappingDefinition.getModelType(),
                      mappingDefinition.getStoreName(),
                      sugoiValue,
                      config));
            }
          }
        }
      } catch (Exception e) {
        throw new LdapMappingConfigurationException(
            "Error occured while mapping attribute to Ldap. Must be caused by the configuration "
                + mappingDefinition
                + " for entity "
                + entity.getClass().getName(),
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
        return new PostalAddress(LdapUtils.getNodeValueFromDN(attrs.get(0).getValue()));
      case LIST_HABILITATION:
        List<String> values = new ArrayList<>();
        attrs.stream().forEach(attribute -> values.addAll(Arrays.asList(attribute.getValues())));
        return values.stream()
            .filter(
                attributeValue ->
                    attributeValue.split("_").length == 2 || attributeValue.split("_").length == 3)
            .map(Habilitation::new)
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
                    config.get(GlobalKeysConfig.ORGANIZATION_SOURCE))));
      case ADDRESS:
        if (((PostalAddress) sugoiValue).getId() != null
            && config.get(GlobalKeysConfig.ADDRESS_SOURCE) != null) {
          return List.of(
              new Attribute(
                  ldapAttributeName,
                  String.format(
                      "%s=%s,%s",
                      // TODO should be a param
                      "l",
                      //
                      ((PostalAddress) sugoiValue).getId(),
                      config.get(GlobalKeysConfig.ADDRESS_SOURCE))));
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
                                "cn=%s,",
                                //
                                group.getName())))
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

  public static <SugoiType> List<Attribute> mapObjectToLdapAttributesForCreation(
      SugoiType entity,
      Map<String, String> config,
      List<StoreMapping> mappings,
      List<String> objectClasses) {
    return getAttributesWithoutEmptyValue(
        mapObjectToLdapAttributes(entity, config, mappings, objectClasses, true));
  }

  public static <SugoiType> List<Attribute> mapObjectToLdapAttributesForFilter(
      SugoiType entity,
      Map<String, String> config,
      List<StoreMapping> mappings,
      List<String> objectClasses) {
    return getAttributesWithoutEmptyValue(
        mapObjectToLdapAttributes(entity, config, mappings, objectClasses, false));
  }

  public static <O> List<Modification> createMods(
      O entity, Map<String, String> config, List<StoreMapping> mappings) {
    return LdapUtils.convertAttributesToModifications(
        // Modification => no need to specify object classes
        mapObjectToLdapAttributes(entity, config, mappings, null, true));
  }

  private static List<Attribute> getAttributesWithoutEmptyValue(List<Attribute> attributes) {
    return attributes.stream()
        .map(GenericLdapMapper::getAttributeWithoutEmptyValues)
        .filter(attribute -> attribute.getValues().length > 0)
        .collect(Collectors.toList());
  }

  private static Attribute getAttributeWithoutEmptyValues(Attribute attribute) {
    ASN1OctetString[] valuesFiltered =
        Arrays.asList(attribute.getRawValues()).stream()
            .filter(value -> value.getValueLength() > 0)
            .toArray(ASN1OctetString[]::new);
    return new Attribute(attribute.getName(), valuesFiltered);
  }
}
