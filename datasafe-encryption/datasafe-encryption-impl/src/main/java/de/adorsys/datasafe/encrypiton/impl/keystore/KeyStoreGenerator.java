package de.adorsys.datasafe.encrypiton.impl.keystore;

import de.adorsys.datasafe.encrypiton.api.types.keystore.*;
import de.adorsys.datasafe.encrypiton.api.types.keystore.exceptions.KeyStoreConfigException;
import de.adorsys.datasafe.encrypiton.impl.keystore.generator.KeyCreationConfigImpl;
import de.adorsys.datasafe.encrypiton.impl.keystore.generator.KeystoreBuilder;
import de.adorsys.datasafe.encrypiton.impl.keystore.types.KeyPairEntry;
import de.adorsys.datasafe.encrypiton.impl.keystore.types.KeyPairGenerator;
import de.adorsys.datasafe.types.api.utils.Obfuscate;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.security.KeyStore;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// TODO: Refactor it - we need to use named keys
@Slf4j
public class KeyStoreGenerator {

    @NonNull
    private final KeyStoreCreationConfig keyStoreCreationConfig;

    @NonNull
    private final String serverKeyPairAliasPrefix;

    @NonNull
    private final KeyCreationConfigImpl config;

    @NonNull
    private final ReadKeyPassword readKeyPassword;

    @NonNull
    private final Map<KeyID, Optional<SecretKeyEntry>> secretKeys;

    @Builder
    protected KeyStoreGenerator(
            KeyCreationConfig keyCreationConfig,
            KeyStoreCreationConfig keyStoreCreationConfig,
            String serverKeyPairAliasPrefix,
            ReadKeyPassword readKeyPassword,
            Map<KeyID, Optional<SecretKeyEntry>> secretKeys
    ) {
        this.config = new KeyCreationConfigImpl(keyCreationConfig);
        this.keyStoreCreationConfig = keyStoreCreationConfig;
        this.serverKeyPairAliasPrefix = "KEYSTORE-ID-0";
        this.readKeyPassword = readKeyPassword;
        this.secretKeys = secretKeys;
        log.debug("Keystore ID ignored {}", Obfuscate.secure(serverKeyPairAliasPrefix));
    }

    public KeyStore generate() {
        if (config.getEncKeyNumber() == 0 &&
                secretKeys.isEmpty() &&
                config.getSignKeyNumber() == 0) {
            throw new KeyStoreConfigException("Configuration of keystore must at least contain one key");
        }
        KeyStore keyStore = null;
        Date startTime = new Date();
        try {
            String keyStoreID = serverKeyPairAliasPrefix;
            KeystoreBuilder keystoreBuilder = new KeystoreBuilder().withStoreType(keyStoreCreationConfig);

            {
                KeyPairGenerator encKeyPairGenerator = config.getEncKeyPairGenerator(keyStoreID);
                int numberOfEncKeyPairs = config.getEncKeyNumber();
                for (int i = 0; i < numberOfEncKeyPairs; i++) {
                    KeyPairEntry signatureKeyPair = encKeyPairGenerator.generateEncryptionKey(
                            serverKeyPairAliasPrefix + UUID.randomUUID().toString(),
                            readKeyPassword
                    );

                    keystoreBuilder = keystoreBuilder.withKeyEntry(signatureKeyPair);
                }
            }
            {
                KeyPairGenerator signKeyPairGenerator = config.getSignKeyPairGenerator(keyStoreID);
                int numberOfSignKeyPairs = config.getSignKeyNumber();
                for (int i = 0; i < numberOfSignKeyPairs; i++) {
                    KeyPairEntry signatureKeyPair = signKeyPairGenerator.generateSignatureKey(
                            serverKeyPairAliasPrefix + UUID.randomUUID().toString(),
                            readKeyPassword
                    );

                    keystoreBuilder = keystoreBuilder.withKeyEntry(signatureKeyPair);
                }
            }
            {
                SecretKeyGenerator secretKeyGenerator = config.getSecretKeyGenerator(keyStoreID);

                for (Map.Entry<KeyID, Optional<SecretKeyEntry>> keyEntry : secretKeys.entrySet()) {
                    keystoreBuilder = buildSecretKey(
                            keyEntry,
                            secretKeyGenerator,
                            readKeyPassword,
                            keystoreBuilder
                    );
                }
            }
            keyStore = keystoreBuilder.build();
            return keyStore;
        } finally {
            Date stopTime = new Date();
            long duration = stopTime.getTime() - startTime.getTime();
            log.debug("KeyStoreGeneration (milliseconds) DURATION WAS {}", duration);
        }
    }

    private KeystoreBuilder buildSecretKey(
            Map.Entry<KeyID, Optional<SecretKeyEntry>> keyEntry,
            SecretKeyGenerator secretKeyGenerator,
            ReadKeyPassword readKeyPassword,
            KeystoreBuilder keystoreBuilder) {

        SecretKeyEntry secretKeyData = keyEntry.getValue().orElse(
                secretKeyGenerator.generate(
                        keyEntry.getKey().getValue(),
                        readKeyPassword
                )
        );

        return keystoreBuilder.withKeyEntry(secretKeyData);
    }
}
