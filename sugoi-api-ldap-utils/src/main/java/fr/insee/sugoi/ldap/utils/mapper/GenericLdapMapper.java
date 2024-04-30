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
import fr.insee.sugoi.ldap.utils.exception.LdapMappingConfigurationException;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Habilitation;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.PostalAddress;
import fr.insee.sugoi.model.RealmConfigKeys;
import fr.insee.sugoi.model.SugoiObject;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.technics.ModelType;
import fr.insee.sugoi.model.technics.StoreMapping;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GenericLdapMapper {

  private GenericLdapMapper() throws IllegalAccessException {
    throw new IllegalAccessException("Utility class");
  }

  public static <R extends SugoiObject> R mapLdapAttributesToObject(
      Collection<Attribute> attributes,
      Class<R> returnClazz,
      Map<RealmConfigKeys, String> config,
      List<StoreMapping> mappings) {
    try {
      R mappedEntity = returnClazz.getDeclaredConstructor().newInstance();
      for (StoreMapping mappingDefinition : mappings) {
        try {

          List<Attribute> correspondingAttributes =
              attributes.stream()
                  .filter(
                      attribute ->
                          mappingDefinition.getStoreName().equalsIgnoreCase(attribute.getName()))
                  .collect(Collectors.toList());
          if (!correspondingAttributes.isEmpty()) {
            mappedEntity.set(
                mappingDefinition.getSugoiName(),
                transformLdapAttributeToSugoiAttribute(
                    mappingDefinition.getModelType(), correspondingAttributes, config));
          } else if (mappingDefinition.getModelType() == ModelType.EXISTS) {
            mappedEntity.set(mappingDefinition.getSugoiName(), false);
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
      throw new LdapMappingConfigurationException(
          "Exception while getting the entity " + returnClazz.getName(), e);
    }
  }

  private static <S extends SugoiObject> List<Attribute> mapObjectToLdapAttributes(
      S entity,
      Map<RealmConfigKeys, String> config,
      List<StoreMapping> mappings,
      List<String> objectClasses,
      boolean isToWrite) {
    List<Attribute> attributes = new ArrayList<>();
    if (objectClasses != null && !objectClasses.isEmpty()) {
      attributes.add(new Attribute("objectClass", objectClasses));
    }
    for (StoreMapping mappingDefinition : mappings) {
      if (mappingDefinition.isWritable() || !isToWrite) {
        try {
          Optional<Object> sugoiValue = entity.get(mappingDefinition.getSugoiName());
          if (sugoiValue.isPresent()) {
            Attribute mappedValue =
                transformSugoiToAttribute(
                    mappingDefinition.getModelType(),
                    mappingDefinition.getStoreName(),
                    sugoiValue.get(),
                    config);
            if (mappedValue != null) {
              attributes.add(mappedValue);
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
    }
    return attributes;
  }

  private static Object transformLdapAttributeToSugoiAttribute(
      ModelType type, List<Attribute> attrs, Map<RealmConfigKeys, String> config) {
    switch (type) {
      case STRING:
        return attrs.get(0).getValue();
      case BOOLEAN:
        return Boolean.parseBoolean(attrs.get(0).getValue());
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
        Pattern patternSuffixGroup =
            Pattern.compile(
                config.get(LdapConfigKeys.GROUP_SOURCE_PATTERN).replace("{appliname}", "(.*)"));
        Pattern patternGroupManager =
            Pattern.compile(
                config
                    .get(LdapConfigKeys.GROUP_MANAGER_SOURCE_PATTERN)
                    .replace("{appliname}", "(.*)"));
        return attrs.stream()
            .flatMap(attribute -> Arrays.stream(attribute.getValues()))
            .map(
                attributeValue -> {
                  // Match only suffix from application group
                  // TODO : Match real value of GROUP_SOURCE_PATTERN and not with substring(),
                  // without breaking other tests
                  String suffixGroup = attributeValue.substring(attributeValue.indexOf(",") + 1);
                  Matcher matcherSuffixGroup = patternSuffixGroup.matcher(suffixGroup);
                  // Match exact group pattern
                  Matcher matcherGroupManager = patternGroupManager.matcher(attributeValue);
                  if (matcherSuffixGroup.matches()) {
                    return new Group(
                        LdapUtils.getNodeValueFromDN(attributeValue), matcherSuffixGroup.group(1));
                  } else if (matcherGroupManager.matches()) {
                    return new Group(
                        LdapUtils.getNodeValueFromDN(attributeValue), matcherGroupManager.group(1));
                  } else {
                    return null;
                  }
                })
            .filter(Objects::nonNull)
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
  private static Attribute transformSugoiToAttribute(
      ModelType type,
      String ldapAttributeName,
      Object sugoiValue,
      Map<RealmConfigKeys, String> config) {
    switch (type) {
      case STRING:
        return new Attribute(ldapAttributeName, (String) sugoiValue);
      case BOOLEAN:
        return new Attribute(ldapAttributeName, ((Boolean) sugoiValue).toString());
      case ORGANIZATION:
        return new Attribute(
            ldapAttributeName,
            config
                .get(LdapConfigKeys.ORGANIZATION_DN_PATTERN)
                .replace("{id}", ((Organization) sugoiValue).getIdentifiant())
                .replace("{source}", config.get(GlobalKeysConfig.ORGANIZATION_SOURCE)));
      case ADDRESS:
        if (((PostalAddress) sugoiValue).getId() != null
            && config.get(GlobalKeysConfig.ADDRESS_SOURCE) != null) {
          return new Attribute(
              ldapAttributeName,
              config
                  .get(LdapConfigKeys.ADDRESS_DN_PATTERN)
                  .replace("{id}", ((PostalAddress) sugoiValue).getId())
                  .replace("{source}", config.get(GlobalKeysConfig.ADDRESS_SOURCE)));
        } else return null;
      case LIST_HABILITATION:
        // Assume that if list is empty it's really to set an empty list (delete all
        // values)
        // Habilitations list can be set null if not needed
        return new Attribute(
            ldapAttributeName,
            ((List<Habilitation>) sugoiValue)
                .stream()
                    // Dont check contents (application nor role) for searching purpose
                    .filter(habilitation -> habilitation.getId() != null)
                    .map(habilitation -> habilitation.getId())
                    .collect(Collectors.toList()));
      case LIST_GROUP:
        // Assume that if list is empty it's really to set an empty list (delete all
        // values)
        // Groups list can be set null if not needed
        String genericGroup =
            config
                .get(LdapConfigKeys.GROUP_DN_PATTERN)
                .replace("{source}", config.get(LdapConfigKeys.GROUP_SOURCE_PATTERN));

        return new Attribute(
            ldapAttributeName,
            ((List<Group>) sugoiValue)
                .stream()
                    .map(
                        group ->
                            genericGroup
                                .replace("{appliname}", group.getAppName())
                                .replace("{id}", group.getName()))
                    .collect(Collectors.toList()));
      case LIST_STRING:
        // Assume that if list is empty it's really to set an empty list (delete all
        // values)
        // Lists in attributes map can be set as null if not needed
        return new Attribute(
            ldapAttributeName,
            ((List<String>) sugoiValue).stream().map(value -> value).collect(Collectors.toList()));
      case LIST_USER:
      default:
        return null;
    }
  }

  public static <S extends SugoiObject> List<Attribute> mapObjectToLdapAttributesForCreation(
      S entity,
      Map<RealmConfigKeys, String> config,
      List<StoreMapping> mappings,
      List<String> objectClasses) {
    return getAttributesWithoutEmptyValue(
        mapObjectToLdapAttributes(entity, config, mappings, objectClasses, true));
  }

  public static <S extends SugoiObject> List<Attribute> mapObjectToLdapAttributesForFilter(
      S entity,
      Map<RealmConfigKeys, String> config,
      List<StoreMapping> mappings,
      List<String> objectClasses) {
    return getAttributesWithoutEmptyValue(
        mapObjectToLdapAttributes(entity, config, mappings, objectClasses, false));
  }

  public static <O extends SugoiObject> List<Modification> createMods(
      O entity, Map<RealmConfigKeys, String> config, List<StoreMapping> mappings) {
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
