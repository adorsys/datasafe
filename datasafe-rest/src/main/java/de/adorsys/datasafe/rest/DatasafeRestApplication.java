package de.adorsys.datasafe.rest;

import de.adorsys.datasafe.rest.config.DatasafeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DatasafeProperties.class)
public class DatasafeRestApplication {

	public static void main(String[] args) {
		System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
		SpringApplication.run(DatasafeRestApplication.class, args);
	}

}