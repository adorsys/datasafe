package de.adorsys.datasafe.business.impl.credentials;

import de.adorsys.datasafe.business.api.deployment.credentials.BucketAccessService;
import de.adorsys.datasafe.business.api.deployment.dfs.DFSConnectionService;
import de.adorsys.datasafe.business.api.deployment.keystore.PrivateKeyService;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreAccess;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.dfs.connection.api.service.api.DFSConnection;

import javax.inject.Inject;

// DEPLOYMENT
/**
 * Retrieves and opens private keystore associated with user from DFS storage.
 */
public class DFSPrivateKeyServiceImpl implements PrivateKeyService {

    private final DFSConnectionService dfsConnectionService;
    private final BucketAccessService bucketAccessService;

    @Inject
    public DFSPrivateKeyServiceImpl(DFSConnectionService dfsConnectionService, BucketAccessService bucketAccessService) {
        this.dfsConnectionService = dfsConnectionService;
        this.bucketAccessService = bucketAccessService;
    }

    @Override
    public KeyStoreAccess keystore(UserIDAuth forUser) {
        DFSAccess access = bucketAccessService.privateAccessFor(
            forUser,
            profile -> profile.privateProfile(forUser).getKeystore()
        );

        DFSConnection connection = dfsConnectionService.obtain(access);
        return null;
    }
}
