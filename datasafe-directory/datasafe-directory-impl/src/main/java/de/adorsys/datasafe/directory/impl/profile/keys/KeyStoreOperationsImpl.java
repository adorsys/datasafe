package de.adorsys.datasafe.directory.impl.profile.keys;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.keys.KeyStoreOperations;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.*;
import de.adorsys.datasafe.storage.api.actions.StorageReadService;
import de.adorsys.datasafe.storage.api.actions.StorageWriteService;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResourceLocation;
import de.adorsys.datasafe.types.api.resource.WithCallback;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.UnrecoverableKeyException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides different kinds of high-level keystore operations.
 */
@Slf4j
@RuntimeDelegate
public class KeyStoreOperationsImpl implements KeyStoreOperations {

    private final DFSConfig dfsConfig;
    private final BucketAccessService access;
    private final ProfileRetrievalService profile;
    private final StorageReadService readService;
    private final StorageWriteService writeService;
    private final KeyStoreCache keystoreCache;
    private final KeyStoreService keyStoreService;

    @Inject
    public KeyStoreOperationsImpl(DFSConfig dfsConfig, BucketAccessService access, ProfileRetrievalService profile,
                                  StorageReadService readService, StorageWriteService writeService,
                                  KeyStoreCache keystoreCache, KeyStoreService keyStoreService) {
        this.dfsConfig = dfsConfig;
        this.access = access;
        this.profile = profile;
        this.readService = readService;
        this.writeService = writeService;
        this.keystoreCache = keystoreCache;
        this.keyStoreService = keyStoreService;
    }

    /**
     * Tries to re-read keystore from storage if supplied password can't open cached keystore.
     */
    @Override
    @SneakyThrows
    public Key getKey(UserIDAuth forUser, String alias) {
        try {
            return keyStore(forUser).getKey(alias, forUser.getReadKeyPassword().getValue().toCharArray());
        } catch (UnrecoverableKeyException ex) {
            keystoreCache.getKeystore().remove(forUser.getUserID());
            keystoreCache.getPublicKeys().remove(forUser.getUserID());

            return keyStore(forUser).getKey(alias, forUser.getReadKeyPassword().getValue().toCharArray());
        }
    }

    /**
     * Reads aliases from keystore associated with user.
     */
    @Override
    @SneakyThrows
    public Set<String> readAliases(UserIDAuth forUser) {
        Set<String> result = new HashSet<>();
        Enumeration<String> aliases = keyStore(forUser).aliases();
        while (aliases.hasMoreElements()) {
            result.add(aliases.nextElement());
        }

        return result;
    }

    @Override
    @SneakyThrows
    public <T extends ResourceLocation<T>> List<PublicKeyIDWithPublicKey> createAndWriteKeyStore(
            UserIDAuth forUser) {
        KeyStoreAuth auth = keystoreAuth(forUser, forUser.getReadKeyPassword());
        KeyStore keystoreBlob = keyStoreService.createKeyStore(
                auth,
                KeyStoreType.DEFAULT,
                new KeyStoreCreationConfig(1, 1)
        );

        writeKeystore(forUser.getUserID(), auth, keystoreLocationWithAccess(forUser), keystoreBlob);
        return keyStoreService.getPublicKeys(new KeyStoreAccess(keystoreBlob, auth));
    }

    @Override
    public void updateReadKeyPassword(UserIDAuth forUser, ReadKeyPassword newPassword) {
        log.debug("Updating users' '{}' keystore ReadKeyPassword", forUser.getUserID());

        KeyStoreAuth newAuth = keystoreAuth(forUser, newPassword);
        KeyStore newKeystore = keyStoreService.updateKeyStoreReadKeyPassword(
                readKeyStore(forUser),
                keystoreAuth(forUser, forUser.getReadKeyPassword()),
                newAuth
        );

        AbsoluteLocation<PrivateResource> location = keystoreLocationWithAccess(forUser);

        writeKeystore(forUser.getUserID(), newAuth, location, newKeystore);

        keystoreCache.getKeystore().remove(forUser.getUserID());
        keystoreCache.getPublicKeys().remove(forUser.getUserID());

        log.debug("Users' '{}' keystore ReadKeyPassword updated", forUser.getUserID());
    }

    private AbsoluteLocation<PrivateResource> keystoreLocationWithAccess(UserIDAuth forUser) {
        return this.access.privateAccessFor(
                forUser,
                profile.privateProfile(forUser).getKeystore().getResource()
        );
    }

    private KeyStoreAuth keystoreAuth(UserIDAuth forUser, ReadKeyPassword readKeyPassword) {
        ReadStorePassword readStorePassword = dfsConfig.privateKeyStoreAuth(forUser).getReadStorePassword();

        return new KeyStoreAuth(readStorePassword, readKeyPassword);
    }

    @SneakyThrows
    private <T extends ResourceLocation<T>> void writeKeystore(UserID forUser, KeyStoreAuth auth,
                                                               AbsoluteLocation<T> keystore, KeyStore keystoreBlob) {
        try (OutputStream os = writeService.write(WithCallback.noCallback(access.withSystemAccess(keystore)))) {
            os.write(keyStoreService.serialize(keystoreBlob, forUser.getValue(), auth.getReadStorePassword()));
        }
        log.debug("Keystore created for user {} in path {}", forUser, keystore);
    }

    private KeyStore keyStore(UserIDAuth forUser) {
        return keystoreCache.getKeystore().computeIfAbsent(
                forUser.getUserID(),
                userId -> readKeyStore(forUser)
        );
    }

    @SneakyThrows
    private KeyStore readKeyStore(UserIDAuth forUser) {
        AbsoluteLocation<PrivateResource> location = keystoreLocationWithAccess(forUser);

        byte[] payload;
        try (InputStream is = readService.read(location)) {
            payload = ByteStreams.toByteArray(is);
        }

        return keyStoreService.deserialize(
                payload,
                forUser.getUserID().getValue(),
                dfsConfig.privateKeyStoreAuth(forUser).getReadStorePassword()
        );
    }
}
