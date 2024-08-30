package de.adorsys;

import de.adorsys.config.Properties;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.encryption.EncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImpl;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import de.adorsys.keymanagement.juggler.services.DaggerBCJuggler;

import javax.crypto.SecretKey;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.List;


public class Keystore {
    private KeyStore keyStore;

    private KeyStoreService keystoreService = new KeyStoreServiceImpl(
            EncryptionConfig.builder().build().getKeystore(),
            DaggerBCJuggler.builder().build()
    );;
    private KeyStoreAuth keyStoreAuth;

    private KeyStoreAccess keyStoreAccess;
    private Properties properties;
    public Keystore(){}

    public void createKeyStore(String readStorePass, String readKeyPass) {
        ReadStorePassword readStorePassword = new ReadStorePassword(readStorePass);
        ReadKeyPassword readKeyPassword = new ReadKeyPassword(readKeyPass.toCharArray());
        keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);
        keyStore = keystoreService.createKeyStore(keyStoreAuth, properties.getKeyCreationConfig());
        keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);
    }
    public List<PublicKeyIDWithPublicKey> getPublicKey () {
        return keystoreService.getPublicKeys(keyStoreAccess);
    }
    public PrivateKey getPrivateKey() throws KeyStoreException {
        List<String> aliases = Collections.list(keyStore.aliases());
        KeyID keyID = new KeyID(aliases.get(0));
        return keystoreService.getPrivateKey(keyStoreAccess, keyID);
    }

    public SecretKey getSecretKey () throws KeyStoreException {
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);
        KeyID keyID = new KeyID("PRIVATE-SECRET");
        return keystoreService.getSecretKey(keyStoreAccess, keyID);
    }
}
