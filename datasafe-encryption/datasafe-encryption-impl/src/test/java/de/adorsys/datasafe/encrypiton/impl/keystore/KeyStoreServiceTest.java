package de.adorsys.datasafe.encrypiton.impl.keystore;

import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.encryption.EncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.encrypiton.impl.KeystoreUtil;
import de.adorsys.datasafe.encrypiton.impl.utils.ProviderUtils;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import de.adorsys.keymanagement.api.Juggler;
import de.adorsys.keymanagement.api.config.keystore.KeyStoreConfig;
import de.adorsys.keymanagement.api.types.KeySetTemplate;
import de.adorsys.keymanagement.api.types.source.KeySet;
import de.adorsys.keymanagement.api.types.template.generated.Encrypting;
import de.adorsys.keymanagement.juggler.services.DaggerBCJuggler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.List;

import static de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig.DOCUMENT_KEY_ID_PREFIX;

class KeyStoreServiceTest extends BaseMockitoTest {

    private KeyStoreService keyStoreService = new KeyStoreServiceImpl(
                EncryptionConfig.builder().build().getKeystore(),
                DaggerBCJuggler.builder().build()
    );
    private KeyStoreAuth keyStoreAuth;

    @BeforeEach
    void setUp() {
        ReadStorePassword readStorePassword = new ReadStorePassword("keystorepass");
        ReadKeyPassword readKeyPassword = ReadKeyPasswordTestFactory.getForString("keypass");
        keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);
    }

    @Test
    void createKeyStore() throws Exception {
        KeyCreationConfig config = KeyCreationConfig.builder().signKeyNumber(0).encKeyNumber(1).build();
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, config);

        Assertions.assertNotNull(keyStore);

        List<String> list = Collections.list(keyStore.aliases());
        // Two additional secret key(key and counter key) being generated for path encryption and one for private doc encryption.
        Assertions.assertEquals(4, list.size());

        Assertions.assertEquals("BCFKS", keyStore.getType());
        Assertions.assertEquals(ProviderUtils.bcProvider, keyStore.getProvider());
    }

    @Test
    void createKeyStoreEmptyConfig() throws Exception {
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyCreationConfig.builder().build());
        Assertions.assertNotNull(keyStore);
        List<String> list = Collections.list(keyStore.aliases());
        // One additional secret key being generated for path encryption and one for private doc encryption.
        Assertions.assertEquals(4, list.size());
    }

    @Test
    void getPublicKeys() {
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyCreationConfig.builder().build());
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);
        List<PublicKeyIDWithPublicKey> publicKeys = keyStoreService.getPublicKeys(keyStoreAccess);

        Assertions.assertEquals(1, publicKeys.size());
    }

    @Test
    void getPrivateKey() throws Exception {
        KeyStoreConfig config = KeyStoreConfig.builder().type("UBER").build();
        Juggler juggler = DaggerBCJuggler.builder().keyStoreConfig(config).build();
        KeySetTemplate template = KeySetTemplate.builder()
                .generatedEncryptionKeys(Encrypting.with().prefix("KEYSTORE-ID-0").password("keypass"::toCharArray).build().repeat(1))
                .build();
        KeySet keySet = juggler.generateKeys().fromTemplate(template);
        PrivateKey privateKey1 = keySet.getKeyPairs().get(0).getPair().getPrivate();
        KeyStore keyStore = juggler.toKeystore().generate(keySet, () -> keyStoreAuth.getReadStorePassword().getValue());
        Assertions.assertEquals("UBER", keyStore.getType());
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

        String keyID = keyStore.aliases().nextElement();
        PrivateKey privateKey2 = keyStoreService.getPrivateKey(keyStoreAccess, new KeyID(keyID));
        Assertions.assertEquals(privateKey1, privateKey2);
    }

    @Test
    void getPrivateKeyException() throws Exception {
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyCreationConfig.builder().build());
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
        KeyCreationConfig config = KeyCreationConfig.builder().signKeyNumber(1).encKeyNumber(0).build();
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, config);
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

        KeyID keyID = KeystoreUtil.keyIdByPrefix(keyStore, DOCUMENT_KEY_ID_PREFIX);
        SecretKey secretKey = keyStoreService.getSecretKey(keyStoreAccess, keyID);
        Assertions.assertNotNull(secretKey);
    }
}
