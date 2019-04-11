package de.adorsys.datasafe.business.impl.cmsencryption;

import de.adorsys.datasafe.business.impl.cmsencryption.services.CMSEncryptionServiceImpl;
import de.adorsys.datasafe.business.impl.keystore.service.KeyStoreServiceImpl;
import de.adorsys.datasafe.business.api.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.business.api.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.types.DocumentContent;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.junit.jupiter.api.Test;

import java.security.KeyStore;
import java.security.PublicKey;

@Slf4j
public class CmsEncryptionServiceTest {

        private CMSEncryptionService cmsEncryptionService = new CMSEncryptionServiceImpl();
        private KeyStoreService keyStoreService = new KeyStoreServiceImpl();

        @Test
        public void cmsEnvelopeEncryptAndDecryptTest() {

            ReadKeyPassword readKeyPassword = new ReadKeyPassword("readkeypassword");
            ReadStorePassword readStorePassword = new ReadStorePassword("readstorepassword");
            KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);

            KeyStoreCreationConfig config = new KeyStoreCreationConfig(1, 0, 1);
            KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, config);
            KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

            PublicKeyIDWithPublicKey publicKeyIDWithPublicKey = keyStoreService.getPublicKeys(keyStoreAccess).get(0);
            PublicKey publicKey = publicKeyIDWithPublicKey.getPublicKey();
            KeyID keyID = publicKeyIDWithPublicKey.getKeyID();

            DocumentContent origMessage = new DocumentContent("message content".getBytes());
            CMSEnvelopedData encrypted  = cmsEncryptionService.encrypt(origMessage, publicKey, keyID);
            DocumentContent decrypted = cmsEncryptionService.decrypt(encrypted, keyStoreAccess);

            assertArrayEquals(origMessage.getValue(), decrypted.getValue());
            log.debug("en and decrypted successfully");
        }
    }
