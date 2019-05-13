package de.adorsys.datasafe.business.api.version.types.keystore.exceptions;

/**
 * Created by peter on 20.01.18 at 17:09.
 */
public class KeyStoreExistsException extends RuntimeException {
    public KeyStoreExistsException(String message) {
        super(message);
    }
}
