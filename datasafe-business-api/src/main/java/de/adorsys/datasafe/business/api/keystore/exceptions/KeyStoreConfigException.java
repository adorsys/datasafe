package de.adorsys.datasafe.business.api.keystore.exceptions;

public class KeyStoreConfigException extends KeyStoreExistsException {
    public KeyStoreConfigException(String message) {
        super(message);
    }
}
