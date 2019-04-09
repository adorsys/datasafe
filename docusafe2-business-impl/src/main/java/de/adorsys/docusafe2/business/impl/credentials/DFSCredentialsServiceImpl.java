package de.adorsys.docusafe2.business.impl.credentials;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.docusafe2.business.api.credentials.DFSCredentialsService;
import de.adorsys.docusafe2.business.api.credentials.dto.DFSCredentials;
import de.adorsys.docusafe2.business.api.types.UserId;
import de.adorsys.docusafe2.business.api.types.UserIdAuth;

import javax.inject.Inject;

public class DFSCredentialsServiceImpl implements DFSCredentialsService {

    @Inject
    public DFSCredentialsServiceImpl() {
    }

    @Override
    public DFSCredentials privateUserCredentials(UserIdAuth forUser, BucketPath forBucket) {
        return null;
    }

    @Override
    public DFSCredentials publicUserCredentials(UserId forUser, BucketPath forBucket) {
        return null;
    }
}
