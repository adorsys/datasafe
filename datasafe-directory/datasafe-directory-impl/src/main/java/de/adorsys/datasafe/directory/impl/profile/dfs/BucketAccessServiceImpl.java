package de.adorsys.datasafe.directory.impl.profile.dfs;

import dagger.Lazy;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

/**
 * Specifies how to access desired user resource (example: private bucket).
 * It can be used for:
 * 1. To add user-specific credentials, if it is 1 user per bucket or similar
 * 2. To redirect requests
 *
 * By default is no-op - simply wraps resource into {@link AbsoluteLocation}
 */
@Slf4j
@RuntimeDelegate
public class BucketAccessServiceImpl implements BucketAccessService {

    @Inject
    // Just declaring dependency, so no-op operations can user runtime-delegations, lazy
    // because there is circular construction time dependency for `withSystemAccess`
    public BucketAccessServiceImpl(Lazy<StorageKeyStoreOperations> storageKeyStoreOperations) {
    }

    /**
     * Do nothing, just wrap, real use case would be to plug user credentials to access bucket.
     */
    @Override
    public AbsoluteLocation<PrivateResource> privateAccessFor(UserIDAuth user, PrivateResource resource) {
        log.debug("get private access for user {} and bucket {}", user, resource);
        return new AbsoluteLocation<>(resource);
    }

    /**
     * Do nothing, just wrap, real use case would be to plug user credentials to access bucket.
     */
    @Override
    public AbsoluteLocation<PublicResource> publicAccessFor(UserID user, PublicResource resource) {
        log.debug("get public access for user {} and bucket {}", user, resource.location());
        return new AbsoluteLocation<>(resource);
    }

    /**
     * Do nothing, just wrap, real use case would be to plug user credentials to access bucket.
     */
    @Override
    public AbsoluteLocation withSystemAccess(AbsoluteLocation resource) {
        log.debug("get system access for {}", resource.location());
        return new AbsoluteLocation<>(resource);
    }
}
