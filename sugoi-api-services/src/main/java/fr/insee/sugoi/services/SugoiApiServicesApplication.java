package fr.insee.sugoi.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = { "fr.insee.sugoi" })
public class SugoiApiServicesApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application
				.properties("spring.config.location=classpath:/,file:${catalina.base}/webapps/" + "sugoi.properties")
				.sources(SugoiApiServicesApplication.class);
	}

	public static void main(String[] args) {

		SpringApplication.run(SugoiApiServicesApplication.class, args);

	}

}
