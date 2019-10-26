package de.adorsys.datasafe.encrypiton.api.types.keystore;

import de.adorsys.datasafe.encrypiton.api.types.keystore.exceptions.KeyStoreAuthException;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;

/**
 * Authorization entity to read keystore or both keystore and key in it.
 */
public class KeyStoreAuth {
    private final ReadStorePassword readStorePassword;
    private final ReadKeyPassword readKeyPassword;

    public KeyStoreAuth(ReadStorePassword readStorePassword, ReadKeyPassword readKeyPassword) {
        this.readStorePassword = readStorePassword;
        this.readKeyPassword = readKeyPassword;
    }

    public ReadStorePassword getReadStorePassword() {
        if (readStorePassword == null) {
            throw new KeyStoreAuthException("Access to READ STORE PASSWORD not allowed.");
        }
        return readStorePassword;
    }

    public ReadKeyPassword getReadKeyPassword() {
        if (readKeyPassword == null) {
            throw new KeyStoreAuthException("Access to READ KEY PASSWORD not allowed");
        }
        return readKeyPassword;
    }

    @Override
    public String toString() {
        return "KeyStoreAuth{" +
                readStorePassword +
                ", " + readKeyPassword +
                '}';
    }
}
