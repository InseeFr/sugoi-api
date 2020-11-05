package fr.insee.sugoi.services.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.insee.sugoi.converter.mapper.OuganextSugoiMapper;

@Configuration
public class CustomObjectMapper {

    @Bean
    public OuganextSugoiMapper ouganextSugoiMapper() {
        OuganextSugoiMapper ouganextSugoiMapper = new OuganextSugoiMapper();
        return ouganextSugoiMapper;
    }

}
