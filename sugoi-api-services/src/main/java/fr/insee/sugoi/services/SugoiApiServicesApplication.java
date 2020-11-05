package fr.insee.sugoi.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = { "fr.insee.sugoi" })
public class SugoiApiServicesApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(SugoiApiServicesApplication.class, args);
	}

}
