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
package fr.insee.sugoi.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = {"fr.insee.sugoi"})
public class SugoiApiServicesApplication extends SpringBootServletInitializer {

  public static final Logger log = LoggerFactory.getLogger(SugoiApiServicesApplication.class);

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    String propertiesPath = System.getProperty("properties.path");
    if (propertiesPath != null) {
      if (!propertiesPath.endsWith("/")) {
        propertiesPath += "/";
      }
    } else {
      propertiesPath = "./";
    }
    log.info("Loading configuration from {}{}", propertiesPath, "sugoi.properties");
    return application
        .properties(
            "spring.config.location=classpath:/,file:" + propertiesPath + "sugoi.properties")
        .sources(SugoiApiServicesApplication.class);
  }

  public static void main(String[] args) {
    SpringApplication.run(SugoiApiServicesApplication.class, args);
  }
}
