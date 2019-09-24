package de.adorsys.datasafe.simple.adapter.impl.legacy.pathencryption;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Configures document path encryption digest.
 */
@Data
@AllArgsConstructor
public class LegacyPathDigestConfig {

    private final String messageDigest;
    private final String algorithm;
    private final int shaKeyPartSize;

    public LegacyPathDigestConfig() {
        this.messageDigest = "SHA-256";
        this.algorithm = "AES";
        this.shaKeyPartSize = 16;
    }
}
