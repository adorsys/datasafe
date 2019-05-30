package de.adorsys.datasafe.directory.impl.profile.keys;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.directory.impl.profile.operations.DFSSystem;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.storage.api.actions.StorageReadService;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import lombok.SneakyThrows;

import javax.crypto.SecretKey;
import javax.inject.Inject;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;

import static de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreCreationConfig.PATH_KEY_ID;
import static de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreCreationConfig.SYMM_KEY_ID;

/**
 * Retrieves and opens private keystore associated with user location DFS storage.
 */
public class DFSPrivateKeyServiceImpl implements PrivateKeyService {

    private final KeyStoreCache keystoreCache;
    private final KeyStoreService keyStoreService;
    private final DFSSystem dfsSystem;
    private final BucketAccessService bucketAccessService;
    private final ProfileRetrievalService profile;
    private final StorageReadService readService;

    @Inject
    public DFSPrivateKeyServiceImpl(KeyStoreCache keystoreCache,
                                    KeyStoreService keyStoreService, DFSSystem dfsSystem,
                                    BucketAccessService bucketAccessService, ProfileRetrievalService profile,
                                    StorageReadService readService) {
        this.keystoreCache = keystoreCache;
        this.keyStoreService = keyStoreService;
        this.dfsSystem = dfsSystem;
        this.bucketAccessService = bucketAccessService;
        this.profile = profile;
        this.readService = readService;
    }

    /**
     * Reads path encryption secret key from DFS and caches the result.
     */
    @Override
    public SecretKeyIDWithKey pathEncryptionSecretKey(UserIDAuth forUser) {
        return new SecretKeyIDWithKey(
                PATH_KEY_ID,
                (SecretKey) keyById(forUser, PATH_KEY_ID.getValue())
        );
    }

    /**
     * Reads document encryption secret key from DFS and caches the result.
     */
    @Override
    public SecretKeyIDWithKey documentEncryptionSecretKey(UserIDAuth forUser) {
        return new SecretKeyIDWithKey(
                SYMM_KEY_ID,
                (SecretKey) keyById(forUser, SYMM_KEY_ID.getValue())
        );
    }

    /**
     * Reads private or secret key from DFS and caches the keystore associated with it.
     */
    @Override
    @SneakyThrows
    public Key keyById(UserIDAuth forUser, String keyId) {
        KeyStore keyStore = keystoreCache.getKeystore().computeIfAbsent(
                forUser.getUserID(),
                userId -> keystore(forUser)
        );

        return keyStore.getKey(
                keyId,
                forUser.getReadKeyPassword().getValue().toCharArray()
        );
    }

    @SneakyThrows
    private KeyStore keystore(UserIDAuth forUser) {
        AbsoluteLocation<PrivateResource> access = bucketAccessService.privateAccessFor(
                forUser,
                profile.privateProfile(forUser).getKeystore()
        );

        byte[] payload;
        try (InputStream is = readService.read(access)) {
            payload = ByteStreams.toByteArray(is);
        }

        return keyStoreService.deserialize(
                payload,
                forUser.getUserID().getValue(),
                dfsSystem.privateKeyStoreAuth(forUser).getReadStorePassword()
        );
    }
}
