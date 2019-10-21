package de.adorsys.datasafe.types.api.types;

import java.util.function.Supplier;

/**
 * Wrapper for keystore serialization/deserialization password as well as password for reading public keys.
 */
public class ReadStorePassword extends BaseTypePasswordString {

    public ReadStorePassword(String readStorePassword) {
        super(new Supplier<char[]>() {
            @Override
            public char[] get() {
                return readStorePassword.toCharArray();
            }
        });
    }
}
