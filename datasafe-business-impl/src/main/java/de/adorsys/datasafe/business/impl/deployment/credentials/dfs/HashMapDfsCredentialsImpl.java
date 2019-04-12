package de.adorsys.datasafe.business.impl.deployment.credentials.dfs;

import de.adorsys.datasafe.business.api.credentials.DFSCredentialsService;
import de.adorsys.datasafe.business.api.credentials.dto.DFSCredentials;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;

public class HashMapDfsCredentialsImpl implements DFSCredentialsService {

    @Override
    public DFSCredentials privateUserCredentials(UserIDAuth forUser, BucketPath forBucket) {
        return new DFSCredentials("private", "");
    }

    @Override
    public DFSCredentials publicUserCredentials(UserID forUser, BucketPath forBucket) {
        return new DFSCredentials("public", "");
    }
}
