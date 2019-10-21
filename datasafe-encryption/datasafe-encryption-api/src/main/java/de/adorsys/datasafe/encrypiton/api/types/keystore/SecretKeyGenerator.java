package de.adorsys.datasafe.encrypiton.api.types.keystore;


import de.adorsys.datasafe.types.api.types.ReadKeyPassword;

/**
 * Generates random secret key entry.
 */
public interface SecretKeyGenerator {

    /**
     * Create random secret key.
     * @param alias Secret key alias.
     * @param readKeyPassword Password to read this key
     * @return Generated secret key
     */
    SecretKeyEntry generate(String alias, ReadKeyPassword readKeyPassword);
}
