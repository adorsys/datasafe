package de.adorsys.datasafe.types.api.types;

import java.util.function.Supplier;

/**
 * Wrapper for password for reading secret or private key entry.
 * This class does not provide constructor with `raw` string as argument to reduce probability of user
 * password to appear in memory dump (the less copies of it exists in memory - the better).
 */
public class ReadKeyPassword extends BaseTypePasswordString {

    /**
     * Caller of method makes sure, supplied char[] is deleted asap
     * @param readKeyPassword will stay unchanged
     */
    public ReadKeyPassword(Supplier<char[]> readKeyPassword) {
        super(readKeyPassword);
    }

    /**
     * ATTENTION:
     * caller of method gives ownership of {@code readKeyPassword} to this class.
     * @code readKeyPassword} will be nullyfied after successful read/write/list.
     * @param readKeyPassword Password to read key that will be cleared after read/write/list.
     */
    public ReadKeyPassword(char[] readKeyPassword) {
        super(readKeyPassword);
    }
}
