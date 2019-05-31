package de.adorsys.datasafe.encrypiton.api.types.keystore;

import de.adorsys.datasafe.encrypiton.api.types.BaseTypeString;
import de.adorsys.datasafe.types.api.utils.Log;

/**
 * Wrapper for password sensitive data.
 */
public class BaseTypePasswordString extends BaseTypeString {

    public BaseTypePasswordString(String value) {
        super(value);
    }

    @Override
    public String toString() {
        return "BaseTypePasswordString{" + Log.secureSensitive(getValue()) + "}";
    }
}
