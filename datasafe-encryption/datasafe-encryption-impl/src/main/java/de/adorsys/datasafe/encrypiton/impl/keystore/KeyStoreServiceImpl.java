package de.adorsys.datasafe.encrypiton.impl.keystore;

import com.google.common.collect.ImmutableMap;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.keystore.*;
import de.adorsys.datasafe.encrypiton.impl.keystore.generator.KeyStoreServiceImplBaseFunctions;
import de.adorsys.datasafe.encrypiton.impl.keystore.types.PasswordBasedKeyConfig;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.*;

import static de.adorsys.datasafe.encrypiton.api.types.keystore.KeyCreationConfig.*;

@Slf4j
@RuntimeDelegate
public class KeyStoreServiceImpl implements KeyStoreService {

    private final PasswordBasedKeyConfig passwordBasedKeyConfig;
    private final Optional<KeyStoreConfig> keyStoreCreationConfig;

    @Inject
    public KeyStoreServiceImpl(PasswordBasedKeyConfig passwordBasedKeyConfig,
                               Optional<KeyStoreConfig> keyStoreCreationConfig) {
        this.passwordBasedKeyConfig = passwordBasedKeyConfig;
        this.keyStoreCreationConfig = keyStoreCreationConfig;
    }

    @Override
    public KeyStore createKeyStore(KeyStoreAuth keyStoreAuth,
                                   KeyCreationConfig config) {

        return createKeyStore(
                keyStoreAuth,
                config,
                ImmutableMap.of(
                        new KeyID(PATH_KEY_ID_PREFIX + UUID.randomUUID().toString()), Optional.empty(),
                        new KeyID(PATH_KEY_ID_PREFIX_CTR + UUID.randomUUID().toString()), Optional.empty(),
                        new KeyID(DOCUMENT_KEY_ID_PREFIX + UUID.randomUUID().toString()), Optional.empty()
                )
        );
    }

    @Override
    public KeyStore createKeyStore(KeyStoreAuth keyStoreAuth,
                                   KeyCreationConfig config,
                                   Map<KeyID, Optional<SecretKeyEntry>> secretKeys) {

        log.debug("start create keystore ");
        if (config == null) {
            config = new KeyCreationConfig(5, 5);
        }
        // TODO, hier also statt der StoreID nun das
        String serverKeyPairAliasPrefix = UUID.randomUUID().toString();
        log.debug("keystoreid = {}", serverKeyPairAliasPrefix);
        KeyStoreGenerator keyStoreGenerator = KeyStoreGenerator.builder()
                .keyCreationConfig(config)
                .keyStoreConfig(keyStoreCreationConfig.orElse(KeyStoreConfig.DEFAULT))
                .serverKeyPairAliasPrefix(serverKeyPairAliasPrefix)
                .readKeyPassword(keyStoreAuth.getReadKeyPassword())
                .secretKeys(secretKeys)
                .build();

        KeyStore userKeyStore = keyStoreGenerator.generate();
        log.debug("finished create keystore ");
        return userKeyStore;
    }

    @Override
    @SneakyThrows
    public KeyStore updateKeyStoreReadKeyPassword(KeyStore current,
                                                  KeyStoreAuth currentCredentials,
                                                  KeyStoreAuth newCredentials) {
        KeyStore newKeystore = KeyStore.getInstance(current.getType());
        newKeystore.load(null, null);
        Enumeration<String> aliases = current.aliases();

        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            Key currentKey = current.getKey(alias, currentCredentials.getReadKeyPassword().getValue().toCharArray());
            newKeystore.setKeyEntry(
                    alias,
                    currentKey,
                    newCredentials.getReadKeyPassword().getValue().toCharArray(),
                    current.getCertificateChain(alias)
            );
        }

        return newKeystore;
    }

    @Override
    @SneakyThrows
    public List<PublicKeyIDWithPublicKey> getPublicKeys(KeyStoreAccess keyStoreAccess) {
        log.debug("get public keys");
        List<PublicKeyIDWithPublicKey> result = new ArrayList<>();
        KeyStore keyStore = keyStoreAccess.getKeyStore();
        for (Enumeration<String> keyAliases = keyStore.aliases(); keyAliases.hasMoreElements(); ) {
            final String keyAlias = keyAliases.nextElement();
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(keyAlias);
            if (cert == null) continue; // skip
            boolean[] keyUsage = cert.getKeyUsage();
            // digitalSignature (0), nonRepudiation (1), keyEncipherment (2), dataEncipherment (3),
            // keyAgreement (4), keyCertSign (5), cRLSign (6), encipherOnly (7), decipherOnly (8)
            if (keyUsage[2] || keyUsage[3] || keyUsage[4]) {
                result.add(new PublicKeyIDWithPublicKey(new KeyID(keyAlias), cert.getPublicKey()));
            }
        }
        return result;
    }

    @Override
    @SneakyThrows
    public PrivateKey getPrivateKey(KeyStoreAccess keyStoreAccess, KeyID keyID) {
        ReadKeyPassword readKeyPassword = keyStoreAccess.getKeyStoreAuth().getReadKeyPassword();
        KeyStore keyStore = keyStoreAccess.getKeyStore();
        PrivateKey privateKey;
        privateKey = (PrivateKey) keyStore.getKey(keyID.getValue(), readKeyPassword.getValue().toCharArray());
        return privateKey;
    }

    @Override
    @SneakyThrows
    public SecretKeySpec getSecretKey(KeyStoreAccess keyStoreAccess, KeyID keyID) {
        KeyStore keyStore = keyStoreAccess.getKeyStore();
        char[] password = keyStoreAccess.getKeyStoreAuth().getReadKeyPassword().getValue().toCharArray();
        return (SecretKeySpec) keyStore.getKey(keyID.getValue(), password);
    }

    @Override
    @SneakyThrows
    public void addPasswordBasedSecretKey(KeyStoreAccess keyStoreAccess, String alias, char[] secret) {
        PBEKeySpec pbeKeySpec = new PBEKeySpec(secret);
        SecretKeyFactory keyFac = SecretKeyFactory.getInstance(passwordBasedKeyConfig.secretKeyFactoryId());
        SecretKey key = keyFac.generateSecret(pbeKeySpec);
        keyStoreAccess.getKeyStore()
                .setKeyEntry(
                        alias,
                        key,
                        keyStoreAccess.getKeyStoreAuth().getReadKeyPassword().getValue().toCharArray(),
                        null
                );
    }

    @Override
    @SneakyThrows
    public void removeKey(KeyStoreAccess keyStoreAccess, String alias) {
        keyStoreAccess.getKeyStore().deleteEntry(alias);
    }

    @Override
    public byte[] serialize(KeyStore store, String storeId, ReadStorePassword readStorePassword) {
        return KeyStoreServiceImplBaseFunctions.toByteArray(
                store,
                storeId,
                readStorePassword
        );
    }

    @Override
    public KeyStore deserialize(byte[] payload, String storeId, ReadStorePassword readStorePassword) {
        return KeyStoreServiceImplBaseFunctions.loadKeyStore(
                payload,
                storeId,
                keyStoreCreationConfig.orElse(KeyStoreConfig.DEFAULT),
                readStorePassword
        );
    }
}
