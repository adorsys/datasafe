package de.adorsys.datasafe.business.impl.profile.keys;

import de.adorsys.datasafe.business.api.encryption.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.business.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.storage.StorageReadService;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.business.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.profile.operations.DFSSystem;
import lombok.SneakyThrows;

import javax.crypto.SecretKey;
import javax.inject.Inject;
import java.security.Key;
import java.util.concurrent.ConcurrentMap;

import static de.adorsys.datasafe.business.api.types.keystore.KeyStoreCreationConfig.PATH_KEY_ID;
import static de.adorsys.datasafe.business.api.types.keystore.KeyStoreCreationConfig.SYMM_KEY_ID;

// DEPLOYMENT
/**
 * Retrieves and opens private keystore associated with user location DFS storage.
 */
public class DFSPrivateKeyServiceImpl implements PrivateKeyService {

    private final ConcurrentMap<UserID, KeyStoreAccess> keystoreCache;

    private final KeyStoreService keyStoreService;
    private final DFSSystem dfsSystem;
    private final BucketAccessService bucketAccessService;
    private final ProfileRetrievalService profile;
    private final StreamReadUtil streamReadUtil;
    private final StorageReadService readService;

    @Inject
    public DFSPrivateKeyServiceImpl(ConcurrentMap<UserID, KeyStoreAccess> keystoreCache,
                                    KeyStoreService keyStoreService, DFSSystem dfsSystem,
                                    BucketAccessService bucketAccessService, ProfileRetrievalService profile,
                                    StreamReadUtil streamReadUtil, StorageReadService readService) {
        this.keystoreCache = keystoreCache;
        this.keyStoreService = keyStoreService;
        this.dfsSystem = dfsSystem;
        this.bucketAccessService = bucketAccessService;
        this.profile = profile;
        this.streamReadUtil = streamReadUtil;
        this.readService = readService;
    }

    @Override
    public SecretKeyIDWithKey pathEncryptionSecretKey(UserIDAuth forUser) {
        return new SecretKeyIDWithKey(
                PATH_KEY_ID,
                (SecretKey) keyById(forUser, PATH_KEY_ID.getValue())
        );
    }

    @Override
    public SecretKeyIDWithKey documentEncryptionSecretKey(UserIDAuth forUser) {
        return new SecretKeyIDWithKey(
                SYMM_KEY_ID,
                (SecretKey) keyById(forUser, SYMM_KEY_ID.getValue())
        );
    }

    @Override
    @SneakyThrows
    public Key keyById(UserIDAuth forUser, String keyId) {
        KeyStoreAccess access = keystoreCache.computeIfAbsent(
                forUser.getUserID(),
                userId -> keystore(forUser)
        );

        return access.getKeyStore().getKey(
                keyId,
                access.getKeyStoreAuth().getReadKeyPassword().getValue().toCharArray()
        );
    }

    private KeyStoreAccess keystore(UserIDAuth forUser) {
        AbsoluteResourceLocation<PrivateResource> access = bucketAccessService.privateAccessFor(
                forUser,
                profile.privateProfile(forUser).getKeystore()
        );

        byte[] payload = streamReadUtil.readStream(readService.read(access));

        return new KeyStoreAccess(
                keyStoreService.deserialize(
                        payload,
                        forUser.getUserID().getValue(),
                        dfsSystem.systemKeystoreAuth()
                ),
                dfsSystem.privateKeyStoreAuth(forUser)
        );
    }
}
