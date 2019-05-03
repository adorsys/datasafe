package de.adorsys.datasafe.business.api.types.keystore.exceptions;

/**
 * Created by peter on 10.01.18 at 08:59.
 */
public class SymmetricEncryptionException extends RuntimeException {
    public SymmetricEncryptionException(String message) {
        super(message);
    }
}
