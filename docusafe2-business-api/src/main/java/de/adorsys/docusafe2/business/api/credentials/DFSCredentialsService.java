package de.adorsys.docusafe2.business.api.credentials;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.docusafe2.business.api.credentials.dto.DFSCredentials;
import de.adorsys.docusafe2.business.api.types.UserIdAuth;

public interface DFSCredentialsService {

    DFSCredentials userCredentials(UserIdAuth forUser, BucketPath forBucket);
}
