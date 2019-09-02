package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Configures document path encryption digest.
 * By default using AES in GCM mode with SIV(synthetic input vector)
 */
@Data
@AllArgsConstructor
public class DefaultPathDigestConfig {

    String messageDigest;
    String algorithm;
    int shaKeyPartSize;

    public DefaultPathDigestConfig() {
        this.messageDigest = "SHA-256";
        this.algorithm = "AES-GCM-SIV";
        this.shaKeyPartSize = 16;
    }
}
