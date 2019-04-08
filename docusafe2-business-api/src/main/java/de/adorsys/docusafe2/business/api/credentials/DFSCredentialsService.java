package de.adorsys.docusafe2.business.api.credentials;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.docusafe2.business.api.credentials.dto.DFSCredentials;
import de.adorsys.docusafe2.business.api.types.UserId;
import de.adorsys.docusafe2.business.api.types.UserIdAuth;

public interface DFSCredentialsService {

    DFSCredentials privateUserCredentials(UserIdAuth forUser, BucketPath forBucket);
    DFSCredentials publicUserCredentials(UserId forUser, BucketPath forBucket);
}
