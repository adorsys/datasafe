package de.adorsys.docusafe2.keystore.impl;

import org.adorsys.cryptoutils.exceptions.BaseException;

/**
 * Created by peter on 16.01.18.
 */
public class InvalidBucketNameException extends BaseException {
    public InvalidBucketNameException(String message) {
        super(message);
    }
}
