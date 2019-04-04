package de.adorsys.docusafe2.keystore.impl;

import de.adorsys.docusafe2.keystore.api.exceptions.KeyStoreConfigException;
import de.adorsys.docusafe2.keystore.api.types.*;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;
import java.util.Date;
import java.util.UUID;

public class KeyStoreGenerator {
    private final static Logger LOGGER = LoggerFactory.getLogger(KeyStoreGenerator.class);
    private final KeyStoreType keyStoreType;
    private final String serverKeyPairAliasPrefix;
    private final KeyStoreCreationConfigImpl config;
    private final ReadKeyPassword readKeyPassword;

    public KeyStoreGenerator(
            KeyStoreCreationConfig config,
            KeyStoreType keyStoreType,
            String serverKeyPairAliasPrefix,
            ReadKeyPassword readKeyPassword
    ) {
        this.config = new KeyStoreCreationConfigImpl(config);
        this.keyStoreType = keyStoreType;
        // this.serverKeyPairAliasPrefix = serverKeyPairAliasPrefix;
        this.serverKeyPairAliasPrefix = "KEYSTORE-ID-0";
        this.readKeyPassword = readKeyPassword;
        LOGGER.debug("Keystore ID ignored " + serverKeyPairAliasPrefix);
    }

    public KeyStore generate() {
        if (config.getEncKeyNumber() == 0 &&
                config.getSecretKeyNumber() == 0 &&
                config.getSignKeyNumber() == 0) {
            throw new KeyStoreConfigException("Configuration of keystore must at least contain one key");
        }
        KeyStore keyStore = null;
        Date startTime = new Date();
        try {
            String keyStoreID = serverKeyPairAliasPrefix;
            CallbackHandler readKeyHandler = new PasswordCallbackHandler(readKeyPassword.getValue().toCharArray());
            KeystoreBuilder keystoreBuilder = new KeystoreBuilder().withStoreType(keyStoreType);

            {
                KeyPairGenerator encKeyPairGenerator = config.getEncKeyPairGenerator(keyStoreID);
                int numberOfEncKeyPairs = config.getEncKeyNumber();
                for (int i = 0; i < numberOfEncKeyPairs; i++) {
                    KeyPairEntry signatureKeyPair = encKeyPairGenerator.generateEncryptionKey(
                            serverKeyPairAliasPrefix + RandomStringUtils.randomAlphanumeric(5).toUpperCase(),
                            readKeyHandler
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
                            readKeyHandler
                    );

                    keystoreBuilder = keystoreBuilder.withKeyEntry(signatureKeyPair);
                }
            }
            {
                SecretKeyGenerator secretKeyGenerator = config.getSecretKeyGenerator(keyStoreID);
                int numberOfSecretKeys = config.getSecretKeyNumber();
                for (int i = 0; i < numberOfSecretKeys; i++) {
                    SecretKeyEntry secretKeyData = secretKeyGenerator.generate(
                            serverKeyPairAliasPrefix + RandomStringUtils.randomAlphanumeric(5).toUpperCase(),
                            readKeyHandler
                    );

                    keystoreBuilder = keystoreBuilder.withKeyEntry(secretKeyData);
                }
            }
            keyStore = keystoreBuilder.build();
            return keyStore;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        } finally {
            Date stopTime = new Date();
            long duration = stopTime.getTime() - startTime.getTime();
            LOGGER.debug("KeyStoreGeneration (milliseconds) DURATION WAS " + duration);
        }
    }
}
