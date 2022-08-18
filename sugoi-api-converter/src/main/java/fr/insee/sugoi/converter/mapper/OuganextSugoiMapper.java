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
package fr.insee.sugoi.converter.mapper;

import fr.insee.sugoi.converter.exception.OuganextParsingException;
import fr.insee.sugoi.converter.ouganext.*;
import fr.insee.sugoi.converter.utils.MapFromAttribute;
import fr.insee.sugoi.converter.utils.MapFromHashmapElement;
import fr.insee.sugoi.model.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OuganextSugoiMapper {

  private static final Logger logger = LoggerFactory.getLogger(OuganextSugoiMapper.class);

  /**
   * Convert Ouganext O object to Sugoi N object
   *
   * @param <O>
   * @param <N>
   * @param ouganextObject
   * @return
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws NoSuchFieldException
   * @throws SecurityException
   * @throws InvocationTargetException
   * @throws NoSuchMethodException
   * @throws InstantiationException
   */
  public <O, N extends SugoiObject> N serializeToSugoi(O ouganextObject, Class<N> clazz) {
    try {
      N sugoiObject = clazz.getDeclaredConstructor().newInstance();
      Class<? extends Object> ouganextObjectClass = ouganextObject.getClass();
      Field[] ouganextObjectFields = ouganextObjectClass.getDeclaredFields();
      for (Field ouganextObjectField : ouganextObjectFields) {
        try {
          ouganextObjectField.setAccessible(true);
          Object ouganextFieldObject = ouganextObjectField.get(ouganextObject);

          if (ouganextObjectField.getDeclaredAnnotationsByType(MapFromAttribute.class).length > 0) {
            sugoiObject.set(getAnnotationAttributeName(ouganextObjectField), ouganextFieldObject);
          }

          if (ouganextObjectField.getDeclaredAnnotationsByType(MapFromHashmapElement.class).length
              > 0) {
            Map<String, String> fieldInfo = getHashMapAnnotationInfo(ouganextObjectField);
            sugoiObject.set(
                fieldInfo.get("name") + "." + fieldInfo.get("key"), ouganextFieldObject);
          }

          if (ouganextObjectField.getName().equalsIgnoreCase("adresse")
              && ouganextFieldObject != null) {
            sugoiObject.set(
                "address",
                createSugoiAddressFromOuganextAddress((AdresseOuganext) ouganextFieldObject));
          }

          if (ouganextObjectField.getName().contains("organisationDeRattachement")
              && ouganextFieldObject != null) {
            sugoiObject.set(
                "organization", serializeToSugoi(ouganextFieldObject, Organization.class));
          }

        } catch (Exception e) {
          logger.info(
              "Erreur lors de la conversion de l'objet "
                  + ouganextObject.getClass().toString()
                  + " sur le champs "
                  + ouganextObjectField.getName(),
              e);
        }
      }
      return sugoiObject;
    } catch (Exception e) {
      throw new OuganextParsingException(
          String.format(
              "Parsing exception from ouganext %s to sugoi %s",
              ouganextObject.getClass().getName(), clazz.getName()),
          e);
    }
  }

  /**
   * Convert Sugoi N object to Ouganext O object
   *
   * @return
   * @throws IllegalArgumentException
   * @throws SecurityException
   */
  public <O extends SugoiObject, N> N serializeToOuganext(O sugoiObject, Class<N> clazz) {
    try {
      N ouganextObject = clazz.getDeclaredConstructor().newInstance();
      Field[] ouganextObjectFields = clazz.getDeclaredFields();
      for (Field ouganextObjectField : ouganextObjectFields) {
        try {
          ouganextObjectField.setAccessible(true);
          if (ouganextObjectField.getDeclaredAnnotationsByType(MapFromAttribute.class).length > 0) {
            Optional<Object> sugoiFieldObject =
                sugoiObject.get(getAnnotationAttributeName(ouganextObjectField));
            if (sugoiFieldObject.isPresent()) {
              ouganextObjectField.set(ouganextObject, sugoiFieldObject.get());
            }
          }
          if (ouganextObjectField.getDeclaredAnnotationsByType(MapFromHashmapElement.class).length
              > 0) {
            Map<String, String> fieldInfo = getHashMapAnnotationInfo(ouganextObjectField);
            Optional<Object> sugoiFieldObject =
                sugoiObject.get(fieldInfo.get("name") + "." + fieldInfo.get("key"));
            if (sugoiFieldObject.isPresent()) {
              ouganextObjectField.set(ouganextObject, sugoiFieldObject.get());
            }
          }
          // cas ou le field correspond a l'adresse
          if (ouganextObjectField.getName().equalsIgnoreCase("adresse")) {
            Optional<Object> address = sugoiObject.get("address");
            if (address.isPresent()) {
              ouganextObjectField.set(
                  ouganextObject,
                  createOuganextAddressFromSugoiAddress((PostalAddress) address.get()));
            }
          }

          // cas ou le field est une organisation
          if (ouganextObjectField.getName().contains("organisationDeRattachement")) {
            Optional<Object> organization = sugoiObject.get("organization");
            if (organization.isPresent()) {
              OrganisationOuganext organisation =
                  serializeToOuganext(
                      (Organization) organization.get(), OrganisationOuganext.class);
              ouganextObjectField.set(ouganextObject, organisation);
            }
          }

        } catch (Exception e) {
          logger.info(
              "Erreur lors de la conversion de l'objet "
                  + ouganextObject.getClass().toString()
                  + " sur le champs "
                  + ouganextObjectField.getName(),
              e);
        }
      }
      return ouganextObject;
    } catch (Exception e) {
      throw new OuganextParsingException(
          String.format(
              "Parsing exception from sugoi %s to ouganext %s",
              sugoiObject.getClass().getName(), clazz.getName()),
          e);
    }
  }

  public <O> Map<String, String> serializeOuganextToMap(O ouganextObject) {
    Map<String, String> map = new HashMap<>();
    try {
      Field[] ouganextObjectFields = ouganextObject.getClass().getDeclaredFields();
      for (Field ouganextObjectField : ouganextObjectFields) {
        ouganextObjectField.setAccessible(true);
        String fieldValue;
        if (ouganextObjectField.get(ouganextObject) != null) {
          fieldValue = ouganextObjectField.get(ouganextObject).toString();
          String fieldName = ouganextObjectField.getName();
          map.put(fieldName, fieldValue);
        }
      }
      return map;
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private AdresseOuganext createOuganextAddressFromSugoiAddress(PostalAddress sugoiAddress) {
    return new AdresseOuganext(sugoiAddress.getLines());
  }

  private PostalAddress createSugoiAddressFromOuganextAddress(AdresseOuganext ouganextAddress) {
    PostalAddress sugoiAddress = new PostalAddress();
    sugoiAddress.setLines(
        new String[] {
          ouganextAddress.getLigneUne(),
          ouganextAddress.getLigneDeux(),
          ouganextAddress.getLigneTrois(),
          ouganextAddress.getLigneQuatre(),
          ouganextAddress.getLigneCinq(),
          ouganextAddress.getLigneSix(),
          ouganextAddress.getLigneSept()
        });
    return sugoiAddress;
  }

  private static String getAnnotationAttributeName(Field field) {
    return field.getAnnotation(MapFromAttribute.class).attributeName();
  }

  private static Map<String, String> getHashMapAnnotationInfo(Field field) {
    Map<String, String> result = new HashMap<String, String>();
    result.put("name", field.getAnnotation(MapFromHashmapElement.class).hashMapName());
    result.put("key", field.getAnnotation(MapFromHashmapElement.class).hashMapKey());
    return result;
  }

  public static HabilitationsOuganext convertHabilitationToHabilitations(
      List<Habilitation> habilitations) {
    Map<String, ApplicationOuganext> applications = new HashMap<>();
    for (Habilitation habilitation : habilitations) {
      if (applications.containsKey(habilitation.getApplication())) {
        ApplicationOuganext application = applications.get(habilitation.getApplication());
        // Role already exist
        if (application.getRole().stream()
                .filter(role -> role.getName().equalsIgnoreCase(habilitation.getRole()))
                .count()
            > 0) {
          // property doesn't exist
          application.getRole().stream()
              .filter(role -> role.getName().equalsIgnoreCase(habilitation.getRole()))
              .forEach(role -> role.getPropriete().add(habilitation.getProperty()));
        }
        // role doesn't exist
        else {
          RoleOuganext role = new RoleOuganext();
          role.setName(habilitation.getRole());
          role.getPropriete().add(habilitation.getProperty());
          application.getRole().add(role);
        }
        applications.put(application.getName(), application);
      } else {
        // applictaion doesn't exist
        ApplicationOuganext application = new ApplicationOuganext();
        application.setName(habilitation.getApplication());
        RoleOuganext role = new RoleOuganext();
        role.setName(habilitation.getRole());
        role.getPropriete().add(habilitation.getProperty());
        application.getRole().add(role);
        applications.put(application.getName(), application);
      }
    }
    HabilitationsOuganext habilitationsOuganext = new HabilitationsOuganext();
    List<ApplicationOuganext> applicationList =
        new ArrayList<ApplicationOuganext>(applications.values());
    habilitationsOuganext.setApplicationList(applicationList);
    return habilitationsOuganext;
  }

  /**
   * This function is used to address the specificity of profil converting such as : - the name
   * which must be extracted from the pattern (which is ouganext pattern :
   * Profil_{realm}_WebservicesLdap) - the UserStorage which takes several values from profil
   */
  public Realm convertProfilToRealm(ProfilOuganext profil) {
    // fields can be treated the usual way on Profil
    Realm realm = serializeToSugoi(profil, Realm.class);
    UserStorage userStorage = new UserStorage();
    userStorage.setOrganizationSource(profil.getBrancheOrganisation());
    userStorage.setAddressSource(profil.getBrancheAdresse());
    userStorage.setUserSource(profil.getBrancheContact());
    realm.setUserStorages(List.of(userStorage));
    realm.setName(profil.getNomProfil());
    return realm;
  }
}
