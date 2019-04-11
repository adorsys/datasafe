package de.adorsys.datasafe.business.impl.dfscredentialservice;

import de.adorsys.common.exceptions.BaseException;

public class DFSCredentialException extends BaseException {

    public DFSCredentialException(String message) {
        super(message);
    }

    public DFSCredentialException(String message, Throwable cause) {
        super(message, cause);
    }
}
