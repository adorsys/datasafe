package de.adorsys.docusafe2.business.api.credentials;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.docusafe2.business.api.profile.UserProfileService;
import de.adorsys.docusafe2.business.api.types.DFSAccess;
import de.adorsys.docusafe2.business.api.types.UserId;
import de.adorsys.docusafe2.business.api.types.UserIdAuth;

import java.util.function.Function;

public interface BucketAccessService {

    DFSAccess privateAccessFor(UserIdAuth user, Function<UserProfileService, BucketPath> bucket);
    DFSAccess publicAccessFor(UserId user, Function<UserProfileService, BucketPath> bucket);
}
