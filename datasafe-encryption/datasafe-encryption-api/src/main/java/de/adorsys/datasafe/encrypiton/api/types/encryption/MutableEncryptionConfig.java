package de.adorsys.datasafe.encrypiton.api.types.encryption;

import de.adorsys.keymanagement.api.config.keystore.KeyStoreConfig;
import de.adorsys.keymanagement.api.config.keystore.pbkdf.PBKDF2;
import de.adorsys.keymanagement.api.config.keystore.pbkdf.Scrypt;
import lombok.Data;

/**
 * This is a helper class to aid mapping between Spring ConfigurationProperties and actual
 * {@link EncryptionConfig} because Spring Boot had started to support immutable fields from version 2.2 which
 * is rather new to rely on now.
 * See {@link EncryptionConfig} for defaults and details on fields.
 */
@Data
public class MutableEncryptionConfig {

    private MutableKeyStoreCreationConfig keystore;
    private MutableKeyCreationConfig keys;
    private MutableCmsEncryptionConfig cms;

    @Data
    public static class MutableCmsEncryptionConfig {

        private String algo;

        CmsEncryptionConfig toCmsEncryptionConfig() {
            CmsEncryptionConfig.CmsEncryptionConfigBuilder builder = CmsEncryptionConfig.builder();
            if (null != algo) {
                builder.algo(algo);
            }

            return builder.build();
        }
    }

    @Data
    public static class MutableKeyStoreCreationConfig {

        private String type;
        private String encryptionAlgo;
        private MutableKeyStoreCreationConfig.MutablePBKDF pbkdf;
        private String macAlgo;
        private String passwordKeysAlgo;

        @Data
        public static class MutablePBKDF {

            private MutablePBKDF2 pbkdf2;
            private MutableScrypt scrypt;

            @Data
            public static class MutablePBKDF2 {

                private String algo;
                private Integer saltLength;
                private Integer iterCount;

                PBKDF2 toPBKDF2() {
                    PBKDF2.PBKDF2Builder builder = PBKDF2.builder();
                    if (null != algo) {
                        builder.algo(algo);
                    }

                    if (null != saltLength) {
                        builder.saltLength(saltLength);
                    }

                    if (null != iterCount) {
                        builder.iterCount(iterCount);
                    }

                    return builder.build();
                }
            }

            @Data
            public static class MutableScrypt {

                private Integer cost;
                private Integer blockSize;
                private Integer parallelization;
                private Integer saltLength;

                Scrypt toScrypt() {

                    Scrypt.ScryptBuilder builder = Scrypt.builder();

                    if (null != cost) {
                        builder.cost(cost);
                    }

                    if (null != blockSize) {
                        builder.blockSize(blockSize);
                    }

                    if (null != parallelization) {
                        builder.parallelization(parallelization);
                    }

                    if (null != saltLength) {
                        builder.saltLength(saltLength);
                    }

                    return builder.build();
                }
            }

            KeyStoreConfig.PBKDF toPBKDF() {
                KeyStoreConfig.PBKDF.PBKDFBuilder builder = KeyStoreConfig.PBKDF.builder();

                if (null != pbkdf2) {
                    builder.pbkdf2(pbkdf2.toPBKDF2());
                }

                if (null != scrypt) {
                    builder.pbkdf2(null);
                    builder.scrypt(scrypt.toScrypt());
                }

                return builder.build();
            }
        }

        KeyStoreConfig toKeyStoreConfig() {
            KeyStoreConfig.KeyStoreConfigBuilder builder = KeyStoreConfig.builder();
            if (null != type) {
                builder.type(type);
            }

            if (null != encryptionAlgo) {
                builder.encryptionAlgo(encryptionAlgo);
            }

            if (null != pbkdf) {
                builder.pbkdf(pbkdf.toPBKDF());
            }

            if (null != macAlgo) {
                builder.macAlgo(macAlgo);
            }

            if (null != passwordKeysAlgo) {
                builder.passwordKeysAlgo(passwordKeysAlgo);
            }

            return builder.build();
        }
    }

    @Data
    public static class MutableKeyCreationConfig {

        private Integer encKeyNumber;
        private Integer signKeyNumber;
        private MutableSecretKeyCreationCfg secret;
        private MutableEncryptingKeyCreationCfg encrypting;
        private MutableSigningKeyCreationCfg signing;

        @Data
        public static class MutableSecretKeyCreationCfg {

            private String algo;
            private Integer size;

            KeyCreationConfig.SecretKeyCreationCfg toSecretKeyCreationCfg() {
                KeyCreationConfig.SecretKeyCreationCfg.SecretKeyCreationCfgBuilder builder =
                        KeyCreationConfig.SecretKeyCreationCfg.builder();
                if (null != algo) {
                    builder.algo(algo);
                }

                if (null != size) {
                    builder.size(size);
                }

                return builder.build();
            }
        }

        @Data
        public static class MutableEncryptingKeyCreationCfg {

            private String algo;
            private Integer size;
            private String sigAlgo;

            KeyCreationConfig.EncryptingKeyCreationCfg toEncryptingKeyCreationCfg() {
                KeyCreationConfig.EncryptingKeyCreationCfg.EncryptingKeyCreationCfgBuilder builder =
                        KeyCreationConfig.EncryptingKeyCreationCfg.builder();
                if (null != algo) {
                    builder.algo(algo);
                }

                if (null != size) {
                    builder.size(size);
                }

                if (null != sigAlgo) {
                    builder.sigAlgo(sigAlgo);
                }

                return builder.build();
            }
        }

        @Data
        public static class MutableSigningKeyCreationCfg {

            private String algo;
            private Integer size;
            private String sigAlgo;

            KeyCreationConfig.SigningKeyCreationCfg toSigningKeyCreationCfg() {
                KeyCreationConfig.SigningKeyCreationCfg.SigningKeyCreationCfgBuilder builder =
                        KeyCreationConfig.SigningKeyCreationCfg.builder();
                if (null != algo) {
                    builder.algo(algo);
                }

                if (null != size) {
                    builder.size(size);
                }

                if (null != sigAlgo) {
                    builder.sigAlgo(sigAlgo);
                }

                return builder.build();
            }
        }

        KeyCreationConfig toKeyCreationConfig() {
            KeyCreationConfig.KeyCreationConfigBuilder builder = KeyCreationConfig.builder();
            if (null != encKeyNumber) {
                builder.encKeyNumber(encKeyNumber);
            }

            if (null != signKeyNumber) {
                builder.signKeyNumber(signKeyNumber);
            }

            if (null != secret) {
                builder.secret(secret.toSecretKeyCreationCfg());
            }

            if (null != encrypting) {
                builder.encrypting(encrypting.toEncryptingKeyCreationCfg());
            }

            if (null != signing) {
                builder.signing(signing.toSigningKeyCreationCfg());
            }

            return builder.build();
        }
    }

    public EncryptionConfig toEncryptionConfig() {
        EncryptionConfig.EncryptionConfigBuilder builder = EncryptionConfig.builder();
        if (null != keystore) {
            builder.keystore(keystore.toKeyStoreConfig());
        }

        if (null != keys) {
            builder.keys(keys.toKeyCreationConfig());
        }

        if (null != cms) {
            builder.cms(cms.toCmsEncryptionConfig());
        }

        return builder.build();
    }
}
