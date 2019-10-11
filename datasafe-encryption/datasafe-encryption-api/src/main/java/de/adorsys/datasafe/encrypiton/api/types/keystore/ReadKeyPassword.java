package de.adorsys.datasafe.encrypiton.api.types.keystore;

import java.util.function.Supplier;

/**
 * Wrapper for password for reading secret or private key entry.
 */
public class ReadKeyPassword extends BaseTypePasswordString {

    /**
     * caller of method makes sure, supplied char[] is deleted asap
     * @param readKeyPassword will stay unchanged
     */
    public ReadKeyPassword(Supplier<char[]> readKeyPassword) {
        super(readKeyPassword);
    }

    /**
     * ATTENTION
     *
     * caller of method gives responsiblity of char[]
     * to this class. char[] will be nullyfied
     * asap (after successfull read/write/list)
     * @param readKeyPassword will be nullified asap
     */
    public ReadKeyPassword(char[] readKeyPassword) {
        super(readKeyPassword);
    }

    public static ReadKeyPassword getForString(String a) {
        return new ReadKeyPassword(new Supplier<char[]>() {
            @Override
            public char[] get() {
                return a.toCharArray();
            }
        });
    }
}
