package fr.insee.sugoi.sugoiapicore.mapper;

import java.lang.reflect.Field;
import java.util.Map;

import com.unboundid.ldap.sdk.SearchResultEntry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.sugoi.sugoiapicore.mapper.properties.utils.AttributeLdapName;
import fr.insee.sugoi.sugoiapicore.mapper.properties.utils.MapToAttribute;
import fr.insee.sugoi.sugoiapicore.mapper.properties.utils.MapToMapElement;

public class GenericLdapMapper {

    private static final Logger logger = LogManager.getLogger(GenericLdapMapper.class);

    @SuppressWarnings("unchecked")
    public static <O, N> N transform(SearchResultEntry result, Class<O> propertiesClazz, Class<N> clazz) {
        try {
            N object = clazz.getDeclaredConstructor().newInstance();
            for (Field field : propertiesClazz.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    if (field.getDeclaredAnnotationsByType(AttributeLdapName.class).length > 0) {

                        if (field.getDeclaredAnnotationsByType(MapToAttribute.class).length > 0) {
                            Field userField = object.getClass()
                                    .getDeclaredField(field.getAnnotation(MapToAttribute.class).value());
                            userField.setAccessible(true);
                            userField.set(object,
                                    result.getAttributeValue(field.getAnnotation(AttributeLdapName.class).value()));
                        }

                        if (field.getDeclaredAnnotationsByType(MapToMapElement.class).length > 0) {
                            Field userField = object.getClass()
                                    .getDeclaredField(field.getAnnotation(MapToMapElement.class).name());
                            userField.setAccessible(true);
                            Map<String, Object> userFieldObject = (Map<String, Object>) userField.get(object);
                            userFieldObject.put(field.getAnnotation(MapToMapElement.class).key(),
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
