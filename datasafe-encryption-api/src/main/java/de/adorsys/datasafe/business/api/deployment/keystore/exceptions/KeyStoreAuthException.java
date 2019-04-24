package de.adorsys.datasafe.business.api.deployment.keystore.exceptions;

import de.adorsys.common.exceptions.BaseException;

/**
 * Created by peter on 10.01.18 at 08:43.
 */
public class KeyStoreAuthException extends BaseException {
    public KeyStoreAuthException(String message) {
        super(message);
    }
}
