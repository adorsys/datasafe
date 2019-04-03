package de.adorsys.docusafe2.keystore.api.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

/**
 * Created by peter on 20.01.18 at 17:09.
 */
public class KeyStoreExistsException extends BaseException {
    public KeyStoreExistsException(String message) {
        super(message);
    }
}
