package de.adorsys.config;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import lombok.Data;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
public class Properties {
    private UserIDAuth userIDAuth;
    private boolean pathEncryptionEnabled;
    private String systemRoot = "file:///Users/thendo/Desktop/test";
    private String keystorePassword;
    private String readKeyPassword;
}
