package de.adorsys.config;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.MutableEncryptionConfig;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class Properties {
    private UserIDAuth userIDAuth;
    private boolean pathEncryptionEnabled;
    private String systemRoot;
    private String keystorePassword;
    private String readKeyPassword;
    private KeyCreationConfig keyCreationConfig = KeyCreationConfig.builder().signKeyNumber(1).encKeyNumber(0).build();
}
