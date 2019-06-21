package de.adorsys.datasafe.rest.impl.exceptions;

public class UserDoesNotExists  extends RuntimeException {
    public UserDoesNotExists(String message) {
        super(message);
    }
}