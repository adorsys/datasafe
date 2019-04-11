package de.adorsys.datasafe.business.impl.credentials;

import de.adorsys.datasafe.business.api.credentials.BucketAccessService;
import de.adorsys.datasafe.business.api.credentials.DFSCredentialsService;
import de.adorsys.datasafe.business.api.credentials.dto.DFSCredentials;
import de.adorsys.datasafe.business.api.profile.UserProfileService;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;

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
    public DFSAccess privateAccessFor(UserIDAuth user, Function<UserProfileService, BucketPath> bucket) {
        BucketPath path = bucket.apply(profiles);

        DFSCredentials creds = credentials.privateUserCredentials(user, path);

        return DFSAccess.builder()
                .path(path)
                .credentials(creds)
                .build();
    }

    @Override
    public DFSAccess publicAccessFor(UserID user, Function<UserProfileService, BucketPath> bucket) {
        BucketPath path = bucket.apply(profiles);

        DFSCredentials creds = credentials.publicUserCredentials(user, path);

        return DFSAccess.builder()
                .path(path)
                .credentials(creds)
                .build();
    }
}
