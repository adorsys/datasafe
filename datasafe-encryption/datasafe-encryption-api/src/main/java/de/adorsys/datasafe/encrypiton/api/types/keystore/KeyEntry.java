package de.adorsys.datasafe.encrypiton.api.types.keystore;

import de.adorsys.datasafe.types.api.types.ReadKeyPassword;

/**
 * Wrapper for key entry within keystore.
 */
public interface KeyEntry {

    /**
     * Password to read key from keystore
     */
    ReadKeyPassword getReadKeyPassword();

    /**
     * Key alias
     */
    String getAlias();
}
