package de.adorsys.datasafe.encrypiton.api.types.keystore;

import lombok.extern.slf4j.Slf4j;

/**
 * Wrapper for password for reading secret or private key entry.
 */
@Slf4j
public class ReadKeyPassword extends BaseTypePasswordString {

    public ReadKeyPassword(String readKeyPassword) {
        super(readKeyPassword);
    }

    public void clear() {
        log.info("CLEAR READ KEY PASSWORD");
    }
}
