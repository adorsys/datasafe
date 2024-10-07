package de.adorsys;

import de.adorsys.config.Properties;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.encryption.EncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig;
import de.adorsys.datasafe.encrypiton.api.types.keystore.*;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImpl;
import de.adorsys.datasafe.storage.api.actions.StorageWriteService;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.WithCallback;
import de.adorsys.keymanagement.juggler.services.DaggerBCJuggler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Enumeration;
import java.util.List;

import static de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig.DOCUMENT_KEY_ID_PREFIX;

@Slf4j
public class KeyStoreOper {
    private KeyStore keyStore;

    private KeyStoreService keyStoreService = new KeyStoreServiceImpl(
            EncryptionConfig.builder().build().getKeystore(),
            DaggerBCJuggler.builder().build()
    );
    private KeyStoreAuth keyStoreAuth;
    private StorageWriteService writeService;
    private KeyCreationConfig keyCreationConfig;

    private KeyStoreAccess keyStoreAccess;
    private Properties properties;
    private DFSConfig config;

    public KeyStoreOper(Properties properties, DFSConfig config, StorageWriteService writeService, KeyCreationConfig keyCreationConfig) {
        this.properties = properties;
        this.config = config;
        this.writeService = writeService;
        this.keyCreationConfig = keyCreationConfig;
    }

    public void createKeyStore(UserPrivateProfile userProfile, UserIDAuth user) {
        keyStoreAuth = new KeyStoreAuth(config.privateKeyStoreAuth(user).getReadStorePassword(), user.getReadKeyPassword());
        keyStore = keyStoreService.createKeyStore(keyStoreAuth, keyCreationConfig);
        writeKeystore(userProfile, user, keyStore, keyStoreAuth);
        keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);
    }

    @SneakyThrows
    private void writeKeystore(UserPrivateProfile userPrivateProfile, UserIDAuth user, KeyStore keystore, KeyStoreAuth auth) {
        PrivateResource resource = userPrivateProfile.getKeystore().getResource();
        AbsoluteLocation<PrivateResource> location = new AbsoluteLocation<>(resource);

        try (OutputStream os = writeService.write(WithCallback.noCallback(location))) {
            os.write(keyStoreService.serialize(keystore, auth.getReadStorePassword()));
        }
        log.debug("Keystore created for user {} in path {}", user, keystore);
    }

    public List<PublicKeyIDWithPublicKey> getPublicKey() {
        return keyStoreService.getPublicKeys(keyStoreAccess);
    }

    @SneakyThrows
    public PrivateKey getPrivateKey(UserIDAuth user) {
        String alias = getPublicKey().get(0).getKeyID().getValue();
        return keyStoreService.getPrivateKey(keyStoreAccess, new KeyID(alias));
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