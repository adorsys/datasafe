package de.adorsys.datasafe.encrypiton.impl.cmsencryption;

import de.adorsys.datasafe.encrypiton.api.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.encryption.CmsEncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.EncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImpl;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import de.adorsys.keymanagement.juggler.services.DaggerBCJuggler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cms.CMSException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.Enumeration;

import static de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig.DOCUMENT_KEY_ID_PREFIX;
import static de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig.PATH_KEY_ID_PREFIX;
import static de.adorsys.datasafe.encrypiton.impl.cmsencryption.KeyStoreUtil.getKeys;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class SymetricEncryptionTest extends BaseMockitoTest {

    private static final String MESSAGE_CONTENT = "message content";

    private CMSEncryptionService cmsEncryptionService = new CMSEncryptionServiceImpl(
            new ASNCmsEncryptionConfig(CmsEncryptionConfig.builder().build())
    );
    private KeyStoreService keyStoreService = new KeyStoreServiceImpl(
            EncryptionConfig.builder().build().getKeystore(),
            DaggerBCJuggler.builder().build()
    );
    private ReadKeyPassword readKeyPassword = ReadKeyPasswordTestFactory.getForString("readkeypassword");
    private ReadStorePassword readStorePassword = new ReadStorePassword("readstorepassword");
    private KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);
    private KeyCreationConfig config = KeyCreationConfig.builder().signKeyNumber(1).encKeyNumber(1).build();
    private KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, config);
    private KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

    @Test
    @SneakyThrows
    void symetricStreamEncryptAndDecryptTest() {
        KeyID keyID = keyIdByPrefix(DOCUMENT_KEY_ID_PREFIX);
        SecretKey secretKey = keyStoreService.getSecretKey(keyStoreAccess, keyID);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStream encryptionStream = cmsEncryptionService.buildEncryptionOutputStream(outputStream,
                secretKey, keyID);

        encryptionStream.write(MESSAGE_CONTENT.getBytes());
        encryptionStream.close();
        byte[] byteArray = outputStream.toByteArray();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        InputStream decryptionStream = cmsEncryptionService.buildDecryptionInputStream(
                inputStream, keyIds -> getKeys(keyIds, keyStoreAccess));

        assertThat(decryptionStream).hasContent(MESSAGE_CONTENT);
        log.debug("en and decrypted successfully");
    }

    @Test()
    @SneakyThrows
    void symetricNegativeStreamEncryptAndDecryptTest() {
        // This is the keystore we use to encrypt, it has SYMM_KEY_ID and PATH_KEY_ID symm. keys.
        keyStoreService.createKeyStore(keyStoreAuth, config);
        SecretKey realSecretKey = keyStoreService.getSecretKey(keyStoreAccess, keyIdByPrefix(DOCUMENT_KEY_ID_PREFIX));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // Test consist in encrypting with real secret key, but use fake secretKeyId - PATH_KEY_ID
        OutputStream encryptionStream = cmsEncryptionService.buildEncryptionOutputStream(outputStream,
                realSecretKey, keyIdByPrefix(PATH_KEY_ID_PREFIX));

        encryptionStream.write(MESSAGE_CONTENT.getBytes());
        encryptionStream.close();
        byte[] byteArray = outputStream.toByteArray();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        // Opening envelope with wrong key must throw a cms exception.
        Assertions.assertThrows(CMSException.class, () ->
            cmsEncryptionService.buildDecryptionInputStream(inputStream, keyIds -> getKeys(keyIds, keyStoreAccess))
        );
    }

    @SneakyThrows
    private KeyID keyIdByPrefix(String prefix) {
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String element = aliases.nextElement();
            if (element.startsWith(prefix)) {
                return new KeyID(element);
            }
        }

        throw new IllegalArgumentException("Keystore does not contain key with prefix: " + prefix);
    }
}
