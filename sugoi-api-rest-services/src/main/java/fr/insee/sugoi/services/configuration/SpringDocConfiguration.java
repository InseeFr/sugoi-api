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
package fr.insee.sugoi.services.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfiguration {
  private static final Logger logger = LoggerFactory.getLogger(SpringDocConfiguration.class);

  @Value("${fr.insee.sugoi.springdoc.issuer.url.authorization:}")
  public String issuerAuthorizationURL;

  @Value("${fr.insee.sugoi.springdoc.issuer.url.refresh:}")
  public String issuerRefreshURL;

  @Value("${fr.insee.sugoi.springdoc.issuer.url.token:}")
  public String issuerTokenURL;

  @Value("${fr.insee.sugoi.springdoc.issuer.description:}")
  public String issuerDescription;

  @Value("${fr.insee.sugoi.springdoc.contact.name:}")
  public String contactName;

  @Value("${fr.insee.sugoi.springdoc.contact.email:}")
  public String contactEmail;

  public final String OAUTHSCHEME = "oAuthScheme";
  public final String SCHEMEBASIC = "basic";

  @Bean
  @ConditionalOnProperty(
      name = "fr.insee.sugoi.security.bearer-authentication-enabled",
      havingValue = "true",
      matchIfMissing = false)
  public OpenAPI customOpenAPIOIDC() {
    final OpenAPI openapi = createOpenAPI();
    openapi.components(
        new Components()
            .addSecuritySchemes(
                OAUTHSCHEME,
                new SecurityScheme()
                    .type(SecurityScheme.Type.OAUTH2)
                    .in(SecurityScheme.In.HEADER)
                    .description(issuerDescription)
                    .flows(
                        new OAuthFlows()
                            .authorizationCode(
                                new OAuthFlow()
                                    .authorizationUrl(issuerAuthorizationURL)
                                    .tokenUrl(issuerTokenURL)
                                    .refreshUrl(issuerRefreshURL)))));
    return openapi;
  }

  @Bean
  @ConditionalOnProperty(
      name = "fr.insee.sugoi.security.basic-authentication-enabled",
      havingValue = "true",
      matchIfMissing = false)
  public OpenAPI customOpenAPIBasic() {
    final OpenAPI openapi = createOpenAPI();
    openapi.components(
        new Components()
            .addSecuritySchemes(
                SCHEMEBASIC,
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme(SCHEMEBASIC)
                    .in(SecurityScheme.In.HEADER)
                    .description("Authentification Basic")
                    .name("basicAuth")));
    return openapi;
  }

  @ConditionalOnProperty(
      name = "fr.insee.sugoi.security.bearer-authentication-enabled",
      havingValue = "true",
      matchIfMissing = false)
  @Bean
  public OperationCustomizer addToken() {
    return (operation, handlerMethod) -> {
      if (handlerMethod.getMethod().getName().startsWith("/api/public/")) {
        return operation;
      }
      return operation.addSecurityItem(new SecurityRequirement().addList(OAUTHSCHEME));
    };
  }

  @ConditionalOnProperty(
      name = "fr.insee.sugoi.security.basic-authentication-enabled",
      havingValue = "true",
      matchIfMissing = false)
  @Bean
  public OperationCustomizer addBasic() {
    return (operation, handlerMethod) -> {
      if (handlerMethod.getMethod().getName().startsWith("/api/public/")) {
        return operation;
      }
      return operation.addSecurityItem(new SecurityRequirement().addList(SCHEMEBASIC));
    };
  }

  @Bean
  @ConditionalOnMissingBean
  public OpenAPI simpleOpenAPI() {
    logger.info("surcharge de la configuration swagger");
    final OpenAPI openapi = createOpenAPI();
    return openapi;
  }

  private OpenAPI createOpenAPI() {
    logger.info("surcharge de la configuration swagger");
    Contact contact = new Contact().url("https://github.com/InseeFrLab/sugoi-api");
    if (true) {
      contact = contact.email(contactEmail).name(contactEmail);
    }
    final OpenAPI openapi =
        new OpenAPI()
            .info(
                new Info()
                    .title("Swagger SUGOI")
                    .description("API de sugoi")
                    .license(
                        new License()
                            .name("Apache 2.0")
                            .url("http://www.apache.org/licenses/LICENSE-2.0.html"))
                    .contact(contact));

    return openapi;
  }
}
