package de.adorsys.datasafe.business.impl.profile.keys;

import de.adorsys.datasafe.business.api.encryption.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.business.api.profile.keys.PublicKeyService;
import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.storage.StorageReadService;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.business.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;
import de.adorsys.datasafe.business.impl.profile.operations.DFSSystem;

import javax.inject.Inject;
import java.security.KeyStore;
import java.util.List;

// DEPLOYMENT
/**
 * Retrieves and opens public keystore associated with user location DFS storage.
 */
public class DFSPublicKeyServiceImpl implements PublicKeyService {

    private final DFSSystem dfsSystem;
    private final KeyStoreService keyStoreService;
    private final BucketAccessService bucketAccessService;
    private final ProfileRetrievalService profiles;
    private final StreamReadUtil streamReadUtil;
    private final StorageReadService readService;

    @Inject
    public DFSPublicKeyServiceImpl(DFSSystem dfsSystem, KeyStoreService keyStoreService,
                                   BucketAccessService bucketAccessService, ProfileRetrievalService profiles,
                                   StreamReadUtil streamReadUtil, StorageReadService readService) {
        this.dfsSystem = dfsSystem;
        this.keyStoreService = keyStoreService;
        this.bucketAccessService = bucketAccessService;
        this.profiles = profiles;
        this.streamReadUtil = streamReadUtil;
        this.readService = readService;
    }

    @Override
    public PublicKeyIDWithPublicKey publicKey(UserID forUser) {
        AbsoluteResourceLocation<PublicResource> accessiblePublicKey = bucketAccessService.publicAccessFor(
            forUser,
            profiles.publicProfile(forUser).getPublicKeys()
        );

        KeyStoreAuth publicAuth = dfsSystem.publicKeyStoreAuth();

        byte[] payload = streamReadUtil.readStream(readService.read(accessiblePublicKey));

        KeyStore keyStore = keyStoreService.deserialize(payload, forUser.getValue(), publicAuth.getReadStorePassword());

        List<PublicKeyIDWithPublicKey> publicKeyList = keyStoreService.getPublicKeys(
            new KeyStoreAccess(
                keyStore,
                publicAuth
            )
        );

        return publicKeyList.get(0);
    }
}
