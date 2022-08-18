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
package fr.insee.sugoi.model;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface SugoiObject extends Serializable {
  default Optional<Object> get(String fieldName) throws NoSuchFieldException {
    try {
      String[] splittedAttributeName = fieldName.split("\\.");
      if (splittedAttributeName.length > 1) {
        Method getterMethod = getGetterMethod(splittedAttributeName[0]);
        Object value = getterMethod.invoke(this);
        if (value instanceof Map) {
          if (((Map<?, ?>) value).containsKey(splittedAttributeName[1])) {
            return Optional.ofNullable(((Map<?, ?>) value).get(splittedAttributeName[1]));
          } else return Optional.empty();
        } else
          throw new ClassCastException(
              String.format(
                  "Field %s is not a map, fieldname %s should not contain . ",
                  splittedAttributeName[0], fieldName));
      } else return Optional.ofNullable(getGetterMethod(fieldName).invoke(this));
    } catch (InvocationTargetException | IllegalAccessException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  default void set(String fieldName, Object fieldValue)
      throws NoSuchFieldException, IllegalAccessException {
    try {
      String[] splittedAttributeName = fieldName.split("\\.");
      if (splittedAttributeName.length > 1) {
        Method setterMethod = getSetterMethod(splittedAttributeName[0]);
        Method getterMethod = getGetterMethod(splittedAttributeName[0]);
        var currentMap = getterMethod.invoke(this);
        if (currentMap instanceof Map) {
          HashMap<Object, Object> nullableTypeCheckedMap = new HashMap<>((Map<?, ?>) currentMap);
          nullableTypeCheckedMap.put(splittedAttributeName[1], fieldValue);
          setterMethod.invoke(this, nullableTypeCheckedMap);
        } else
          throw new ClassCastException(
              String.format(
                  "field %s is not a map, fieldname %s should not contain . ",
                  splittedAttributeName[0], fieldName));
      } else getSetterMethod(fieldName).invoke(this, fieldValue);
    } catch (InvocationTargetException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  private Method getGetterMethod(String fieldName) throws NoSuchFieldException {
    return Arrays.stream(this.getClass().getMethods())
        .filter(method -> method.getName().equalsIgnoreCase("get" + fieldName))
        .findFirst()
        .orElseThrow(
            () ->
                new NoSuchFieldException(
                    String.format(
                        "getter for %s not found in object %s",
                        fieldName, this.getClass().getSimpleName())));
  }

  private Method getSetterMethod(String fieldName) throws NoSuchFieldException {
    return Arrays.stream(this.getClass().getMethods())
        .filter(method -> method.getName().equalsIgnoreCase("set" + fieldName))
        .findFirst()
        .orElseThrow(
            () ->
                new NoSuchFieldException(
                    String.format(
                        "setter for %s not found in object %s",
                        fieldName, this.getClass().getSimpleName())));
  }
}
