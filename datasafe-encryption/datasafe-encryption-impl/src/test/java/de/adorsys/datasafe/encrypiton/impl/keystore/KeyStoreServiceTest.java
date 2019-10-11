package de.adorsys.datasafe.encrypiton.impl.keystore;

import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.keystore.*;
import de.adorsys.datasafe.encrypiton.api.types.keystore.exceptions.KeyStoreConfigException;
import de.adorsys.datasafe.encrypiton.impl.KeystoreUtil;
import de.adorsys.datasafe.encrypiton.impl.WithBouncyCastle;
import de.adorsys.datasafe.encrypiton.impl.keystore.generator.KeyCreationConfigImpl;
import de.adorsys.datasafe.encrypiton.impl.keystore.generator.KeyStoreServiceImplBaseFunctions;
import de.adorsys.datasafe.encrypiton.impl.keystore.types.KeyPairEntry;
import de.adorsys.datasafe.encrypiton.impl.keystore.types.KeyPairGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.adorsys.datasafe.encrypiton.api.types.keystore.KeyCreationConfig.DOCUMENT_KEY_ID_PREFIX;

class KeyStoreServiceTest extends WithBouncyCastle {

    private KeyStoreService keyStoreService = new KeyStoreServiceImpl(new DefaultPasswordBasedKeyConfig(), Optional.empty());
    private KeyStoreAuth keyStoreAuth;

    @BeforeEach
    void setUp() {
        ReadStorePassword readStorePassword = new ReadStorePassword("keystorepass");
        ReadKeyPassword readKeyPassword = new ReadKeyPassword("keypass");
        keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);
    }

    @Test
    void createKeyStore() throws Exception {
        KeyCreationConfig config = new KeyCreationConfig(0, 1);
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, config);

        Assertions.assertNotNull(keyStore);

        List<String> list = Collections.list(keyStore.aliases());
        // Two additional secret key(key and counter key) being generated for path encryption and one for private doc encryption.
        Assertions.assertEquals(4, list.size());

        Assertions.assertEquals("BCFKS", keyStore.getType());
        Assertions.assertEquals(Security.getProvider("BC"), keyStore.getProvider());
    }

    @Test
    void createKeyStoreEmptyConfig() throws Exception {
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, null);
        Assertions.assertNotNull(keyStore);
        List<String> list = Collections.list(keyStore.aliases());
        // One additional secret key being generated for path encryption and one for private doc encryption.
        Assertions.assertEquals(13, list.size());
    }

    @Test
    void createKeyStoreException() {
        KeyCreationConfig config = new KeyCreationConfig(0, 0);

            Assertions.assertThrows(KeyStoreConfigException.class, () ->
                    keyStoreService.createKeyStore(keyStoreAuth, config, Collections.emptyMap())
            );
    }

    @Test
    void getPublicKeys() {
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, null);
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);
        List<PublicKeyIDWithPublicKey> publicKeys = keyStoreService.getPublicKeys(keyStoreAccess);

        Assertions.assertEquals(5, publicKeys.size());
    }

    @Test
    void getPrivateKey() throws Exception {
        KeyStore keyStore = KeyStoreServiceImplBaseFunctions.newKeyStore(KeyStoreCreationConfig.DEFAULT); // BCFKS

        ReadKeyPassword readKeyPassword = new ReadKeyPassword("keypass");
        KeyCreationConfigImpl keyStoreCreationConfig = new KeyCreationConfigImpl(null);
        KeyPairGenerator encKeyPairGenerator = keyStoreCreationConfig.getEncKeyPairGenerator("KEYSTORE-ID-0");
        String alias = "KEYSTORE-ID-0" + UUID.randomUUID().toString();
        KeyPairEntry keyPairEntry = encKeyPairGenerator.generateEncryptionKey(alias, readKeyPassword);
        KeyStoreServiceImplBaseFunctions.addToKeyStore(keyStore, keyPairEntry);

        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

        String keyID = keyStore.aliases().nextElement();
        PrivateKey privateKey = keyStoreService.getPrivateKey(keyStoreAccess, new KeyID(keyID));
        System.out.println(privateKey);
        System.out.println(keyID);
        Assertions.assertEquals(alias, keyID);
    }

    @Test
    void getPrivateKeyException() throws Exception {
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, null);
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);
        List<String> list = Collections.list(keyStore.aliases());
        Assertions.assertThrows(ClassCastException.class, () -> {
        	for(String id : list) {
        		keyStoreService.getPrivateKey(keyStoreAccess, new KeyID(id));
        	}
        });
    }

    @Test
    void getSecretKey() {
        KeyCreationConfig config = new KeyCreationConfig(0, 1);
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, config);
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

        KeyID keyID = KeystoreUtil.keyIdByPrefix(keyStore, DOCUMENT_KEY_ID_PREFIX);
        SecretKey secretKey = keyStoreService.getSecretKey(keyStoreAccess, keyID);
        Assertions.assertNotNull(secretKey);
    }
}
