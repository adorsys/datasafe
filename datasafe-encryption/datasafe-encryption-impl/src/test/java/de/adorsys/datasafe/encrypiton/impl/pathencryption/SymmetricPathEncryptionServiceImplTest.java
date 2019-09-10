package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.keystore.*;
import de.adorsys.datasafe.encrypiton.impl.KeystoreUtil;
import de.adorsys.datasafe.encrypiton.impl.keystore.DefaultPasswordBasedKeyConfig;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImpl;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.security.KeyStore;

import static de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreCreationConfig.PATH_KEY_ID_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class SymmetricPathEncryptionServiceImplTest extends BaseMockitoTest {

    private SymmetricPathEncryptionServiceImpl bucketPathEncryptionService = new SymmetricPathEncryptionServiceImpl(
            new DefaultPathEncryptorDecryptor()
    );

    private KeyStoreService keyStoreService = new KeyStoreServiceImpl(new DefaultPasswordBasedKeyConfig());
    private ReadKeyPassword readKeyPassword = new ReadKeyPassword("readkeypassword");
    private ReadStorePassword readStorePassword = new ReadStorePassword("readstorepassword");
    private KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);
    private KeyStoreCreationConfig config = new KeyStoreCreationConfig(0, 1);
    private KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, config);
    private KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

    @Test
    void testSuccessEncryptDecryptPath() {
        String testPath = "path/to/file";

        Uri testURI = new Uri(testPath);

        SecretKeySpec secretKey = keyStoreService.getSecretKey(
                keyStoreAccess,
                KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX)
        );

        SecretKeySpec secretKeyCrt = keyStoreService.getSecretKey(
                keyStoreAccess,
                KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX)
        );

        SecretKeyIDWithKey secretKeyIDWithKey = new SecretKeyIDWithKey(
                KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX), secretKey, secretKeyCrt);

        Uri encrypted = bucketPathEncryptionService.encrypt(secretKeyIDWithKey, testURI);
        log.debug("Encrypted path: {}", encrypted);

        Uri decrypted = bucketPathEncryptionService.decrypt(secretKeyIDWithKey, encrypted);
        log.debug("Decrypted path: {}", decrypted);

        assertEquals(testPath, decrypted.toASCIIString());
    }

    @Test
    void testFailEncryptPathWithWrongKeyID() {
        String testPath = "path/to/file/";

        log.info("Test path: {}", testPath);

        Uri testURI = new Uri(testPath);

        SecretKeySpec secretKey = keyStoreService.getSecretKey(keyStoreAccess, new KeyID("Invalid key"));

        SecretKeyIDWithKey secretKeyIDWithKey = new SecretKeyIDWithKey(
                KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX), secretKey, secretKey);

        // secret keys is null, because during key obtain was used incorrect KeyID,
        // so bucketPathEncryptionService#encrypt throw BaseException(was handled NullPointerException)
        assertThrows(IllegalArgumentException.class, () -> bucketPathEncryptionService.encrypt(secretKeyIDWithKey, testURI));
    }

    @Test
    void testFailEncryptPathWithBrokenEncryptedPath() {
        SecretKeySpec secretKey = keyStoreService.getSecretKey(
                keyStoreAccess,
                KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX)
        );

        SecretKeyIDWithKey secretKeyIDWithKey = new SecretKeyIDWithKey(
                KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX), secretKey, secretKey);

        assertThrows(BadPaddingException.class,
                () -> bucketPathEncryptionService.decrypt(secretKeyIDWithKey,
                        new Uri(URI.create("bRQiW8qLNPEy5tO7shfV0w==/k0HooCVlmhHkQFw8mc=="))));
    }

    @Test
    void testFailEncryptPathWithTextPath() {
        SecretKeySpec secretKey = keyStoreService.getSecretKey(
                keyStoreAccess,
                KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX)
        );

        SecretKeyIDWithKey secretKeyIDWithKey = new SecretKeyIDWithKey(
                KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX), secretKey, secretKey);

        assertThrows(IllegalBlockSizeException.class,
                () -> bucketPathEncryptionService.decrypt(secretKeyIDWithKey,
                        new Uri("/simple/text/path/")));
    }
}