package de.adorsys.datasafe.business.impl.cmsencryption.exceptions;

import de.adorsys.common.exceptions.BaseException;

/**
 * General decryption exception
 * 
 * @author fpo
 */
public class DecryptionException extends BaseException {
    public DecryptionException(String message) {
        super(message);
    }
}
