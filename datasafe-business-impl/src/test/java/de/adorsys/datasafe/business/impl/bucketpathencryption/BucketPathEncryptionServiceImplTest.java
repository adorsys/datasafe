package de.adorsys.datasafe.business.impl.bucketpathencryption;

import de.adorsys.common.exceptions.BaseException;
import de.adorsys.datasafe.business.api.deployment.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.deployment.keystore.types.*;
import de.adorsys.datasafe.business.api.encryption.bucketpathencryption.BucketPathEncryptionService;
import de.adorsys.datasafe.business.impl.keystore.service.KeyStoreServiceImpl;
import de.adorsys.datasafe.business.impl.pathencryption.BucketPathEncryptionServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class BucketPathEncryptionServiceImplTest {

    private BucketPathEncryptionService bucketPathEncryptionService = new BucketPathEncryptionServiceImpl();
    private KeyStoreService keyStoreService = new KeyStoreServiceImpl();
    private ReadKeyPassword readKeyPassword = new ReadKeyPassword("readkeypassword");
    private ReadStorePassword readStorePassword = new ReadStorePassword("readstorepassword");
    private KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);
    private KeyStoreCreationConfig config = new KeyStoreCreationConfig(1, 0, 1);
    private KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, config);
    private KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

    @Test
    public void testSuccessEncryptDecryptPath() throws URISyntaxException {
        String testPath = "path/to/file/";

        log.info("Test path: {}", testPath);

        URI testURI = new URI(testPath);

        SecretKeySpec secretKey = keyStoreService.getSecretKey(keyStoreAccess, KeyStoreCreationConfig.PATH_KEY_ID);
        URI encrypted = bucketPathEncryptionService.encrypt(secretKey, testURI);
        URI decrypted = bucketPathEncryptionService.decrypt(secretKey, encrypted);

        log.debug("Encrypted path: {}", encrypted);

        assertEquals(testPath, decrypted.toString());
    }

    @Test
    public void testFailEncryptPathWithWrongKeyID() throws URISyntaxException {
        String testPath = "path/to/file/";

        log.info("Test path: {}", testPath);

        URI testURI = new URI(testPath);

        SecretKeySpec secretKey = keyStoreService.getSecretKey(keyStoreAccess, new KeyID("Invalid key"));
        // secret keys is null, because during key obtain was used incorrect KeyID,
        // so bucketPathEncryptionService#encrypt throw BaseException(was handled NullPointerException)
        assertThrows(BaseException.class, () -> bucketPathEncryptionService.encrypt(secretKey, testURI));
    }

    @Test
    public void testFailEncryptPathWithBrokenEncryptedPath() throws URISyntaxException {
        SecretKeySpec secretKey = keyStoreService.getSecretKey(keyStoreAccess, KeyStoreCreationConfig.PATH_KEY_ID);

        assertThrows(BadPaddingException.class,
                () -> bucketPathEncryptionService.decrypt(secretKey,
                        new URI("bRQiW8qLNPEy5tO7shfV0w==/k0HooCVlmhHkQFw8mc_BROKEN_PATH")));
    }

    @Test
    public void testFailEncryptPathWithTextPath() throws URISyntaxException {
        SecretKeySpec secretKey = keyStoreService.getSecretKey(keyStoreAccess, KeyStoreCreationConfig.PATH_KEY_ID);
        assertThrows(IllegalBlockSizeException.class,
                () -> bucketPathEncryptionService.decrypt(secretKey,
                        new URI("/simple/text/path/")));
    }
}
