package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.keystore.*;
import de.adorsys.datasafe.encrypiton.impl.KeystoreUtil;
import de.adorsys.datasafe.encrypiton.impl.keystore.DefaultPasswordBasedKeyConfig;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImpl;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.extern.slf4j.Slf4j;
import org.cryptomator.siv.SivMode;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.security.KeyStore;

import static de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreCreationConfig.PATH_KEY_ID_PREFIX_CTR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class SymmetricPathEncryptionServiceImplTest extends BaseMockitoTest {

    private SymmetricPathEncryptionServiceImpl bucketPathEncryptionService = new SymmetricPathEncryptionServiceImpl(
            new DefaultPathEncryptorDecryptor(new SivMode())
    );

    private KeyStoreService keyStoreService = new KeyStoreServiceImpl(new DefaultPasswordBasedKeyConfig());
    private ReadKeyPassword readKeyPassword = new ReadKeyPassword("readkeypassword");
    private ReadStorePassword readStorePassword = new ReadStorePassword("readstorepassword");
    private KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);
    private KeyStoreCreationConfig config = new KeyStoreCreationConfig(0, 1);
    private KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, config);
    private KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

    @Test
    void testEncryptionDoesNotLeakSameSegments() {
        String testPath = "path/to/path/file/to";

        Uri testURI = new Uri(testPath);
        PathEncryptionSecretKey pathEncryptionSecretKey = pathEncryptionSecretKey();

        Uri encrypted = bucketPathEncryptionService.encrypt(pathEncryptionSecretKey, testURI);

        String[] encryptedSegments = encrypted.asString().split("/");
        assertThat(encryptedSegments[0]).isNotEqualTo(encryptedSegments[2]);
        assertThat(encryptedSegments[1]).isNotEqualTo(encryptedSegments[4]);
    }

    @Test
    void testSuccessEncryptDecryptPath() {
        String testPath = "path/to/file";

        Uri testURI = new Uri(testPath);
        PathEncryptionSecretKey pathEncryptionSecretKey = pathEncryptionSecretKey();

        Uri encrypted = bucketPathEncryptionService.encrypt(pathEncryptionSecretKey, testURI);
        log.debug("Encrypted path: {}", encrypted);

        Uri decrypted = bucketPathEncryptionService.decrypt(pathEncryptionSecretKey, encrypted);
        log.debug("Decrypted path: {}", decrypted);

        assertEquals(testPath, decrypted.toASCIIString());
    }

    @Test
    void testFailEncryptPathWithWrongPathKeyID() {
        String testPath = "path/to/file/";

        Uri testURI = new Uri(testPath);

        PathEncryptionSecretKey correctKey = pathEncryptionSecretKey();
        PathEncryptionSecretKey toTest = new PathEncryptionSecretKey(
                new KeyID("Wrong id"),
                null,
                correctKey.getCounterKeyId(),
                correctKey.getCounterSecretKey()
        );

        assertThrows(IllegalArgumentException.class, () -> bucketPathEncryptionService.encrypt(toTest, testURI));
    }

    @Test
    void testFailEncryptPathWithWrongPathCtrKeyID() {
        String testPath = "path/to/file/";

        Uri testURI = new Uri(testPath);

        PathEncryptionSecretKey correctKey = pathEncryptionSecretKey();
        PathEncryptionSecretKey toTest = new PathEncryptionSecretKey(
                correctKey.getSecretKeyId(),
                correctKey.getSecretKey(),
                new KeyID("Wrong id"),
                null
        );

        assertThrows(IllegalArgumentException.class, () -> bucketPathEncryptionService.encrypt(toTest, testURI));
    }

    @Test
    void testFailEncryptPathWithBrokenEncryptedPath() {
        PathEncryptionSecretKey pathEncryptionSecretKey = pathEncryptionSecretKey();

        assertThrows(BadPaddingException.class,
                () -> bucketPathEncryptionService.decrypt(pathEncryptionSecretKey,
                        new Uri(URI.create("bRQiW8qLNPEy5tO7shfV0w==/k0HooCVlmhHkQFw8mc=="))));
    }

    @Test
    void testFailEncryptPathWithTextPath() {
        PathEncryptionSecretKey pathEncryptionSecretKey = pathEncryptionSecretKey();

        assertThrows(
                IllegalBlockSizeException.class,
                () -> bucketPathEncryptionService.decrypt(pathEncryptionSecretKey, new Uri("simple/text/path/"))
        );
    }

    private PathEncryptionSecretKey pathEncryptionSecretKey() {
        KeyID secretKeyId = KeystoreUtil.keyIdByPrefix(keyStore, KeyStoreCreationConfig.PATH_KEY_ID_PREFIX);
        SecretKeySpec secretKey = keyStoreService.getSecretKey(
                keyStoreAccess,
                secretKeyId
        );

        KeyID counterSecretKeyId = KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX_CTR);
        SecretKeySpec secretKeyCtr = keyStoreService.getSecretKey(
                keyStoreAccess,
                counterSecretKeyId
        );

        return new PathEncryptionSecretKey(
                secretKeyId, secretKey, counterSecretKeyId, secretKeyCtr);
    }
}
