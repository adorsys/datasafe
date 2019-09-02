package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import lombok.Data;

/**
 * Configures document path encryption digest.
 */
@Data
public class DatevPathDigestConfig extends DefaultPathDigestConfig {

    public DatevPathDigestConfig() {
        this.algorithm = "AES";
    }
}
