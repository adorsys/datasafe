package de.adorsys.datasafe.business.impl.encryption.keystore;

import de.adorsys.common.exceptions.BaseException;
import de.adorsys.common.exceptions.BaseExceptionHandler;
import de.adorsys.common.utils.HexUtil;
import de.adorsys.datasafe.business.api.encryption.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.types.keystore.*;
import de.adorsys.datasafe.business.impl.encryption.keystore.generator.KeyStoreServiceImplBaseFunctions;
import de.adorsys.datasafe.business.impl.encryption.keystore.generator.PasswordCallbackHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;

@Slf4j
public class KeyStoreServiceImpl implements KeyStoreService {

    @Inject
    public KeyStoreServiceImpl() {
    }

    @Override
    public KeyStore createKeyStore(KeyStoreAuth keyStoreAuth,
                                   KeyStoreType keyStoreType,
                                   KeyStoreCreationConfig config) {
        try {
            log.debug("start create keystore ");
            if (config == null) {
                config = new KeyStoreCreationConfig(5, 5, 5);
            }
            // TODO, hier also statt der StoreID nun das
            String serverKeyPairAliasPrefix = HexUtil.convertBytesToHexString(UUID.randomUUID().toString().getBytes());
            log.debug("keystoreid = " + serverKeyPairAliasPrefix);
            {
                String realKeyStoreId = new String(HexUtil.convertHexStringToBytes(serverKeyPairAliasPrefix));
                log.debug("meaning of keystoreid = " + realKeyStoreId);
            }
            KeyStoreGenerator keyStoreGenerator = new KeyStoreGenerator(
                    config,
                    keyStoreType,
                    serverKeyPairAliasPrefix,
                    keyStoreAuth.getReadKeyPassword());
            KeyStore userKeyStore = keyStoreGenerator.generate();
            log.debug("finished create keystore ");
            return userKeyStore;
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }

    @Override
    public List<PublicKeyIDWithPublicKey> getPublicKeys(KeyStoreAccess keyStoreAccess) {
        log.debug("get public keys");
        List<PublicKeyIDWithPublicKey> result = new ArrayList<>();
        KeyStore keyStore = keyStoreAccess.getKeyStore();
        try {
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
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
        return result;
    }

    @Override
    public PrivateKey getPrivateKey(KeyStoreAccess keyStoreAccess, KeyID keyID) {
        ReadKeyPassword readKeyPassword = keyStoreAccess.getKeyStoreAuth().getReadKeyPassword();
        KeyStore keyStore = keyStoreAccess.getKeyStore();
        PrivateKey privateKey;
        try {
            privateKey = (PrivateKey) keyStore.getKey(keyID.getValue(), readKeyPassword.getValue().toCharArray());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
        return privateKey;
    }

    @Override
    public SecretKeySpec getSecretKey(KeyStoreAccess keyStoreAccess, KeyID keyID) {
        KeyStore keyStore = keyStoreAccess.getKeyStore();
        SecretKeySpec key = null;
        try {
            char[] password = keyStoreAccess.getKeyStoreAuth().getReadKeyPassword().getValue().toCharArray();
            key = (SecretKeySpec) keyStore.getKey(keyID.getValue(), password);
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
            throw BaseExceptionHandler.handle(e);
        }
        return key;
    }

    @Override
    public SecretKeyIDWithKey getRandomSecretKeyID(KeyStoreAccess keyStoreAccess) {
        KeyStore keyStore = keyStoreAccess.getKeyStore();
        Key key = null;
        String randomAlias = null;
        try {
            Enumeration<String> aliases = keyStore.aliases();
            // Do not return the Path encryption key

            List<String> keyIDs = new ArrayList<>();
            for (String keyAlias : Collections.list(aliases)) {
                if (keyStore.entryInstanceOf(keyAlias, KeyStore.SecretKeyEntry.class)) {
                    // DO not use the path encryption key for content encryption.
                    if (KeyStoreCreationConfig.PATH_KEY_ID.getValue().equals(keyAlias)) {
                        continue;
                    }
                    keyIDs.add(keyAlias);
                }
            }
            if (keyIDs.size() == 0) {
                throw new BaseException("No secret keys in the keystore");
            }
            int randomIndex = RandomUtils.nextInt(0, keyIDs.size());
            randomAlias = keyIDs.get(randomIndex);
            char[] password = keyStoreAccess.getKeyStoreAuth().getReadKeyPassword().getValue().toCharArray();
            key = keyStore.getKey(randomAlias, password);
        } catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
            throw BaseExceptionHandler.handle(e);
        }
        return new SecretKeyIDWithKey(new KeyID(randomAlias), (SecretKey) key);
    }

    @Override
    public byte[] serialize(KeyStore store, String storeId, ReadStorePassword password) {
        return KeyStoreServiceImplBaseFunctions.toByteArray(
                store,
                storeId,
                new PasswordCallbackHandler(password.getValue().toCharArray())
        );
    }

    @Override
    public KeyStore deserialize(byte[] payload, String storeId, ReadStorePassword password) {
        return KeyStoreServiceImplBaseFunctions.loadKeyStore(
                payload,
                storeId,
                KeyStoreType.DEFAULT,
                new PasswordCallbackHandler(password.getValue().toCharArray())
        );
    }
}
