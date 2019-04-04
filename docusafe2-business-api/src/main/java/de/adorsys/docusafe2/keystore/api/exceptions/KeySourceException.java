package de.adorsys.docusafe2.keystore.api.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

/**
 * Created by peter on 10.01.18 at 08:32.
 */
public class KeySourceException extends BaseException {
    public KeySourceException(String message) {
        super(message);
    }
}
