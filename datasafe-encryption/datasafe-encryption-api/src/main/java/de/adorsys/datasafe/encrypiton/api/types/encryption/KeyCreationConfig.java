package de.adorsys.datasafe.encrypiton.api.types.encryption;

import lombok.Builder;
import lombok.Getter;

/**
 * Wrapper that contains count of public-key pairs and count of encryption keys.
 */
@Getter
@Builder(toBuilder = true)
public class KeyCreationConfig {

    public static final String PATH_KEY_ID_PREFIX = "PATH_SECRET";
    public static final String PATH_KEY_ID_PREFIX_CTR = "PATH_CTR_SECRET_";
    public static final String DOCUMENT_KEY_ID_PREFIX = "PRIVATE_SECRET";

    @Builder.Default
    private final int encKeyNumber = 1;

    @Builder.Default
    private final int signKeyNumber = 1;

    @Builder.Default
    private final SecretKeyCreationCfg secret = SecretKeyCreationCfg.builder().build();

    @Builder.Default
    private final EncryptingKeyCreationCfg encrypting = EncryptingKeyCreationCfg.builder().build();

    @Builder.Default
    private final SigningKeyCreationCfg signing = SigningKeyCreationCfg.builder().build();

    @Getter
    @Builder
    public static class SecretKeyCreationCfg {

        @Builder.Default
        private final String algo = "AES";

        @Builder.Default
        private final int size = 256;
    }

    @Getter
    @Builder
    public static class EncryptingKeyCreationCfg {

        @Builder.Default
        private final String algo = "RSA";

        @Builder.Default
        private final int size = 2048;

        @Builder.Default
        private final String sigAlgo = "SHA256withRSA";
    }

    @Getter
    @Builder
    public static class SigningKeyCreationCfg {

        @Builder.Default
        private final String algo = "RSA";

        @Builder.Default
        private final int size = 2048;

        @Builder.Default
        private final String sigAlgo = "SHA256withRSA";
    }
}
