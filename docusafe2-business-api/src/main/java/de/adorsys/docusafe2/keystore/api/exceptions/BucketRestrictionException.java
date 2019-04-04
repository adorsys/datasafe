package de.adorsys.docusafe2.keystore.api.exceptions;

import org.adorsys.cryptoutils.exceptions.BaseException;

/**
 * Created by peter on 20.03.18 at 08:02.
 */
public class BucketRestrictionException extends BaseException {
    public BucketRestrictionException(String message) {
        super(message);
    }
}
