package de.adorsys.datasafe.encrypiton.impl.keystore;

/**
 * Wrapper for keystore config.
 */
public class KeyStoreCreationConfig {
    public static KeyStoreCreationConfig DEFAULT = getDefaultConfig();

    private String keyStoreType;
    private String storeEncryptionAlgorithm;
    private String storePBKDFConfig;
    private String storeMacAlgorithm;

    public KeyStoreCreationConfig(String keyStoreType, String storeEncryptionAlgorithm,
                                  String storePBKDFConfig, String storeMacAlgorithm) {
        this.keyStoreType = keyStoreType;
        this.storeEncryptionAlgorithm = storeEncryptionAlgorithm;
        this.storePBKDFConfig = storePBKDFConfig;
        this.storeMacAlgorithm = storeMacAlgorithm;
    }

    private static KeyStoreCreationConfig getDefaultConfig() {
        return new KeyStoreCreationConfig("BCFKS", "AES256_KWP", "PRF_SHA512", "HmacSHA3_512");
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public String getStoreEncryptionAlgorithm() {
        return storeEncryptionAlgorithm;
    }

    public String getStorePBKDFConfig() {
        return storePBKDFConfig;
    }

    public String getStoreMacAlgorithm() {
        return storeMacAlgorithm;
    }
}
