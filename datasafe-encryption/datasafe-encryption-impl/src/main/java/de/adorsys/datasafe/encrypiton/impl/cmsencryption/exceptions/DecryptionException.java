package de.adorsys.datasafe.encrypiton.impl.cmsencryption.exceptions;

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
