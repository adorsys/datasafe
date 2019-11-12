package de.adorsys.datasafe.types.api.types;

import java.util.function.Supplier;

/**
 * Wrapper for keystore serialization/deserialization password as well as password for reading public keys.
 */
public class ReadStorePassword extends BaseTypePasswordString {

    /**
     * Consider using supplier-based constructor instead.
     * This constructor exists because this kind of password has more relaxed security requirements compared
     * to {@link ReadKeyPassword}, as it does not provide access to key content.
     */
    public ReadStorePassword(String readStorePassword) {
        super(readStorePassword::toCharArray);
    }

    public ReadStorePassword(Supplier<char[]> readStorePassword) {
        super(readStorePassword);
    }
}
