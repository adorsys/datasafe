package de.adorsys.datasafe.business.api.version.types.keystore;

import de.adorsys.datasafe.business.api.version.types.BaseTypeString;

public class BaseTypePasswordString extends BaseTypeString {
    public BaseTypePasswordString(String value) {
        super(value);
    }

    public String toString() {
        return this.getClass().getSimpleName() + "{'" + hide(this.getValue()) + "'}";
    }

    private static String hide(String value) {
        return value.length() > 4 ? value.substring(0, 2) + "***" + value.substring(value.length() - 2) : "***";
    }
}
