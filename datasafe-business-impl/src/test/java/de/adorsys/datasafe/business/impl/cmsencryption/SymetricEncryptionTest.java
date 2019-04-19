package de.adorsys.datasafe.business.impl.cmsencryption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.cms.CMSException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.adorsys.datasafe.business.api.deployment.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreAccess;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreAuth;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreCreationConfig;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreType;
import de.adorsys.datasafe.business.api.deployment.keystore.types.ReadKeyPassword;
import de.adorsys.datasafe.business.api.deployment.keystore.types.ReadStorePassword;
import de.adorsys.datasafe.business.api.deployment.keystore.types.SecretKeyIDWithKey;
import de.adorsys.datasafe.business.api.encryption.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.business.impl.cmsencryption.services.CMSEncryptionServiceImpl;
import de.adorsys.datasafe.business.impl.keystore.service.KeyStoreServiceImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SymetricEncryptionTest {

	private static final String MESSAGE_CONTENT = "message content";

	private CMSEncryptionService cmsEncryptionService = new CMSEncryptionServiceImpl();
	private KeyStoreService keyStoreService = new KeyStoreServiceImpl();
	ReadKeyPassword readKeyPassword = new ReadKeyPassword("readkeypassword");
	ReadStorePassword readStorePassword = new ReadStorePassword("readstorepassword");
	KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);
	KeyStoreCreationConfig config = new KeyStoreCreationConfig(1, 0, 1);
	KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, config);
	KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

	@Test
	@SneakyThrows
	public void symetricStreamEncryptAndDecryptTest() {
		SecretKeyIDWithKey secretKeyID = keyStoreService.getRandomSecretKeyID(keyStoreAccess);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		OutputStream encryptionStream = cmsEncryptionService.buildEncryptionOutputStream(outputStream,
				secretKeyID.getSecretKey(), secretKeyID.getKeyID());

		encryptionStream.write(MESSAGE_CONTENT.getBytes());
		encryptionStream.close();
		byte[] byteArray = outputStream.toByteArray();

		ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
		InputStream decryptionStream = cmsEncryptionService.buildDecryptionInputStream(inputStream, keyStoreAccess);
		byte[] actualResult = IOUtils.toByteArray(decryptionStream);

		assertThat(MESSAGE_CONTENT).isEqualTo(new String(actualResult));
		log.debug("en and decrypted successfully");
	}

	@Test()
	@SneakyThrows
	public void symetricNegativeStreamEncryptAndDecryptTest() {
		// THis is the keystore we use to encrypt.
		KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, config);
		KeyStoreAccess realAccess = new KeyStoreAccess(keyStore, keyStoreAuth);
		SecretKeyIDWithKey realSecretKeyID = keyStoreService.getRandomSecretKeyID(realAccess);

		// Test consist in encrypting with real secret key, but use fake secretKeyId
		SecretKeyIDWithKey fakeSecretKey = keyStoreService.getRandomSecretKeyID(keyStoreAccess);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		OutputStream encryptionStream = cmsEncryptionService.buildEncryptionOutputStream(outputStream,
				realSecretKeyID.getSecretKey(), fakeSecretKey.getKeyID());

		encryptionStream.write(MESSAGE_CONTENT.getBytes());
		encryptionStream.close();
		byte[] byteArray = outputStream.toByteArray();

		ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
		// Opening envelope with wrong key must throw a cms exception.
		Assertions.assertThrows(CMSException.class, () -> {
			cmsEncryptionService.buildDecryptionInputStream(inputStream, keyStoreAccess);
		});
	}
}
