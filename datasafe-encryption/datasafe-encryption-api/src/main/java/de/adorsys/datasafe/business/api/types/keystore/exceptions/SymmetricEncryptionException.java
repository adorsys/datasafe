package de.adorsys.datasafe.business.api.types.keystore.exceptions;

import de.adorsys.common.exceptions.BaseException;

/**
 * Created by peter on 10.01.18 at 08:59.
 */
public class SymmetricEncryptionException extends BaseException {
    public SymmetricEncryptionException(String message) {
        super(message);
    }
}
