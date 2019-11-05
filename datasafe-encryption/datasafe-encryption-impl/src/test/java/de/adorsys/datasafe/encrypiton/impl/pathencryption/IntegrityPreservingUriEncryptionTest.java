package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.encryption.EncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig;
import de.adorsys.datasafe.encrypiton.api.types.keystore.AuthPathEncryptionSecretKey;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.encrypiton.impl.KeystoreUtil;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImpl;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import de.adorsys.keymanagement.juggler.services.DaggerBCJuggler;
import lombok.extern.slf4j.Slf4j;
import org.cryptomator.siv.SivMode;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.security.KeyStore;

import static de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig.PATH_KEY_ID_PREFIX_CTR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class IntegrityPreservingUriEncryptionTest extends BaseMockitoTest {

    private IntegrityPreservingUriEncryption symmetricPathEncryptionService = new IntegrityPreservingUriEncryption(
            new PathSegmentEncryptorDecryptor(new SivMode())
    );

    private KeyStoreService keyStoreService = new KeyStoreServiceImpl(
            EncryptionConfig.builder().build().getKeystore(),
            DaggerBCJuggler.builder().build()
    );
    private ReadKeyPassword readKeyPassword = ReadKeyPasswordTestFactory.getForString("readkeypassword");
    private ReadStorePassword readStorePassword = new ReadStorePassword("readstorepassword");
    private KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);
    private KeyCreationConfig config = KeyCreationConfig.builder().encKeyNumber(0).signKeyNumber(1).build();
    private KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, config);
    private KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

    @Test
    void testEncryptionDoesNotLeakSameSegments() {
        String testPath = "path/to/path/file/to";

        Uri testURI = new Uri(testPath);
        AuthPathEncryptionSecretKey pathEncryptionSecretKey = pathEncryptionSecretKey();

        Uri encrypted = symmetricPathEncryptionService.encrypt(pathEncryptionSecretKey, testURI);

        String[] encryptedSegments = encrypted.asString().split("/");
        assertThat(encryptedSegments[1]).isNotEqualTo(encryptedSegments[3]);
        assertThat(encryptedSegments[2]).isNotEqualTo(encryptedSegments[5]);
    }

    @Test
    void testSuccessEncryptDecryptPath() {
        String testPath = "path/to/file";

        Uri testURI = new Uri(testPath);
        AuthPathEncryptionSecretKey pathEncryptionSecretKey = pathEncryptionSecretKey();

        Uri encrypted = symmetricPathEncryptionService.encrypt(pathEncryptionSecretKey, testURI);
        log.debug("Encrypted path: {}", encrypted);

        Uri decrypted = symmetricPathEncryptionService.decrypt(pathEncryptionSecretKey, encrypted);
        log.debug("Decrypted path: {}", decrypted);

        assertEquals(testPath, decrypted.toASCIIString());
    }

    @Test
    void testFailEncryptPathWithBrokenEncryptedPath() {
        AuthPathEncryptionSecretKey pathEncryptionSecretKey = pathEncryptionSecretKey();

        assertThrows(BadPaddingException.class,
                () -> symmetricPathEncryptionService.decrypt(pathEncryptionSecretKey,
                        new Uri(URI.create("bRQiW8qLNPEy5tO7shfV0w==/k0HooCVlmhHkQFw8mc=="))));
    }

    @Test
    void testFailEncryptPathWithTextPath() {
        AuthPathEncryptionSecretKey pathEncryptionSecretKey = pathEncryptionSecretKey();

        assertThrows(
                IllegalBlockSizeException.class,
                () -> symmetricPathEncryptionService.decrypt(pathEncryptionSecretKey, new Uri("simple/text/path/"))
        );
    }

    private AuthPathEncryptionSecretKey pathEncryptionSecretKey() {
        KeyID secretKeyId = KeystoreUtil.keyIdByPrefix(keyStore, KeyCreationConfig.PATH_KEY_ID_PREFIX);
        SecretKeySpec secretKey = keyStoreService.getSecretKey(
                keyStoreAccess,
                secretKeyId
        );

        KeyID counterSecretKeyId = KeystoreUtil.keyIdByPrefix(keyStore, PATH_KEY_ID_PREFIX_CTR);
        SecretKeySpec secretKeyCtr = keyStoreService.getSecretKey(
                keyStoreAccess,
                counterSecretKeyId
        );

        return new AuthPathEncryptionSecretKey(
                new SecretKeyIDWithKey(secretKeyId, secretKey),
                new SecretKeyIDWithKey(counterSecretKeyId, secretKeyCtr)
        );
    }
}
