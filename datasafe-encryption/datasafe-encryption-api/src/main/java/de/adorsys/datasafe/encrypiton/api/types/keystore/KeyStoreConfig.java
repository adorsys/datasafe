package de.adorsys.datasafe.encrypiton.api.types.keystore;

import de.adorsys.datasafe.encrypiton.api.types.keystore.pbkdf.PBKDF2;
import de.adorsys.datasafe.encrypiton.api.types.keystore.pbkdf.Scrypt;
import lombok.Builder;
import lombok.Getter;

/**
 * Wrapper for keystore config.
 */
@Getter
@Builder
public class KeyStoreConfig {

    public static final KeyStoreConfig DEFAULT = KeyStoreConfig.builder().build();

    /**
     * Which type of keystore to use i.e. BCFKS or UBER, etc.
     */
    @Builder.Default
    private final String type = "BCFKS";

    /**
     * Keystore encryption algorithm (used both for keys and keystore). For BCFKS refer to for BCFKS refer to
     * {@see org.bouncycastle.jcajce.BCFKSLoadStoreParameter}
     */
    @Builder.Default
    private final String encryptionAlgo = "AES256_KWP";

    /**
     * Password key derivation configuration.
     */
    @Builder.Default
    private final PBKDF pbkdf = PBKDF.builder().build();

    /**
     * KeyStore authentication algorithm, for BCFKS refer to {@see org.bouncycastle.jcajce.BCFKSLoadStoreParameter}
     */
    @Builder.Default
    private final String macAlgo = "HmacSHA3_512";

    /**
     * Password key derivation configuration.
     */
    @Getter
    @Builder
    public static class PBKDF {

        /**
         * This is non null if we should use PBKDF2 based routines.
         */
        @Builder.Default
        private final PBKDF2 pbkdf2 = PBKDF2.builder().build();

        /**
         * This is non null if we should use Scrypt-based routines.
         */
        private final Scrypt scrypt;
    }
}
