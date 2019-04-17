package de.adorsys.datasafe.business.impl.credentials;

import de.adorsys.datasafe.business.api.deployment.credentials.DFSCredentialsService;
import de.adorsys.datasafe.business.api.deployment.credentials.dto.DFSCredentials;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.impl.profile.DFSSystem;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;

import javax.inject.Inject;

// DEPLOYMENT
/**
 * Retrieves DFS credentials associated with some resource (i.e. s3 credentials for private folder access).
 */
public class DFSCredentialsServiceImpl implements DFSCredentialsService {

    private final DFSSystem dfsSystem;

    @Inject
    public DFSCredentialsServiceImpl(DFSSystem dfsSystem) {
        this.dfsSystem = dfsSystem;
    }


    @Override
    public DFSCredentials privateUserCredentials(UserIDAuth forUser, BucketPath forBucket) {
        return dfsSystem.systemDfs().getCredentials();
    }

    @Override
    public DFSCredentials publicUserCredentials(UserID forUser, BucketPath forBucket) {
        return dfsSystem.systemDfs().getCredentials();
    }
}
