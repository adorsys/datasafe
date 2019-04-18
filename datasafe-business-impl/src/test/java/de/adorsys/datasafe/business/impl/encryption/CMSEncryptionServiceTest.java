package de.adorsys.datasafe.business.impl.encryption;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.*;
import java.security.KeyStore;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import de.adorsys.datasafe.business.api.encryption.EncryptionService;
import de.adorsys.datasafe.business.api.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.keystore.types.KeyStoreAccess;
import de.adorsys.datasafe.business.api.keystore.types.KeyStoreAuth;
import de.adorsys.datasafe.business.api.keystore.types.KeyStoreCreationConfig;
import de.adorsys.datasafe.business.api.keystore.types.KeyStoreType;
import de.adorsys.datasafe.business.api.keystore.types.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.business.api.keystore.types.ReadKeyPassword;
import de.adorsys.datasafe.business.api.keystore.types.ReadStorePassword;
import de.adorsys.datasafe.business.impl.keystore.service.KeyStoreServiceImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class CMSEncryptionServiceTest {

    private static final String MESSAGE_CONTENT = "message content";
    private EncryptionService cmsEncryptionService = new CMSEncryptionService();
    private KeyStoreService keyStoreService = new KeyStoreServiceImpl();

    @Test
    @SneakyThrows
    public void cmsEnvelopeEncryptAndDecryptTest() {
        ReadKeyPassword readKeyPassword = new ReadKeyPassword("readkeypassword");
        ReadStorePassword readStorePassword = new ReadStorePassword("readstorepassword");
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);

        KeyStoreCreationConfig config = new KeyStoreCreationConfig(1, 0, 1);
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, config);

        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

        PublicKeyIDWithPublicKey publicKeyIDWithPublicKey = keyStoreService.getPublicKeys(keyStoreAccess).get(0);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        OutputStream encryptionStream = cmsEncryptionService.buildEncryptionOutputStream(outputStream, publicKeyIDWithPublicKey);

        encryptionStream.write(MESSAGE_CONTENT.getBytes());
        encryptionStream.close();
        byte[] byteArray = outputStream.toByteArray();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        InputStream decryptionStream = cmsEncryptionService.buildDecryptionInputStream(inputStream, keyStoreAccess);
        byte[] actualResult = IOUtils.toByteArray(decryptionStream);

        assertThat(MESSAGE_CONTENT).isEqualTo(new String(actualResult));
        log.debug("en and decrypted successfully");
    }

}
