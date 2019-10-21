package de.adorsys.datasafe.types.api.types;


import java.security.UnrecoverableKeyException;

public class BaseTypePasswordStringException extends UnrecoverableKeyException {
    public BaseTypePasswordStringException(String message) {
        super(message);
    }
}
