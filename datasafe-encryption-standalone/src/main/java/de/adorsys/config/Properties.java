package de.adorsys.config;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import lombok.Data;


@Data
public class Properties {
    private UserIDAuth userIDAuth;
    private boolean pathEncryptionEnabled;
    private String systemRoot = "file:///Users/thendo/Desktop/test";
    private String keystorePassword;
    private String readKeyPassword;
}
