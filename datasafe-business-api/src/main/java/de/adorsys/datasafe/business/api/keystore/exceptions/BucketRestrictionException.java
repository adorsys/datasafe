package de.adorsys.datasafe.business.api.keystore.exceptions;

import de.adorsys.common.exceptions.BaseException;

/**
 * Created by peter on 20.03.18 at 08:02.
 */
public class BucketRestrictionException extends BaseException {
    public BucketRestrictionException(String message) {
        super(message);
    }
}
