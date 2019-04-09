package de.adorsys.docusafe2.business.impl.keystore.service;

import de.adorsys.common.exceptions.BaseException;
import de.adorsys.docusafe2.business.api.keystore.KeyStoreService;
import de.adorsys.docusafe2.business.api.keystore.types.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.SecretKey;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.util.Collections;
import java.util.List;

public class KeyStoreServiceTest {

    private KeyStoreService keyStoreService = new KeyStoreServiceImpl();
    private KeyStoreAuth keyStoreAuth;

    @Before
    public void setUp() throws Exception {
        ReadStorePassword readStorePassword = new ReadStorePassword("keystorepass");
        ReadKeyPassword readKeyPassword = new ReadKeyPassword("keypass");
        keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);
    }

    @Test
    public void createKeyStore() throws Exception {
        KeyStoreCreationConfig config = new KeyStoreCreationConfig(1, 0, 1);
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, config);

        Assert.assertNotNull(keyStore);

        List<String> list = Collections.list(keyStore.aliases());
        Assert.assertEquals(2, list.size());

        Assert.assertEquals("UBER", keyStore.getType());
        Assert.assertEquals(Security.getProvider("BC"), keyStore.getProvider());

//        String firstEntryAlias = keyStore.aliases().nextElement();
//        //KeyStore.PasswordProtection protParam = new KeyStore.PasswordProtection(readStorePassword.getValue().toCharArray());
//        KeyStore.PasswordProtection protParam = new KeyStore.PasswordProtection("".toCharArray());
//        KeyStore.Entry firstEntry = keyStore.getEntry(firstEntryAlias, protParam);
//        Assert.assertTrue(firstEntry instanceof KeyStore.PrivateKeyEntry);
//
//        String secondEntryAlias = keyStore.aliases().nextElement();
//        KeyStore.Entry secondEntry = keyStore.getEntry(secondEntryAlias, null);
//        Assert.assertTrue(secondEntry instanceof KeyStore.TrustedCertificateEntry);
    }

    @Test
    public void createKeyStoreEmptyConfig() throws Exception {
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, null);

        Assert.assertNotNull(keyStore);

        List<String> list = Collections.list(keyStore.aliases());

        Assert.assertEquals(15, list.size());
    }

    @Test(expected = BaseException.class)
    public void createKeyStoreException() {
        KeyStoreCreationConfig config = new KeyStoreCreationConfig(0, 0, 0);
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, config);
    }

    @Test
    public void getPublicKeys() {
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, null);
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);
        List<PublicKeyIDWithPublicKey> publicKeys = keyStoreService.getPublicKeys(keyStoreAccess);
        Assert.assertEquals(5, publicKeys.size());
    }

    @Test
    public void getPrivateKey() throws Exception {
        KeyStoreCreationConfig config = new KeyStoreCreationConfig(1, 0, 0);
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, config);

//        KeyStore.Entry privateKeyEntry = new KeyStore.PrivateKeyEntry();
//        KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection("123".toCharArray());
//        keyStore.setEntry("privateTest", privateKeyEntry, protParam);
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

        String keyID = keyStore.aliases().nextElement();
        PrivateKey privateKey = keyStoreService.getPrivateKey(keyStoreAccess, new KeyID(keyID));
        System.out.println(privateKey);
        //Assert.assertEquals(privateKey.);
    }

    @Test(expected = BaseException.class)
    public void getPrivateKeyException() throws Exception {
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, null);
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);
        List<String> list = Collections.list(keyStore.aliases());
        for(String id : list) {
            System.out.println(keyStoreService.getPrivateKey(keyStoreAccess, new KeyID(id)));
        }
    }

    @Test
    public void getSecretKey() throws Exception {
        KeyStoreCreationConfig config = new KeyStoreCreationConfig(0, 0, 1);
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, config);
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

        String keyID = keyStore.aliases().nextElement();
        SecretKey secretKey = keyStoreService.getSecretKey(keyStoreAccess, new KeyID(keyID));

    }

    @Test
    public void getRandomSecretKeyID() {
        KeyStoreCreationConfig config = new KeyStoreCreationConfig(10, 10, 1);
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, config);
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);
        SecretKeyIDWithKey randomSecretKeyID = keyStoreService.getRandomSecretKeyID(keyStoreAccess);
        Assert.assertNotNull(randomSecretKeyID);
    }

    @Test(expected = BaseException.class)
    public void getRandomSecretKeyIDException() {
        KeyStoreCreationConfig config = new KeyStoreCreationConfig(10, 10, 0);
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, config);
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);
        keyStoreService.getRandomSecretKeyID(keyStoreAccess);
    }
}