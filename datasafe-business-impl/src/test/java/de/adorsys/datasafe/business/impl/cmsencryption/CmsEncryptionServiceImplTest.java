package de.adorsys.datasafe.business.impl.cmsencryption;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.PublicKey;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.junit.jupiter.api.Test;

import de.adorsys.datasafe.business.api.deployment.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyID;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreAccess;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreAuth;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreCreationConfig;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreType;
import de.adorsys.datasafe.business.api.deployment.keystore.types.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.business.api.deployment.keystore.types.ReadKeyPassword;
import de.adorsys.datasafe.business.api.deployment.keystore.types.ReadStorePassword;
import de.adorsys.datasafe.business.api.encryption.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.business.api.types.DocumentContent;
import de.adorsys.datasafe.business.impl.cmsencryption.services.CMSEncryptionServiceImpl;
import de.adorsys.datasafe.business.impl.keystore.service.KeyStoreServiceImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CmsEncryptionServiceImplTest {

	private CMSEncryptionService cmsEncryptionService = new CMSEncryptionServiceImpl();
	private KeyStoreService keyStoreService = new KeyStoreServiceImpl();
    private static final String MESSAGE_CONTENT = "message content";

	@Test
	@SneakyThrows
	public void cmsStreamEnvelopeEncryptAndDecryptTest() {
		ReadKeyPassword readKeyPassword = new ReadKeyPassword("readkeypassword");
		ReadStorePassword readStorePassword = new ReadStorePassword("readstorepassword");
		KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);

		KeyStoreCreationConfig config = new KeyStoreCreationConfig(1, 0, 1);
		KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, config);

		KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

		PublicKeyIDWithPublicKey publicKeyIDWithPublicKey = keyStoreService.getPublicKeys(keyStoreAccess).get(0);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		OutputStream encryptionStream = cmsEncryptionService.buildEncryptionOutputStream(outputStream,
				publicKeyIDWithPublicKey.getPublicKey(), publicKeyIDWithPublicKey.getKeyID());

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
