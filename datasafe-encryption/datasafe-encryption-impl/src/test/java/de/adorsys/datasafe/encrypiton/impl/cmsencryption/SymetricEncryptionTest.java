package de.adorsys.datasafe.encrypiton.impl.cmsencryption;

import de.adorsys.datasafe.encrypiton.api.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.keystore.*;
import de.adorsys.datasafe.encrypiton.impl.WithBouncyCastle;
import de.adorsys.datasafe.encrypiton.impl.keystore.DefaultPasswordBasedKeyConfig;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImpl;
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

import static de.adorsys.datasafe.encrypiton.api.types.keystore.KeyCreationConfig.PATH_KEY_ID_PREFIX;
import static de.adorsys.datasafe.encrypiton.api.types.keystore.KeyCreationConfig.DOCUMENT_KEY_ID_PREFIX;
import static de.adorsys.datasafe.encrypiton.impl.cmsencryption.KeyStoreUtil.getKeys;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class SymetricEncryptionTest extends WithBouncyCastle {

    private static final String MESSAGE_CONTENT = "message content";

    private CMSEncryptionService cmsEncryptionService = new CMSEncryptionServiceImpl(new DefaultCMSEncryptionConfig());
    private KeyStoreService keyStoreService = new KeyStoreServiceImpl(new DefaultPasswordBasedKeyConfig());
    private ReadKeyPassword readKeyPassword = new ReadKeyPassword("readkeypassword");
    private ReadStorePassword readStorePassword = new ReadStorePassword("readstorepassword");
    private KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);
    private KeyCreationConfig config = new KeyCreationConfig(1, 1);
    private KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreCreationConfig.DEFAULT, config);
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
        keyStoreService.createKeyStore(keyStoreAuth, KeyStoreCreationConfig.DEFAULT, config);
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
