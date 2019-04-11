package de.adorsys.datasafe.business.impl.credentials;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.docusafe2.business.api.credentials.BucketAccessService;
import de.adorsys.docusafe2.business.api.credentials.DFSCredentialsService;
import de.adorsys.docusafe2.business.api.credentials.dto.DFSCredentials;
import de.adorsys.docusafe2.business.api.profile.UserProfileService;
import de.adorsys.docusafe2.business.api.types.DFSAccess;
import de.adorsys.docusafe2.business.api.types.UserId;
import de.adorsys.docusafe2.business.api.types.UserIdAuth;

import javax.inject.Inject;
import java.util.function.Function;

/**
 * Specifies how to access desired user resource (i.e. private bucket).
 */
public class BucketAccessServiceImpl implements BucketAccessService {

    private final UserProfileService profiles;
    private final DFSCredentialsService credentials;

    @Inject
    public BucketAccessServiceImpl(UserProfileService profiles, DFSCredentialsService credentials) {
        this.profiles = profiles;
        this.credentials = credentials;
    }

    @Override
    public DFSAccess privateAccessFor(UserIdAuth user, Function<UserProfileService, BucketPath> bucket) {
        BucketPath path = bucket.apply(profiles);

        DFSCredentials creds = credentials.privateUserCredentials(user, path);

        return DFSAccess.builder()
                .path(path)
                .credentials(creds)
                .build();
    }

    @Override
    public DFSAccess publicAccessFor(UserId user, Function<UserProfileService, BucketPath> bucket) {
        BucketPath path = bucket.apply(profiles);

        DFSCredentials creds = credentials.publicUserCredentials(user, path);

        return DFSAccess.builder()
                .path(path)
                .credentials(creds)
                .build();
    }
}
