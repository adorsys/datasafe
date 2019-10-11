package de.adorsys.datasafe.types.api.types;

/**
 * Wrapper for keystore serialization/deserialization password as well as password for reading public keys.
 */
public class ReadStorePassword extends BaseTypePasswordString {

    public ReadStorePassword(String readStorePassword) {
        super(readStorePassword);
    }
}
