package de.adorsys.datasafe.rest.impl.exceptions;

public class UserDoesNotExistsException extends RuntimeException {
    public UserDoesNotExistsException(String message) {
        super(message);
    }
}