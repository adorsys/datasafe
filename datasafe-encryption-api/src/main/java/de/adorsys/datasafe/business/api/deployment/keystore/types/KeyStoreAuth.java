package de.adorsys.datasafe.business.api.deployment.keystore.types;

import de.adorsys.datasafe.business.api.deployment.keystore.exceptions.KeyStoreAuthException;

/**
 * Created by peter on 05.01.18.
 *
 * BTW, so liest man das Kennwort aus dem Handler
 * char[] password = PasswordCallbackUtils.getPassword(keyStoreAuth.getReadKeyHandler(), keyStorePassword);
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
