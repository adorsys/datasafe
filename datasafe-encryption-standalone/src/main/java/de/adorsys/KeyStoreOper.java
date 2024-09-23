package de.adorsys;

import de.adorsys.config.Properties;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.encryption.EncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.keystore.*;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImpl;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import de.adorsys.keymanagement.juggler.services.DaggerBCJuggler;
import lombok.SneakyThrows;

import javax.crypto.SecretKey;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig.DOCUMENT_KEY_ID_PREFIX;


public class KeyStoreOper {
    private KeyStore keyStore;

    private KeyStoreService keyStoreService = new KeyStoreServiceImpl(
            EncryptionConfig.builder().build().getKeystore(),
            DaggerBCJuggler.builder().build()
    );
    ;
    private KeyStoreAuth keyStoreAuth;

    private KeyStoreAccess keyStoreAccess;
    private Properties properties;

    private ReadStorePassword readStorePassword;

    private ReadKeyPassword readKeyPassword;

    public KeyStoreOper() {
    }

    public void createKeyStore(String readStorePass, String readKeyPass) {
        readStorePassword = new ReadStorePassword(readStorePass);
        readKeyPassword = new ReadKeyPassword(readKeyPass.toCharArray());
        keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);
        keyStore = keyStoreService.createKeyStore(keyStoreAuth, properties.getKeyCreationConfigEC());
        keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);
    }

    public List<PublicKeyIDWithPublicKey> getPublicKey() {
        return keyStoreService.getPublicKeys(keyStoreAccess);
    }

    @SneakyThrows
    public PrivateKey getPrivateKey() {
        List<String> aliases = Collections.list(keyStore.aliases());
        KeyID keyID = new KeyID(aliases.get(0));
        return keyStoreService.getPrivateKey(keyStoreAccess, keyID);
    }

    @SneakyThrows
    public SecretKeyIDWithKey getSecretKey() {
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);
        KeyID keyID = KeyIDByPrefix();
        SecretKey secretKey = keyStoreService.getSecretKey(keyStoreAccess, keyID);
        return new SecretKeyIDWithKey(keyID, secretKey);
    }

    @SneakyThrows
    private KeyID KeyIDByPrefix() {
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String element = aliases.nextElement();
            if (element.startsWith(DOCUMENT_KEY_ID_PREFIX)) {
                return new KeyID(element);
            }
        }
        throw new IllegalArgumentException("Keystore does not contain key with prefix: " + DOCUMENT_KEY_ID_PREFIX);
    }
}
