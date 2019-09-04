package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import lombok.Data;

/**
 * Configures document path encryption digest.
 */
@Data
public class SimplePathDigestConfig extends DefaultPathDigestConfig {

    public SimplePathDigestConfig() {
        this.algorithm = "AES";
    }
}
