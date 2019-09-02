package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.keystore.*;
import de.adorsys.datasafe.encrypiton.impl.KeystoreUtil;
import de.adorsys.datasafe.encrypiton.impl.keystore.DefaultPasswordBasedKeyConfig;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImpl;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.macs.CMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.cryptomator.siv.SivMode;
import org.cryptomator.siv.UnauthenticCiphertextException;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.util.concurrent.ThreadLocalRandom;

import static de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreCreationConfig.PATH_KEY_ID_PREFIX;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class SymmetricPathEncryptionServiceImplTest extends BaseMockitoTest {

    private SymmetricPathEncryptionServiceImpl bucketPathEncryptionService = new SymmetricPathEncryptionServiceImpl(
            new DefaultPathEncryptor(new DefaultPathDigestConfig())
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
        Uri encrypted = bucketPathEncryptionService.encrypt(secretKey, testURI);
        System.out.println("Encrypted path: {}"+ encrypted);

        Uri decrypted = bucketPathEncryptionService.decrypt(secretKey, encrypted);
        System.out.println("Decrypted path: {}"+ decrypted);

        assertEquals(testPath, decrypted.toASCIIString());
    }

    @Test
    void test() {
        String testPath = "aaa/aaa";

        System.out.println("Test path: {}"+ testPath);

        Uri testURI = new Uri(testPath);

        SecretKeySpec secretKey = keyStoreService.getSecretKey(
                keyStoreAccess,
                KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX)
        );
        Uri encrypted = bucketPathEncryptionService.encrypt(secretKey, testURI);
        System.out.println("Encrypted path 1: {}"+ encrypted);
        System.out.println();

        bucketPathEncryptionService.encrypt(secretKey, new Uri("bbb/bbb"));
        encrypted = bucketPathEncryptionService.encrypt(secretKey, testURI);
        System.out.println("Encrypted path 2: {}"+ encrypted);
        System.out.println();

        Uri decrypted = bucketPathEncryptionService.decrypt(secretKey, encrypted);
        System.out.println("Decrypted path: {}"+ decrypted);

        assertEquals(testPath, decrypted.toASCIIString());
    }

    @Test
    void test2() {
        byte[] key = "key_blah_blah".getBytes();

        byte[] zeros = new byte[16];

        byte[] result = new byte[16];

        CipherParameters params = new KeyParameter(key);
        BlockCipher aes;
        aes = new AESEngine();
        CMac mac = new CMac(aes);
        mac.init(params);
        mac.update(zeros, 0, 16);
        mac.doFinal(result, 0);

        System.out.println("Result: " + new String(result));
    }

    @Test
    public void test4() throws UnauthenticCiphertextException, IllegalBlockSizeException {
        SivMode AES_SIV = new SivMode();
        SecretKeySpec secretKey = keyStoreService.getSecretKey(
                keyStoreAccess,
                KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX)
        );

        byte[] ctr = new byte[16];
        ThreadLocalRandom.current().nextBytes(ctr);

        byte[] encrypted1 = AES_SIV.encrypt(ctr, secretKey.getEncoded(), "aaa".getBytes());

        byte[] ctr2 = new byte[16];
        byte[] encrypted2 = AES_SIV.encrypt(ctr2, secretKey.getEncoded(), "aaa".getBytes());

        byte[] decrypted = AES_SIV.decrypt(ctr, secretKey.getEncoded(), encrypted1);
        byte[] decrypted2 = AES_SIV.decrypt(ctr2, secretKey.getEncoded(), encrypted2);

        assertArrayEquals(encrypted1, encrypted2);
        assertArrayEquals(decrypted2, decrypted2);

        System.out.println(new String(encrypted1) + " " + new String(decrypted));
        System.out.println(new String(encrypted2) + " " + new String(decrypted2));

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
