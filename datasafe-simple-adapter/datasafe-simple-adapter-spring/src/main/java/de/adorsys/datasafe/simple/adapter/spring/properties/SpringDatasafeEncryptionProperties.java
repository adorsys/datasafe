package de.adorsys.datasafe.simple.adapter.spring.properties;

import de.adorsys.datasafe.encrypiton.api.types.encryption.MutableEncryptionConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;

@Data
@ConfigurationProperties(prefix = "datasafe")
public class SpringDatasafeEncryptionProperties {

    private MutableEncryptionConfig encryption = new MutableEncryptionConfig();
    @Nullable
    private Boolean pathEncryption = true;
}
