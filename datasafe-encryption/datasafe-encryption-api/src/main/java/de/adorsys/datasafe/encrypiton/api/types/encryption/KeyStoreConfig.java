package de.adorsys.datasafe.encrypiton.api.types.encryption;

import de.adorsys.datasafe.encrypiton.api.types.keystore.pbkdf.PBKDF2;
import de.adorsys.datasafe.encrypiton.api.types.keystore.pbkdf.Scrypt;
import lombok.Builder;
import lombok.Getter;

/**
 * Wrapper for keystore config.
 */
@Getter
@Builder(toBuilder = true)
public class KeyStoreConfig {

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
     * Algorithm to use when encrypting password-like keys to be stored in keystore (i.e. storage credentials).
     */
    @Builder.Default
    private final String passwordKeysAlgo = "PBEWithHmacSHA256AndAES_256";

    /**
     * Password key derivation configuration.
     */
    @Getter
    public static class PBKDF {

        /**
         * This is non null if we should use PBKDF2 based routines.
         */
        private final PBKDF2 pbkdf2;

        /**
         * This is non null if we should use Scrypt-based routines.
         */
        private final Scrypt scrypt;

        @Builder
        public PBKDF(PBKDF2 pbkdf2, Scrypt scrypt) {
            if (null != pbkdf2 && null != scrypt) {
                throw new IllegalArgumentException("Ambiguous PBKDF - both scrypt and pbkdf2 are set");
            }

            if (null == pbkdf2 && null == scrypt) {
                pbkdf2 = PBKDF2.builder().build();
            }

            this.pbkdf2 = pbkdf2;
            this.scrypt = scrypt;
        }
    }
}
