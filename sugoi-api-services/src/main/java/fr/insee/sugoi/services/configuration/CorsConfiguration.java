package fr.insee.sugoi.services.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConfigurationProperties("fr.insee.sugoi.cors")
public class CorsConfiguration {

    /**
     * Pattern of urls accepted by CORS protection
     */
    private String PATH_PATTERN = "/**";

    /**
     * Array of origins allowed to connect
     */
    private String[] allowedOrigins = {};

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping(PATH_PATTERN).allowedOrigins(allowedOrigins);
            }
        };
    }

    public String[] getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

}
