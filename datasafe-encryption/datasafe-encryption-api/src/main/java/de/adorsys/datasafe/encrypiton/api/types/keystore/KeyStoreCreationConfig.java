package de.adorsys.datasafe.encrypiton.api.types.keystore;

import lombok.Builder;
import lombok.Getter;
import org.bouncycastle.crypto.util.PBKDF2Config;
import org.bouncycastle.jcajce.BCFKSLoadStoreParameter;

import static org.bouncycastle.crypto.util.PBKDF2Config.PRF_SHA512;

/**
 * Wrapper for keystore config.
 */
@Builder(toBuilder = true)
@Getter
public class KeyStoreCreationConfig {
    public static KeyStoreCreationConfig DEFAULT = getDefaultConfig();

    private BCFKSLoadStoreParameter.EncryptionAlgorithm storeEncryptionAlgorithm;
    private PBKDF2Config storePBKDFConfig;
    private BCFKSLoadStoreParameter.MacAlgorithm storeMacAlgorithm;

    private static KeyStoreCreationConfig getDefaultConfig() {
        return KeyStoreCreationConfig.builder()
        .storeEncryptionAlgorithm(BCFKSLoadStoreParameter.EncryptionAlgorithm.AES256_KWP)
        .storePBKDFConfig(new PBKDF2Config.Builder().withPRF(PRF_SHA512).build())
        .storeMacAlgorithm(BCFKSLoadStoreParameter.MacAlgorithm.HmacSHA3_512)
        .build();
    }
}
