package fr.insee.sugoi.sugoiapicore.mapper.properties.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MapToAttribute {

    /**
     * Nom de l'attributLdap ce field.
     */
    String value();
}
