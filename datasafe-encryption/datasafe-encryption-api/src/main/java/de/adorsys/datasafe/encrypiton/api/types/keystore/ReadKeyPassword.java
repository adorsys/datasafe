package de.adorsys.datasafe.encrypiton.api.types.keystore;

/**
 * Wrapper for password for reading secret or private key entry.
 */
public class ReadKeyPassword extends BaseTypePasswordString {

    public ReadKeyPassword(String readKeyPassword) {
        super(readKeyPassword);
    }
}
