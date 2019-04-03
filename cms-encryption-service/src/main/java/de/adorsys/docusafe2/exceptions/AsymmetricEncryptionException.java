package de.adorsys.docusafe2.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

/**
 * Created by peter on 10.01.18 at 09:00.
 */
public class AsymmetricEncryptionException extends BaseException {
    public AsymmetricEncryptionException(String message) {
        super(message);
    }
}
