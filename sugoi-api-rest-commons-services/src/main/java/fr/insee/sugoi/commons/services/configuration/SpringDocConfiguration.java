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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityScheme;

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

  public final String OAUTHSCHEME = "oAuth";
  public final String SCHEMEBASIC = "basic";

  @Bean
  public OpenAPI customOpenAPIBasicAndOIDC() {
    final OpenAPI openapi = createOpenAPI();
    openapi.components(new Components()
        .addSecuritySchemes(SCHEMEBASIC,
            new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme(SCHEMEBASIC).in(SecurityScheme.In.HEADER)
                .description("Authentification Basic"))
        .addSecuritySchemes(OAUTHSCHEME,
            new SecurityScheme().type(SecurityScheme.Type.OAUTH2).in(SecurityScheme.In.HEADER)
                .description(issuerDescription).flows(new OAuthFlows().authorizationCode(new OAuthFlow()
                    .authorizationUrl(issuerAuthorizationURL).tokenUrl(issuerTokenURL).refreshUrl(issuerRefreshURL)))));
    return openapi;
  }

  private OpenAPI createOpenAPI() {
    logger.info("surcharge de la configuration swagger");
    Contact contact = new Contact().url("https://github.com/InseeFrLab/sugoi-api");
    if (contactEmail != null) {
      contact = contact.email(contactEmail).name(contactEmail);
    }
    final OpenAPI openapi = new OpenAPI().info(new Info().title("Swagger SUGOI").description("API de sugoi")
        .license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0.html"))
        .contact(contact));

    return openapi;
  }
}
