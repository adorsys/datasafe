package de.adorsys.docusafe2.business.impl.credentials;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.docusafe2.business.api.credentials.DFSCredentialsService;
import de.adorsys.docusafe2.business.api.credentials.dto.DFSCredentials;
import de.adorsys.docusafe2.business.api.profile.UserProfileService;
import de.adorsys.docusafe2.business.api.types.UserIdAuth;
import de.adorsys.docusafe2.business.api.types.DFSAccess;

import javax.inject.Inject;
import java.util.function.Function;

public class BucketAccessService {

    private final UserProfileService profiles;
    private final DFSCredentialsService credentials;

    @Inject
    public BucketAccessService(UserProfileService profiles, DFSCredentialsService credentials) {
        this.profiles = profiles;
        this.credentials = credentials;
    }

    public DFSAccess accessFor(UserIdAuth user, Function<UserProfileService, BucketPath> bucket) {
        BucketPath path = bucket.apply(profiles);

        DFSCredentials creds = credentials.userCredentials(user, path);

        return DFSAccess.builder()
                .path(path)
                .credentials(creds)
                .build();
    }
}
