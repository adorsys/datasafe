package de.adorsys.datasafe.encrypiton.impl.keystore;

import com.google.common.collect.ImmutableMap;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.keystore.*;
import de.adorsys.datasafe.encrypiton.impl.keystore.generator.KeyStoreServiceImplBaseFunctions;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.*;

import static de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreCreationConfig.PATH_KEY_ID_PREFIX;
import static de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreCreationConfig.DOCUMENT_KEY_ID_PREFIX;

@Slf4j
@RuntimeDelegate
public class KeyStoreServiceImpl implements KeyStoreService {

    @Inject
    public KeyStoreServiceImpl() {
    }

    @Override
    public KeyStore createKeyStore(KeyStoreAuth keyStoreAuth,
                                   KeyStoreType keyStoreType,
                                   KeyStoreCreationConfig config) {

        return createKeyStore(
                keyStoreAuth,
                keyStoreType,
                config,
                ImmutableMap.of(
                        new KeyID(PATH_KEY_ID_PREFIX + UUID.randomUUID().toString()), Optional.empty(),
                        new KeyID(DOCUMENT_KEY_ID_PREFIX + UUID.randomUUID().toString()), Optional.empty()
                )
        );
    }

    @Override
    public KeyStore createKeyStore(KeyStoreAuth keyStoreAuth,
                                   KeyStoreType keyStoreType,
                                   KeyStoreCreationConfig config,
                                   Map<KeyID, Optional<SecretKeyEntry>> secretKeys) {

        log.debug("start create keystore ");
        if (config == null) {
            config = new KeyStoreCreationConfig(5, 5);
        }
        // TODO, hier also statt der StoreID nun das
        String serverKeyPairAliasPrefix = UUID.randomUUID().toString();
        log.debug("keystoreid = {}", serverKeyPairAliasPrefix);
        KeyStoreGenerator keyStoreGenerator = KeyStoreGenerator.builder()
                .config(config)
                .keyStoreType(keyStoreType)
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
        SecretKeySpec key = null;
        char[] password = keyStoreAccess.getKeyStoreAuth().getReadKeyPassword().getValue().toCharArray();
        key = (SecretKeySpec) keyStore.getKey(keyID.getValue(), password);
        return key;
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
                KeyStoreType.DEFAULT,
                readStorePassword
        );
    }
}
