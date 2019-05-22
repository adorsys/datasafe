package de.adorsys.datasafe.rest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "datasafe")
@Data
public class DatasafeProperties {

    private String bucketName;
    private String systemRoot;
    private String keystorePassword;
}
