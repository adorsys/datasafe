package de.adorsys.docusafe2.business.impl.keystore;

import de.adorsys.common.exceptions.BaseException;

/**
 * Created by peter on 16.01.18.
 */
public class InvalidBucketNameException extends BaseException {
    public InvalidBucketNameException(String message) {
        super(message);
    }
}
