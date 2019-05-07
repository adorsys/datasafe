package de.adorsys.datasafe.business.impl.profile.keys;

import de.adorsys.datasafe.business.api.encryption.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.business.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.storage.StorageReadService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.profile.operations.DFSSystem;

import javax.inject.Inject;

// DEPLOYMENT
/**
 * Retrieves and opens private keystore associated with user location DFS storage.
 */
public class DFSPrivateKeyServiceImpl implements PrivateKeyService {

    private final KeyStoreService keyStoreService;
    private final DFSSystem dfsSystem;
    private final BucketAccessService bucketAccessService;
    private final ProfileRetrievalService profile;
    private final StreamReadUtil streamReadUtil;
    private final StorageReadService readService;

    @Inject
    public DFSPrivateKeyServiceImpl(KeyStoreService keyStoreService, DFSSystem dfsSystem,
                                    BucketAccessService bucketAccessService, ProfileRetrievalService profile,
                                    StreamReadUtil streamReadUtil, StorageReadService readService) {
        this.keyStoreService = keyStoreService;
        this.dfsSystem = dfsSystem;
        this.bucketAccessService = bucketAccessService;
        this.profile = profile;
        this.streamReadUtil = streamReadUtil;
        this.readService = readService;
    }

    @Override
    public KeyStoreAccess keystore(UserIDAuth forUser) {
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
