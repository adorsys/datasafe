package de.adorsys.datasafe.rest.impl;

import de.adorsys.datasafe.rest.impl.config.DatasafeProperties;
import de.adorsys.datasafe.rest.impl.security.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({DatasafeProperties.class, SecurityProperties.class})
public class DatasafeRestApplication {

	public static void main(String[] args) {
		System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
		SpringApplication.run(DatasafeRestApplication.class, args);
	}

}
