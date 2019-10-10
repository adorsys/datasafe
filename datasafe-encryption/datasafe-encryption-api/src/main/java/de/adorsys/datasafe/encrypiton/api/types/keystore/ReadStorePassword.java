package de.adorsys.datasafe.encrypiton.api.types.keystore;

/**
 * Wrapper for keystore serialization/deserialization password as well as password for reading public keys.
 */
public class ReadStorePassword extends BaseTypePasswordString {

    @Deprecated
    public ReadStorePassword(String readStorePassword) {
        super(readStorePassword);
    }
}
