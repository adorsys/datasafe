package de.adorsys.datasafe.business.impl.encryption.cmsencryption.exceptions;

/**
 * General decryption exception
 * 
 * @author fpo
 */
public class DecryptionException extends RuntimeException {
    public DecryptionException(String message) {
        super(message);
    }
}
