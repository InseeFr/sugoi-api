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
package fr.insee.sugoi.app.service.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesLoaderService {

  public static String load(String name) {
    Properties props = new Properties();
    if (System.getProperty(name) != null) {
      return System.getProperty(name);
    } else {
      try {
        props.load(
            PropertiesLoaderService.class.getResourceAsStream("/application-default.properties"));
        String value = props.getProperty(name);

        // Property not present in application-default
        if (value != null) {
          return value;
        }

        props.load(PropertiesLoaderService.class.getResourceAsStream("/application.properties"));
        return props.getProperty(name);
      } catch (Exception e) {
        try {

          props.load(PropertiesLoaderService.class.getResourceAsStream("/application.properties"));
          return props.getProperty(name);

        } catch (IOException e1) {
          throw new RuntimeException("Properties " + name + " not found");
        }
      }
    }
  }

  public static Map<String, String> loadFile(String file) {
    Map<String, String> config = new HashMap<>();

    Properties props = new Properties();
    try {
      props.load(PropertiesLoaderService.class.getResourceAsStream(file));
      for (String propertyKey : new ArrayList<>(props.stringPropertyNames())) {
        config.put(propertyKey, props.getProperty(propertyKey));
      }
      return config;
    } catch (Exception e) {
      throw new RuntimeException("Properties file not found: " + file);
    }
  }
}
