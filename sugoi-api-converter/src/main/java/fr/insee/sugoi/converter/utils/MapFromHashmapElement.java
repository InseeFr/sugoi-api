package fr.insee.sugoi.converter.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MapFromHashmapElement {
    /**
     * Name of the method to add element to the hashmap
     * 
     * @return
     */
    String hashMapName();

    /**
     * Name of the attribute
     * 
     * @return
     */
    String hashMapKey();
}
