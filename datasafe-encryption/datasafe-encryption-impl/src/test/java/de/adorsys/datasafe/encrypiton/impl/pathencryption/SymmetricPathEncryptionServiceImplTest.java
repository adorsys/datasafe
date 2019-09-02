package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import static de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreCreationConfig.PATH_KEY_ID_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

import org.cryptomator.siv.SivMode;
import org.cryptomator.siv.UnauthenticCiphertextException;
import org.junit.jupiter.api.Test;

import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.keystore.Counter;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreCreationConfig;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreType;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadStorePassword;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.encrypiton.impl.KeystoreUtil;
import de.adorsys.datasafe.encrypiton.impl.keystore.DefaultPasswordBasedKeyConfig;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImpl;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class SymmetricPathEncryptionServiceImplTest extends BaseMockitoTest {

    private SymmetricPathEncryptionServiceImpl bucketPathEncryptionService = new SymmetricPathEncryptionServiceImpl(
            new DefaultPathEncryptor()
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

        System.out.println("Test path: {}"+ testPath);

        Uri testURI = new Uri(testPath);

        SecretKeySpec secretKey = keyStoreService.getSecretKey(
                keyStoreAccess,
                KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX)
        );

        SecretKeyIDWithKey secretKeyIDWithKey = new SecretKeyIDWithKey(
                KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX), secretKey, new Counter());

        Uri encrypted = bucketPathEncryptionService.encrypt(secretKeyIDWithKey, testURI);
        log.debug("Encrypted path: {}"+ encrypted);

        Uri decrypted = bucketPathEncryptionService.decrypt(secretKeyIDWithKey, encrypted);
        log.debug("Decrypted path: {}"+ decrypted);

        assertEquals(testPath, decrypted.toASCIIString());
    }
    
    @Test
    public void test4() throws UnauthenticCiphertextException, IllegalBlockSizeException {
        SivMode AES_SIV = new SivMode();
        SecretKeySpec secretKey = keyStoreService.getSecretKey(
                keyStoreAccess,
                KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX)
        );

        byte[] ctr = new byte[16];
        Arrays.fill(ctr,(byte)'a');

//        ThreadLocalRandom.current().nextBytes(ctr);

        byte[] encrypted1 = AES_SIV.encrypt(ctr, secretKey.getEncoded(), "aaa".getBytes());

        byte[] ctr2 = new byte[16];
        Arrays.fill(ctr2,(byte)'b');
        byte[] encrypted2 = AES_SIV.encrypt(ctr2, secretKey.getEncoded(), "aaa".getBytes());

        byte[] decrypted = AES_SIV.decrypt(ctr, secretKey.getEncoded(), encrypted1);
        byte[] decrypted2 = AES_SIV.decrypt(ctr2, secretKey.getEncoded(), encrypted2);

       /* assertArrayEquals(encrypted1, encrypted2);
        assertArrayEquals(decrypted2, decrypted2);*/

        System.out.println(new String(encrypted1) + " " + new String(decrypted));
        System.out.println(new String(encrypted2) + " " + new String(decrypted2));

    }

    @Test
    void testFailEncryptPathWithWrongKeyID() throws URISyntaxException {
        String testPath = "path/to/file/";

        log.info("Test path: {}", testPath);

        Uri testURI = new Uri(testPath);

        SecretKeySpec secretKey = keyStoreService.getSecretKey(keyStoreAccess, new KeyID("Invalid key"));

        SecretKeyIDWithKey secretKeyIDWithKey = new SecretKeyIDWithKey(
                KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX), secretKey, new Counter());

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
                KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX), secretKey, new Counter());

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
                KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX), secretKey, new Counter());

        assertThrows(IllegalBlockSizeException.class,
                () -> bucketPathEncryptionService.decrypt(secretKeyIDWithKey,
                        new Uri("/simple/text/path/")));
    }
}
