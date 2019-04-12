package de.adorsys.datasafe.business.api.deployment.keystore.exceptions;

public class KeyStoreConfigException extends KeyStoreExistsException {
    public KeyStoreConfigException(String message) {
        super(message);
    }
}
