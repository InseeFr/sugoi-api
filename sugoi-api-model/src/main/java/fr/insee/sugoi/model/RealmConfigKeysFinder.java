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

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import org.reflections.Reflections;

public class RealmConfigKeysFinder {

  private Set<Class<? extends RealmConfigKeys>> enumClasses;

  private Set<Class<? extends RealmConfigKeys>> getFoundRealmConfigKeyClasses() {
    if (enumClasses == null) {
      Reflections reflections = new Reflections("fr.insee.sugoi");
      enumClasses = reflections.getSubTypesOf(RealmConfigKeys.class);
    }
    return enumClasses;
  }

  public RealmConfigKeys getRealmConfigKey(String key) {
    RealmConfigKeys enumKey = null;
    for (Class<? extends RealmConfigKeys> enumClass : getFoundRealmConfigKeyClasses()) {
      try {
        enumKey =
            (RealmConfigKeys)
                enumClass.getMethod("getRealmConfigKey", String.class).invoke(null, key);
      } catch (IllegalAccessException
          | IllegalArgumentException
          | InvocationTargetException
          | NoSuchMethodException
          | SecurityException e) {
        // error in one class should be ignored
      }
      if (enumKey != null) {
        return enumKey;
      }
    }
    return null;
  }
}
