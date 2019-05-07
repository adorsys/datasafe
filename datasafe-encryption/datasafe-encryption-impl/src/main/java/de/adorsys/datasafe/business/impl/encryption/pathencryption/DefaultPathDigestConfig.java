package de.adorsys.datasafe.business.impl.encryption.pathencryption;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DefaultPathDigestConfig {

    private final String messageDigest;
    private final String algorithm;
    private final int shaKeyPartSize;

    public DefaultPathDigestConfig() {
        this.messageDigest = "SHA-256";
        this.algorithm = "AES";
        this.shaKeyPartSize = 16;
    }
}
