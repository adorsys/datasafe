package de.adorsys.datasafe.simple.adapter.spring.properties;

import de.adorsys.datasafe.encrypiton.api.types.encryption.MutableEncryptionConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "datasafe")
public class SpringDatasafeEncryptionProperties {

    private MutableEncryptionConfig encryption = new MutableEncryptionConfig();
}
