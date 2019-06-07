package de.adorsys.datasafe.rest.impl.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties
@PropertySource("classpath:jwt-config.properties")
@Data
public class SecurityProperties {

    private String jwtSecret;
    private String defaultUser;
    private String defaultPassword;
    private long tokenExpiration;
}
