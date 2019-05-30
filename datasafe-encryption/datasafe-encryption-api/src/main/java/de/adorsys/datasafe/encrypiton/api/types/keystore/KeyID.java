package de.adorsys.datasafe.encrypiton.api.types.keystore;

import de.adorsys.datasafe.encrypiton.api.types.BaseTypeString;

/**
 * Wrapper that identifies key inside keystore.
 */
public class KeyID extends BaseTypeString {

    public KeyID(String value) {
        super(value);
    }
}
