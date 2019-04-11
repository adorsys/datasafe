package de.adorsys.datasafe.business.impl.credentials;

import de.adorsys.datasafe.business.api.credentials.DFSCredentialsService;
import de.adorsys.datasafe.business.api.credentials.dto.DFSCredentials;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;

import javax.inject.Inject;

/**
 * Retrieves DFS credentials associated with some resource (i.e. s3 credentials for private folder access).
 */
public class DFSCredentialsServiceImpl implements DFSCredentialsService {

    @Inject
    public DFSCredentialsServiceImpl() {
    }

    @Override
    public DFSCredentials privateUserCredentials(UserIDAuth forUser, BucketPath forBucket) {
        // FIXME https://github.com/adorsys/datasafe2/issues/12
        throw new UnsupportedOperationException("https://github.com/adorsys/datasafe2/issues/12");
    }

    @Override
    public DFSCredentials publicUserCredentials(UserID forUser, BucketPath forBucket) {
        // FIXME https://github.com/adorsys/datasafe2/issues/12
        throw new UnsupportedOperationException("https://github.com/adorsys/datasafe2/issues/12");
    }
}
