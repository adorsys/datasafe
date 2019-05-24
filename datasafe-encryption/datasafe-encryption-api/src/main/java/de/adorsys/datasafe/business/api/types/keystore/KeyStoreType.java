package de.adorsys.datasafe.business.api.types.keystore;

import de.adorsys.datasafe.business.api.types.BaseTypeString;

/**
 * Created by peter on 29.12.2017 at 14:11:52.
 */
public class KeyStoreType extends BaseTypeString {

    public static KeyStoreType DEFAULT = getDefaultKeyStoreType();

    public KeyStoreType(String value) {
        super(value);
    }

    protected static KeyStoreType getDefaultKeyStoreType() {
        String serverKeystoreType = System.getProperty("SERVER_KEYSTORE_TYPE");
        if (null != serverKeystoreType && !serverKeystoreType.isEmpty()) {
            return new KeyStoreType(serverKeystoreType);
        }
        return new KeyStoreType("UBER");
    }
}
