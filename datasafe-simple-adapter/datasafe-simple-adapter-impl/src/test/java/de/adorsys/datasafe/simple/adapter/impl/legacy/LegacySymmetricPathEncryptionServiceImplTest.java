package de.adorsys.datasafe.simple.adapter.impl.legacy;

import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.keystore.*;
import de.adorsys.datasafe.encrypiton.impl.keystore.DefaultPasswordBasedKeyConfig;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImpl;
import de.adorsys.datasafe.simple.adapter.api.legacy.pathencryption.LegacySymmetricPathEncryptionService;
import de.adorsys.datasafe.simple.adapter.impl.WithBouncyCastle;
import de.adorsys.datasafe.simple.adapter.impl.legacy.pathencryption.LegacyPathDigestConfig;
import de.adorsys.datasafe.simple.adapter.impl.legacy.pathencryption.LegacyPathEncryptor;
import de.adorsys.datasafe.simple.adapter.impl.legacy.pathencryption.LegacySymmetricPathEncryptionServiceImpl;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.util.Optional;

import static de.adorsys.datasafe.encrypiton.api.types.keystore.KeyCreationConfig.PATH_KEY_ID_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class LegacySymmetricPathEncryptionServiceImplTest extends WithBouncyCastle {

    private LegacySymmetricPathEncryptionService bucketPathEncryptionService = new LegacySymmetricPathEncryptionServiceImpl(
            new LegacyPathEncryptor(new LegacyPathDigestConfig())
    );

    private KeyStoreService keyStoreService = new KeyStoreServiceImpl(new DefaultPasswordBasedKeyConfig(), Optional.empty());
    private ReadKeyPassword readKeyPassword = new ReadKeyPassword("readkeypassword");
    private ReadStorePassword readStorePassword = new ReadStorePassword("readstorepassword");
    private KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);
    private KeyCreationConfig config = new KeyCreationConfig(0, 1);
    private KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, config);
    private KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

    @Test
    void testSuccessEncryptDecryptPath() {
        String testPath = "path/to/file";

        log.info("Test path: {}", testPath);

        Uri testURI = new Uri(testPath);

        SecretKeySpec secretKey = keyStoreService.getSecretKey(
                keyStoreAccess,
                KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX)
        );
        Uri encrypted = bucketPathEncryptionService.encrypt(secretKey, testURI);
        Uri decrypted = bucketPathEncryptionService.decrypt(secretKey, encrypted);

        log.debug("Encrypted path: {}", encrypted);

        assertEquals(testPath, decrypted.toASCIIString());
    }

    @Test
    void testFailEncryptPathWithWrongKeyID() throws URISyntaxException {
        String testPath = "path/to/file/";

        log.info("Test path: {}", testPath);

        Uri testURI = new Uri(testPath);

        SecretKeySpec secretKey = keyStoreService.getSecretKey(keyStoreAccess, new KeyID("Invalid key"));
        // secret keys is null, because during key obtain was used incorrect KeyID,
        // so bucketPathEncryptionService#encrypt throw BaseException(was handled NullPointerException)
        assertThrows(IllegalArgumentException.class, () -> bucketPathEncryptionService.encrypt(secretKey, testURI));
    }

    @Test
    void testFailEncryptPathWithBrokenEncryptedPath() {
        SecretKeySpec secretKey = keyStoreService.getSecretKey(
                keyStoreAccess,
                KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX)
        );

        assertThrows(BadPaddingException.class,
                () -> bucketPathEncryptionService.decrypt(secretKey,
                        new Uri(URI.create("bRQiW8qLNPEy5tO7shfV0w==/k0HooCVlmhHkQFw8mc=="))));
    }

    @Test
    void testFailEncryptPathWithTextPath() {
        SecretKeySpec secretKey = keyStoreService.getSecretKey(
                keyStoreAccess,
                KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX)
        );

        assertThrows(IllegalBlockSizeException.class,
                () -> bucketPathEncryptionService.decrypt(secretKey,
                        new Uri("/simple/text/path/")));
    }
}
