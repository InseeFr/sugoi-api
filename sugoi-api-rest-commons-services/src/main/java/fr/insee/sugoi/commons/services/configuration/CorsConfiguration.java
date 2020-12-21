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
package fr.insee.sugoi.commons.services.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConfigurationProperties("fr.insee.sugoi.cors")
public class CorsConfiguration {

  /** Pattern of urls accepted by CORS protection */
  private String PATH_PATTERN = "/**";

  /** Array of origins allowed to connect */
  private String[] allowedOrigins = {};

  private String[] allowedMethods = {};

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping(PATH_PATTERN)
            .allowedOrigins(allowedOrigins)
            .allowedMethods(allowedMethods);
      }
    };
  }

  public String[] getAllowedOrigins() {
    return allowedOrigins;
  }

  public void setAllowedOrigins(String[] allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
  }

  public String[] getAllowedMethods() {
    return allowedMethods;
  }

  public void setAllowedMethods(String[] allowedMethods) {
    this.allowedMethods = allowedMethods;
  }
}
