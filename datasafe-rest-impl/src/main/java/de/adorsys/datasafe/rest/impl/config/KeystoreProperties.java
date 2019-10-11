package de.adorsys.datasafe.rest.impl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keystore")
@Data
public class KeystoreProperties {
    private String type;
    private String encAlgorithm;
    private String prfAlgorithm;
    private String macAlgorithm;
}
