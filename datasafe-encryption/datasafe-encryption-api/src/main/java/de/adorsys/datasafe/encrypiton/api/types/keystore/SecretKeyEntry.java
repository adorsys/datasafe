package de.adorsys.datasafe.encrypiton.api.types.keystore;

import javax.crypto.SecretKey;

/**
 * Wrapper for secret key entry within keystore.
 */
public interface SecretKeyEntry extends KeyEntry {

    /**
     * Secret key value.
     */
    SecretKey getSecretKey();

    /**
     * Algorithm associated with it.
     */
    String getKeyAlgo();
}
