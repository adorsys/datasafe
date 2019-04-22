package de.adorsys.datasafe.business.impl.credentials;

import de.adorsys.datasafe.business.api.storage.dfs.credentials.DFSCredentials;
import de.adorsys.datasafe.business.api.storage.dfs.credentials.DFSCredentialsService;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.impl.profile.DFSSystem;

import javax.inject.Inject;
import java.net.URI;

// DEPLOYMENT
/**
 * Retrieves DFS credentials associated with some resource (i.e. s3 credentials for private folder access).
 */
public class SystemCredentialsServiceImpl {

    private final DFSSystem dfsSystem;

    @Inject
    public SystemCredentialsServiceImpl(DFSSystem dfsSystem) {
        this.dfsSystem = dfsSystem;
    }


    @Override
    public DFSCredentials privateUserCredentials(UserIDAuth forUser, URI forBucket) {
        return dfsSystem.systemDfs().getCredentials();
    }

    @Override
    public DFSCredentials publicUserCredentials(UserID forUser, URI forBucket) {
        return dfsSystem.systemDfs().getCredentials();
    }
}
