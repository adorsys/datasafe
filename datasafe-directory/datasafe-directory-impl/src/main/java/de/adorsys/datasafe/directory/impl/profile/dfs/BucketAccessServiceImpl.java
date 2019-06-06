package de.adorsys.datasafe.directory.impl.profile.dfs;

import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

/**
 * Specifies how to access desired user resource (example: private bucket). By default is no-op - simply wraps
 * resource into {@link AbsoluteLocation}
 */
@Slf4j
public class BucketAccessServiceImpl implements BucketAccessService {

    @Inject
    public BucketAccessServiceImpl() {
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
}
