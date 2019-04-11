package de.adorsys.datasafe.business.api.credentials;

import de.adorsys.datasafe.business.api.credentials.dto.DFSCredentials;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.datasafe.business.api.types.UserId;
import de.adorsys.datasafe.business.api.types.UserIdAuth;

public interface DFSCredentialsService {

    DFSCredentials privateUserCredentials(UserIdAuth forUser, BucketPath forBucket);
    DFSCredentials publicUserCredentials(UserId forUser, BucketPath forBucket);
}
