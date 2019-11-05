package de.adorsys.datasafe.directory.impl.profile.keys;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.storage.api.actions.StorageReadService;
import de.adorsys.datasafe.storage.api.actions.StorageWriteService;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.WithCallback;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.UnrecoverableKeyException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Common operations that are done with keystore.
 */
@Slf4j
@RuntimeDelegate
public class GenericKeystoreOperations {

    private final KeyCreationConfig config;
    private final DFSConfig dfsConfig;
    private final StorageWriteService writeService;
    private final StorageReadService readService;
    private final KeyStoreCache keystoreCache;
    private final KeyStoreService keyStoreService;

    @Inject
    public GenericKeystoreOperations(
            KeyCreationConfig config,
            DFSConfig dfsConfig,
            StorageWriteService writeService,
            StorageReadService readService,
            KeyStoreCache keystoreCache,
            KeyStoreService keyStoreService) {
        this.config = config;
        this.dfsConfig = dfsConfig;
        this.writeService = writeService;
        this.readService = readService;
        this.keystoreCache = keystoreCache;
        this.keyStoreService = keyStoreService;
    }

    public KeyStore createEmptyKeystore(UserIDAuth auth) {
        return keyStoreService.createKeyStore(
                keystoreAuth(auth),
                config.toBuilder().signKeyNumber(0).encKeyNumber(0).build()
        );
    }

    /**
     * Tries to re-read keystore from storage if supplied password can't open cached keystore.
     * Clears all keystore caches if reading operation fails.
     */
    @SneakyThrows
    public Key getKey(Supplier<KeyStore> keystore, UserIDAuth forUser, String alias) {
        try {
            return keystore.get().getKey(alias, forUser.getReadKeyPassword().getValue());
        } catch (UnrecoverableKeyException ex) {
            keystoreCache.remove(forUser.getUserID());
            return keystore.get().getKey(alias, forUser.getReadKeyPassword().getValue());
        }
    }

    /**
     * Reads aliases from keystore associated with user.
     */
    @SneakyThrows
    public Set<String> readAliases(KeyStore keystore) {
        Set<String> result = new HashSet<>();
        Enumeration<String> aliases = keystore.aliases();
        while (aliases.hasMoreElements()) {
            result.add(aliases.nextElement());
        }

        return result;
    }

    public void updateReadKeyPassword(KeyStore keystore, AbsoluteLocation location,
                                      UserIDAuth forUser, ReadKeyPassword newPassword) {
        log.debug("Updating users' '{}' keystore ReadKeyPassword", forUser.getUserID());

        KeyStoreAuth newAuth = keystoreAuth(forUser, newPassword);
        KeyStore newKeystore = keyStoreService.updateKeyStoreReadKeyPassword(
                keystore,
                keystoreAuth(forUser),
                newAuth
        );

        writeKeystore(forUser.getUserID(), newAuth, location, newKeystore);

        keystoreCache.remove(forUser.getUserID());

        log.debug("Users' '{}' keystore ReadKeyPassword updated", forUser.getUserID());
    }

    @SneakyThrows
    public KeyStore readKeyStore(UserIDAuth forUser, AbsoluteLocation location) {
        byte[] payload;
        try (InputStream is = readService.read(location)) {
            payload = ByteStreams.toByteArray(is);
        }

        return keyStoreService.deserialize(payload, dfsConfig.privateKeyStoreAuth(forUser).getReadStorePassword()
        );
    }

    public KeyStoreAuth keystoreAuth(UserIDAuth forUser) {
        return keystoreAuth(forUser, forUser.getReadKeyPassword());
    }

    @SneakyThrows
    public void writeKeystore(UserID forUser, KeyStoreAuth auth,
                              AbsoluteLocation locationWithAccess, KeyStore keystoreBlob) {
        try (OutputStream os = writeService.write(WithCallback.noCallback(locationWithAccess))) {
            os.write(keyStoreService.serialize(keystoreBlob, auth.getReadStorePassword()));
        }
        log.debug("Keystore written for user {} in path {}", forUser, locationWithAccess);
    }

    private KeyStoreAuth keystoreAuth(UserIDAuth forUser, ReadKeyPassword newPassword) {
        ReadStorePassword readStorePassword = dfsConfig.privateKeyStoreAuth(forUser).getReadStorePassword();
        return new KeyStoreAuth(readStorePassword, newPassword);
    }
}
