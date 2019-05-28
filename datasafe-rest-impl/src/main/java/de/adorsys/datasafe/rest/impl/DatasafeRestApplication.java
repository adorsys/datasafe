package de.adorsys.datasafe.rest.impl;

import de.adorsys.datasafe.rest.impl.config.DatasafeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DatasafeProperties.class)
public class DatasafeRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatasafeRestApplication.class, args);
	}

}
