package de.adorsys.datasafe.rest.impl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "keystore")
@Data
public class KeystoreProperties {

    /**
     * Which type of keystore to use i.e. BCFKS or UBER, etc.
     */
    @NotBlank
    private String type = "BCFKS";

    /**
     * Keystore encryption algorithm (used both for keys and keystore). For BCFKS refer to for BCFKS refer to
     * {@link org.bouncycastle.jcajce.BCFKSLoadStoreParameter}
     */
    @NotBlank
    private String encryptionAlgo = "AES256_KWP";

    /**
     * Password key derivation configuration.
     */
    @NotNull
    private PBKDF pbkdf;

    /**
     * KeyStore authentication algorithm, for BCFKS refer to {@link org.bouncycastle.jcajce.BCFKSLoadStoreParameter}
     */
    @NotBlank
    private String macAlgo = "HmacSHA3_512";

    /**
     * Password key derivation configuration.
     */
    @Data
    public static class PBKDF {

        /**
         * This is non null if we should use PBKDF2 based routines.
         */
        private PBKDF2 pbkdf2;

        /**
         * This is non null if we should use Scrypt-based routines.
         */
        private Scrypt scrypt;
    }

    @Data
    public class PBKDF2 {

        /**
         * Password derivation algorithm, for BCFKS refer to {@link org.bouncycastle.crypto.util.PBKDF2Config}
         */
        @NotNull
        private String algo = "PRF_SHA512";

        /**
         * Password derivation salt length, for BCFKS refer to {@link org.bouncycastle.crypto.util.PBKDF2Config}
         */
        @Min(-1)
        private int saltLength = 32;

        /**
         * Password derivation iteration count, for BCFKS refer to {@link org.bouncycastle.crypto.util.PBKDF2Config}
         */
        @Min(1024)
        private int iterCount = 20480;
    }

    @Data
    public class Scrypt {

        /**
         * Password derivation cost, for BCFKS refer to {@link org.bouncycastle.crypto.util.ScryptConfig}
         */
        @Min(1)
        private int cost;
        /**
         * Password derivation block size, for BCFKS refer to {@link org.bouncycastle.crypto.util.ScryptConfig}
         */
        @Min(1)
        private int blockSize;

        /**
         * Password derivation parallelization, for BCFKS refer to {@link org.bouncycastle.crypto.util.ScryptConfig}
         */
        @Min(1)
        private int parallelization;

        /**
         * Password derivation salt length, for BCFKS refer to {@link org.bouncycastle.crypto.util.ScryptConfig}
         */
        @Min(1)
        private int saltLength;
    }
}
