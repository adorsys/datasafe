package de.adorsys.config;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.encryption.CmsEncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.MutableEncryptionConfig;
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
    private MutableEncryptionConfig encryption = new MutableEncryptionConfig();
    private KeyCreationConfig keyCreationConfigEC = KeyCreationConfig.builder().signKeyNumber(1).encKeyNumber(0).build();
    private CmsEncryptionConfig cmsEncryptionConfig = CmsEncryptionConfig.builder().algo("AES256_GCM").build();
//    private KeyCreationConfig keyCreationConfigRSA = = KeyCreationConfig.builder().signing(KeyCreationConfig.SigningKeyCreationCfg.builder()
//    .algo("RSA").size(2048).sigAlgo( "SHA256withRSA").curve(null).build()).encrypting(KeyCreationConfig.EncryptingKeyCreationCfg.builder().algo("RSA").size(2048).sigAlgo("SHA256withRSA").curve(null).build()).build();
}
