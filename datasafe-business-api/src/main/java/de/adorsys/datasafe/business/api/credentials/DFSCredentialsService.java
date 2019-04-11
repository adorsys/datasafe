package de.adorsys.datasafe.business.api.credentials;

import de.adorsys.datasafe.business.api.credentials.dto.DFSCredentials;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;

public interface DFSCredentialsService {

    DFSCredentials privateUserCredentials(UserIDAuth forUser, BucketPath forBucket);
    DFSCredentials publicUserCredentials(UserID forUser, BucketPath forBucket);
}
