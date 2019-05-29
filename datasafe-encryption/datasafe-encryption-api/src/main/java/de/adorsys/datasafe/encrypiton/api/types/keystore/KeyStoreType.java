package de.adorsys.datasafe.encrypiton.api.types.keystore;

import de.adorsys.datasafe.encrypiton.api.types.BaseTypeString;

/**
 * Wrapper for keystore type - i.e. PKCS12,JKS,UBER.
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
