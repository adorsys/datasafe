package de.adorsys.datasafe.business.impl.profile.keys;

import de.adorsys.datasafe.business.api.encryption.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.business.api.profile.keys.PublicKeyService;
import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.storage.StorageReadService;
import de.adorsys.datasafe.business.api.version.types.UserID;
import de.adorsys.datasafe.business.api.version.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.business.api.version.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.business.api.version.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.business.api.version.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.version.types.resource.PublicResource;
import de.adorsys.datasafe.business.impl.profile.operations.DFSSystem;

import javax.inject.Inject;
import java.security.KeyStore;

/**
 * Retrieves and opens public keystore associated with user location DFS storage.
 */
public class DFSPublicKeyServiceImpl implements PublicKeyService {

    private final KeyStoreCache keystoreCache;
    private final DFSSystem dfsSystem;
    private final KeyStoreService keyStoreService;
    private final BucketAccessService bucketAccessService;
    private final ProfileRetrievalService profiles;
    private final StreamReadUtil streamReadUtil;
    private final StorageReadService readService;

    @Inject
    public DFSPublicKeyServiceImpl(KeyStoreCache keystoreCache,
                                   DFSSystem dfsSystem, KeyStoreService keyStoreService,
                                   BucketAccessService bucketAccessService, ProfileRetrievalService profiles,
                                   StreamReadUtil streamReadUtil, StorageReadService readService) {
        this.keystoreCache = keystoreCache;
        this.dfsSystem = dfsSystem;
        this.keyStoreService = keyStoreService;
        this.bucketAccessService = bucketAccessService;
        this.profiles = profiles;
        this.streamReadUtil = streamReadUtil;
        this.readService = readService;
    }

    @Override
    public PublicKeyIDWithPublicKey publicKey(UserID forUser) {
        KeyStoreAuth publicAuth = dfsSystem.publicKeyStoreAuth();

        KeyStore keyStore = keystoreCache.getPublicKeys().computeIfAbsent(
                forUser,
                id -> keystore(forUser, publicAuth)
        );

        return keyStoreService.getPublicKeys(new KeyStoreAccess(keyStore, publicAuth)).get(0);
    }

    private KeyStore keystore(UserID forUser, KeyStoreAuth publicAuth) {
        AbsoluteResourceLocation<PublicResource> accessiblePublicKey = bucketAccessService.publicAccessFor(
                forUser,
                profiles.publicProfile(forUser).getPublicKeys()
        );

        byte[] payload = streamReadUtil.readStream(readService.read(accessiblePublicKey));

        return keyStoreService.deserialize(
                payload,
                forUser.getValue(),
                publicAuth.getReadStorePassword()
        );
    }
}
