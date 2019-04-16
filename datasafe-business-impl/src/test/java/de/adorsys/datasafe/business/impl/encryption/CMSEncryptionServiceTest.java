package de.adorsys.datasafe.business.impl.encryption;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import javax.crypto.SecretKey;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.cms.CMSAlgorithm;
import org.junit.jupiter.api.Test;

import de.adorsys.datasafe.business.api.encryption.EncryptionService;
import de.adorsys.datasafe.business.api.encryption.EncryptionSpec;
import de.adorsys.datasafe.business.api.encryption.KeySource;
import de.adorsys.datasafe.business.api.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.keystore.types.KeyID;
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
        KeyID keyID = publicKeyIDWithPublicKey.getKeyID();
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        EncryptionSpec encryptionSpec = new EncryptionSpec(CMSAlgorithm.AES128_CBC);
		encryptionSpec.setPublicRecipients(Arrays.asList(publicKeyIDWithPublicKey));

		OutputStream encryptionStream = cmsEncryptionService.buildEncryptionOutputStream(outputStream, encryptionSpec);
		
		encryptionStream.write(MESSAGE_CONTENT.getBytes());
		encryptionStream.close();
		byte[] byteArray = outputStream.toByteArray();
		
		ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
		KeySource keySource = new KeySource() {
			
			@Override
			public SecretKey findSecretKey(byte[] keyIdentifier) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public PublicKey findPublicKey(byte[] subjectKeyId) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public PrivateKey findPrivateKey(byte[] subjectKeyId) {
				return keyStoreService.getPrivateKey(keyStoreAccess, keyID);
			}
		};
		InputStream decryptionStream = cmsEncryptionService.buildDecryptionInputStream(inputStream, keySource);
		byte[] byteArray2 = IOUtils.toByteArray(decryptionStream);

        assertThat(MESSAGE_CONTENT).isEqualTo(new String(byteArray2));
        log.debug("en and decrypted successfully");
    }
	
}
